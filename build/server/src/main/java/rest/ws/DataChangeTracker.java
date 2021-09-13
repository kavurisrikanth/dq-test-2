package rest.ws;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.function.BiConsumer;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import classes.PrimitiveType;
import d3e.core.D3ELogger;
import d3e.core.D3ESubscription;
import d3e.core.DFile;
import d3e.core.IterableExt;
import d3e.core.ListExt;
import d3e.core.StructBase;
import gqltosql.schema.DField;
import gqltosql.schema.DModel;
import gqltosql.schema.FieldPrimitiveType;
import gqltosql.schema.FieldType;
import gqltosql.schema.IModelSchema;
import gqltosql2.Field;
import gqltosql2.OutObject;
import gqltosql2.Selection;
import io.reactivex.rxjava3.disposables.Disposable;
import lists.TypeAndId;
import store.DBObject;
import store.EntityHelperService;
import store.ListChanges;
import store.StoreEventType;

@Service
public class DataChangeTracker {
	@Autowired
	D3EWebsocket socket;

	@Autowired
	D3ESubscription subscription;

	@Autowired
	private IModelSchema schema;

	@Autowired
	private ObjectFactory<EntityHelperService> helperService;

	List<Key> keys = new ArrayList<>();
	Map<Integer, Map<Long, ObjectInterests>> perObjectListeners = new HashMap<>();
	Map<Integer, Vector<ObjectListener>> perTypeListeners = new HashMap<>();

	static class ObjectInterests {
		Vector<ObjectListener> fieldListeners = new Vector<>();
		Vector<ObjectUsage> refListeners = new Vector<>();

		public boolean isEmpty() {
			return fieldListeners.isEmpty() && refListeners.isEmpty();
		}
	}

	static class ObjectUsage {
		Field field;
		DisposableListener listener;
		int parentType;
		long parentId;
		int fieldIdx;

		public ObjectUsage(int parentType, long parentId, int fieldIdx, Field field, DisposableListener listener) {
			this.parentType = parentType;
			this.parentId = parentId;
			this.fieldIdx = fieldIdx;
			this.field = field;
			this.listener = listener;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ObjectUsage) {
				ObjectUsage other = (ObjectUsage) obj;
				return Objects.equals(other.listener, listener) && Objects.equals(other.field, field);
			}
			return false;

		}

		@Override
		public int hashCode() {
			return Objects.hash(field, listener);
		}
	}

	static class ObjectListener {
		BitSet fields;
		Disposable listener;
		Field field;

		public ObjectListener(BitSet fields, Disposable listener, Field field) {
			this.fields = fields;
			this.listener = listener;
			this.field = field;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ObjectListener) {
				ObjectListener other = (ObjectListener) obj;
				return Objects.equals(other.listener, listener) && Objects.equals(other.fields, fields)
						&& Objects.equals(other.field, field);
			}
			return false;

		}

		@Override
		public int hashCode() {
			return Objects.hash(fields, listener, field);
		}
	}

	static class Key {
		final int type;
		final BitSet fields;

		Key(int type, BitSet fields) {
			this.type = type;
			this.fields = fields;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Key) {
				Key other = (Key) obj;
				return other.type == type && Objects.equals(other.fields, this.fields);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return type + fields.hashCode();
		}
	}

	class DisposableListener implements Disposable {
		boolean disposed;
		ClientSession session;
		private Field field;
		private TypeAndId obj;
		List<TypeAndId> objects = new ArrayList<>();
		Set<Integer> types = new HashSet<>();

		public DisposableListener(Field field, TypeAndId obj, ClientSession session) {
			this.session = session;
			this.field = field;
			this.obj = obj;
		}

		@Override
		public void dispose() {
			disposed = true;
			onDispose(this);
		}

		@Override
		public boolean isDisposed() {
			return disposed;
		}

	}

	class TypeListener implements Disposable {
		Set<Integer> types = new HashSet<>();
		boolean disposed;
		BiConsumer<DBObject, StoreEventType> listener;
		BitSet fields;
		int type;

		TypeListener(int type, BitSet fields, BiConsumer<DBObject, StoreEventType> listener) {
			this.listener = listener;
			this.type = type;
			this.fields = fields;
		}

		@Override
		public void dispose() {
			disposed = true;
			onDispose(this);
		}

		@Override
		public boolean isDisposed() {
			return disposed;
		}

	}

	@PostConstruct
	public void init() {
		subscription.flowable.filter(i -> i.getEntity() instanceof DBObject).subscribe((i) -> {
			try {
				this.fire((DBObject) i.getEntity(), i.getType());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public Disposable listen(Object obj, Field field, ClientSession session) {
		int type = 0;
		long id = 0;
		if (obj instanceof OutObject) {
			OutObject outObject = (OutObject) obj;
			type = outObject.getType();
			id = outObject.getId();
		} else if (obj instanceof TypeAndId) {
			TypeAndId typeId = (TypeAndId) obj;
			type = typeId.type;
			id = typeId.id;
			obj = fromTypeAndId(typeId);
		} else {
			DBObject dbObj = (DBObject) obj;
			type = dbObj._typeIdx();
			id = dbObj.getId();
		}
		TypeAndId typeAndId = new TypeAndId(type, id);
		DisposableListener dl = new DisposableListener(field, typeAndId, session);
		scan(null, null, obj, field, dl, null, null);
		return dl;
	}

	public void onDispose(TypeListener tl) {
		int type = tl.type;
		Vector<ObjectListener> fieldListeners = perTypeListeners.get(type);
		if (fieldListeners == null) {
			return;
		}
		fieldListeners.remove(new ObjectListener(tl.fields, tl, null));
	}

	public void onDispose(DisposableListener dl) {
		for (TypeAndId ti : dl.objects) {
			Map<Long, ObjectInterests> objectListeners = perObjectListeners.get(ti.type);
			if (objectListeners != null) {
				ObjectInterests ol = objectListeners.get(ti.id);
				if (ol != null) {
					ListExt.removeWhere(ol.fieldListeners, (fl) -> fl.listener.isDisposed());
					if (ol.fieldListeners.isEmpty()) {
						objectListeners.remove(ti.id);
					}
					ListExt.removeWhere(ol.refListeners, (fl) -> fl.listener.isDisposed());
				}
			}
		}
		for (int type : dl.types) {
			Vector<ObjectListener> fieldListeners = perTypeListeners.get(type);
			if (fieldListeners != null) {
				ListExt.removeWhere(fieldListeners, (ol) -> ol.listener.isDisposed());
				if (fieldListeners.isEmpty()) {
					perTypeListeners.remove(type);
				}
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void scan(DBObject parent, DField parentField, Object obj, Field field, DisposableListener dl,
			ObjectsToSend toSend, List<Integer> allParents) {
		int type = 0;
		long id = 0;
		DBObject dbObj = null;
		if (obj instanceof OutObject) {
			OutObject outObject = (OutObject) obj;
			type = outObject.getType();
			id = outObject.getId();
		} else if (obj instanceof TypeAndId) {
			TypeAndId typeId = (TypeAndId) obj;
			type = typeId.type;
			id = typeId.id;
			obj = fromTypeAndId(typeId);
		} else {
			dbObj = (DBObject) obj;
			type = dbObj._typeIdx();
			if (obj instanceof StructBase) {
				type = ((StructBase) obj)._actualType();
			}
			id = dbObj.getId();
		}
		Selection sel = field.getSelectionForType(type);
		if (sel == null || sel.getFields().isEmpty()) {
			return;
		}
		Map<Long, ObjectInterests> perObj = perObjectListeners.get(type);
		if (perObj == null) {
			perObj = new HashMap<>();
			perObjectListeners.put(type, perObj);
		}
		ObjectInterests interests = perObj.get(id);
		if (interests == null) {
			interests = new ObjectInterests();
			perObj.put(id, interests);
		}
		ObjectListener ol = new ObjectListener(sel.getFieldsSet(), dl, field);
		interests.fieldListeners.add(ol);
		if (parent == null) {
			interests.refListeners.add(new ObjectUsage(-1, -1, -1, field, dl));
		} else {
			interests.refListeners
					.add(new ObjectUsage(parent._typeIdx(), parent.getId(), parentField.getIndex(), field, dl));
		}
		TypeAndId typeAndId = new TypeAndId(type, id);
		if (!dl.objects.contains(typeAndId)) {
			dl.objects.add(typeAndId);
		}
		dl.types.add(type);
		if (toSend != null && dbObj != null) {
			BitSet set = field.getBitSet(allParents);
			if(parentField != null && parentField.getReference().isEmbedded()) {
				toSend.addEmbedded(dl.session, parent, parentField, set);
			} else {
				toSend.add(dl.session, dbObj, set);
			}
		}
//		DModel<?> objType = schema.getType(type);
//		 D3ELogger.info("WATCHING " + SEL.GETTYPE().GETTYPE() + " ID: " + ID + " FIELDS: "
//		 + LISTEXT.MAP(SEL.GETFIELDS(), (F) -> F.GETField().getName()) + " Object Type: " + objType.getType() + " Type : " + type);
		for (Field f : sel.getFields()) {
			DField dField = f.getField();
			if(dField.getReference() == null || dField.getReference().getType().equals("DFile")) {
				continue;
			}
			FieldType fieldType = dField.getType();
			if (fieldType == FieldType.Reference) {
				Object value;
				if (obj instanceof OutObject) {
					OutObject outObject = (OutObject) obj;
					value = outObject.getFields().get(dField.getName());
				} else {
					value = dField.getValue(obj);
				}
				if (value != null && !(value instanceof DFile)) {
					if (toSend == null) {
						scan(dbObj, dField, value, f, dl, null, null);
					} else {
						DModel dm = dField.getReference();
						List<Integer> allParents2 = new ArrayList<Integer>();
						dm.addAllParents(allParents2);
						allParents2.add(dm.getIndex());
						scan(dbObj, dField, value, f, dl, toSend, allParents2);
					}

				}
			} else if (fieldType == FieldType.ReferenceCollection || fieldType == FieldType.InverseCollection) {
				List value = null;
				if (obj instanceof OutObject) {
					OutObject outObject = (OutObject) obj;
					value = (List) outObject.getFields().get(dField.getName());
				} else {
					value = (List) dField.getValue(obj);
				}
				if (value != null && !value.isEmpty()) {
					if (toSend == null) {
						for (Object o : value) {
							scan(dbObj, dField, o, f, dl, null, null);
						}
					} else {
						DModel dm = dField.getReference();
						List<Integer> allParents2 = new ArrayList<Integer>();
						dm.addAllParents(allParents2);
						allParents2.add(dm.getIndex());
						for (Object o : value) {
							scan(dbObj, dField, o, f, dl, toSend, allParents2);
						}
					}
				}
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void remove(DBObject obj, ObjectUsage ou) {
		int type = obj._typeIdx();
		long id = obj.getId();
		Selection sel = ou.field.getSelectionForType(type);
		if (sel.getFields().isEmpty()) {
			return;
		}
		Map<Long, ObjectInterests> perObj = perObjectListeners.get(type);
		if (perObj == null) {
			return;
		}
		ObjectInterests interests = perObj.get(id);
		if (interests == null) {
			return;
		}
		BitSet fieldsSet = sel.getFieldsSet();
		interests.fieldListeners.remove(new ObjectListener(fieldsSet, ou.listener, ou.field));
		interests.refListeners.remove(ou);
		D3ELogger.info("Un Watching " + sel.getType().getType() + " ID: " + id + " Fields: "
				+ ListExt.map(sel.getFields(), (f) -> f.getField().getName()));
		for (Field f : sel.getFields()) {
			DField dField = f.getField();
			if(dField.getReference() == null || dField.getReference().getType().equals("DFile")) {
				continue;
			}
			int fieldIndex = dField.getIndex();
			FieldType fieldType = dField.getType();
			if (fieldType == FieldType.Reference) {
				DBObject value = (DBObject) dField.getValue(obj);
				if (value != null) {
					List<ObjectUsage> refListeners = refListeners(value._typeIdx(), value.getId(), type, id,
							fieldIndex);
					for (var ol : refListeners) {
						remove(value, ol);
					}
				}
			} else if (fieldType == FieldType.ReferenceCollection || fieldType == FieldType.InverseCollection) {
				List<Object> value = (List<Object>) dField.getValue(obj);
				if (value != null && !value.isEmpty()) {
					for (Object o : value) {
						DBObject dbObj = null;
						if (o instanceof TypeAndId) {
							dbObj = fromTypeAndId((TypeAndId) o);
						} else {
							dbObj = (DBObject) o;
						}
						List<ObjectUsage> refListeners = refListeners(dbObj._typeIdx(), dbObj.getId(), type, id,
								fieldIndex);
						for (var ol : refListeners) {
							remove(dbObj, ol);
						}
					}
				}
			}
		}
	}

	private DBObject fromTypeAndId(TypeAndId ti) {
		String type = schema.getType(ti.type).getType();
		return helperService.getObject().get(type, ti.id);
	}

	public Disposable listen(int type, BitSet fields, BiConsumer<DBObject, StoreEventType> listener) {
		TypeListener dl = new TypeListener(type, fields, listener);
		Vector<ObjectListener> perType = perTypeListeners.get(type);
		if (perType == null) {
			perType = new Vector<>();
			perTypeListeners.put(type, perType);
		}
		ObjectListener ol = new ObjectListener(fields, dl, null);
		perType.add(ol);
		dl.types.add(type);
		return dl;
	}

	public void fire(DBObject object, StoreEventType changeType) {
		long id = object.getId();
		BitSet changes = object._changes();
		D3ELogger.info(
				"Fire: " + object._type() + " ID: " + id + " Event:" + changeType.toString() + " Changes: " + changes);
		int type = object._typeIdx();
		if (object instanceof StructBase) {
			type = ((StructBase) object)._actualType();
		}
		Map<Long, ObjectInterests> objectListeners = perObjectListeners.get(type);
		Set<ObjectListener> listeners = new HashSet<>();
		ObjectInterests interests = null;
		if (objectListeners != null) {
			interests = objectListeners.get(object.getId());
			if (interests != null) {
				for (ObjectListener ol : interests.fieldListeners) {
					if (ol.listener.isDisposed()) {
						// Remove
					} else if (ol.fields == null || ol.fields.intersects(changes)) {
						listeners.add(ol);
					}
				}
			}
		}
		Vector<ObjectListener> fieldListeners = perTypeListeners.get(type);
		if (fieldListeners != null) {
			for (ObjectListener ol : fieldListeners) {
				if (ol.listener.isDisposed()) {
					// Remove
				} else if (ol.fields == null || ol.fields.intersects(changes)) {
					listeners.add(ol);
				}
			}
		}
		ObjectsToSend toSend = new ObjectsToSend();
		if (interests != null) {
			DModel<?> model = schema.getType(type);
			for (int field : changes.stream().toArray()) {
				DField dField = model.getField(field);
				int fieldIndex = dField.getIndex();
				FieldType fieldType = dField.getType();
				if (fieldType == FieldType.Reference) {
					Object _oldValue = object._oldValue(field);
					Object _newValue = dField.getValue(object);
					if (_oldValue != null) {
						DBObject dbObj = null;
						int oldType = 0;
						long oldId = 0;
						if (_oldValue instanceof TypeAndId) {
							TypeAndId typeId = (TypeAndId) _oldValue;
							oldType = typeId.type;
							oldId = typeId.id;
							dbObj = fromTypeAndId((TypeAndId) _oldValue);
						} else {
							dbObj = (DBObject) _oldValue;
							oldType = dbObj._typeIdx();
							oldId = dbObj.getId();
						}
						List<ObjectUsage> refListeners = refListeners(oldType, oldId, type, id, fieldIndex);
						if (dbObj != null) {
							for (var ol : refListeners) {
								remove(dbObj, ol);
							}
						}
					}
					if (_newValue != null) {
						DBObject dbObj = null;
						if (_newValue instanceof TypeAndId) {
							dbObj = fromTypeAndId((TypeAndId) _newValue);
						} else {
							dbObj = (DBObject) _newValue;
						}
						DModel<?> dm = schema.getType(dbObj._typeIdx());
						List<Integer> allParents = new ArrayList<Integer>();
						dm.addAllParents(allParents);
						allParents.add(dm.getIndex());
						for (ObjectUsage ou : interests.refListeners) {
							Iterable<Field> expand = ListExt.expand(ou.field.getSelections(), s -> s.getFields());
							Iterable<Field> where = IterableExt.where(expand, (i) -> i.getField() == dField);
							for (Field f : where) {
								scan(object, dField, dbObj, f, ou.listener, toSend, allParents);
							}
						}
					}
				} else if (fieldType == FieldType.ReferenceCollection || fieldType == FieldType.InverseCollection) {
					ListChanges _oldChange = (ListChanges) object._oldValue(field);
					List _oldValue = _oldChange.getOld();
					List _newValue = (List) dField.getValue(object);
					for (Object o : _oldValue) {
						if (_newValue.contains(o)) {
							continue;
						}
						DBObject dbObj = null;
						int oldType = 0;
						long oldId = 0;
						if (o instanceof TypeAndId) {
							TypeAndId typeId = (TypeAndId) o;
							oldType = typeId.type;
							oldId = typeId.id;
							dbObj = fromTypeAndId((TypeAndId) o);
						} else {
							dbObj = (DBObject) o;
							oldType = dbObj._typeIdx();
							oldId = dbObj.getId();
						}
						List<ObjectUsage> refListeners = refListeners(oldType, oldId, type, id, fieldIndex);
						if (dbObj != null) {
							for (var ol : refListeners) {
								remove(dbObj, ol);
							}
						}
					}

					for (Object o : _newValue) {
						if (_oldValue.contains(o)) {
							continue;
						}
						DBObject dbObj = null;
						if (o instanceof TypeAndId) {
							dbObj = fromTypeAndId((TypeAndId) o);
						} else {
							dbObj = (DBObject) o;
						}
						DModel<?> dm = schema.getType(dbObj._typeIdx());
						List<Integer> allParents = new ArrayList<Integer>();
						dm.addAllParents(allParents);
						allParents.add(dm.getIndex());
						for (ObjectUsage ou : interests.refListeners) {
							Iterable<Field> expand = ListExt.expand(ou.field.getSelections(), s -> s.getFields());
							Iterable<Field> where = IterableExt.where(expand, (i) -> i.getField() == dField);
							for (Field f : where) {
								scan(object, dField, dbObj, f, ou.listener, toSend, allParents);
							}
						}
					}
				}
			}
		}
		if (listeners.isEmpty()) {
			return;
		}
		if (changeType == StoreEventType.Delete) {
			List<ObjectUsage> refListeners = refListeners(type, id);
			if (refListeners != null) {
				for (var refL : refListeners) {
					remove(object, refL);
				}
			}
		}

		for (ObjectListener ol : listeners) {
			if (ol.listener instanceof DisposableListener) {
				DisposableListener dl = (DisposableListener) ol.listener;
				if (changeType == StoreEventType.Delete) {
					toSend.delete(dl.session, new TypeAndId(type, id));
				} else {
					BitSet set = new BitSet();
					set.or(ol.fields);
					set.and(changes);
					toSend.add(dl.session, object, set);
				}
			} else {
				TypeListener tl = (TypeListener) ol.listener;
				tl.listener.accept(object, changeType);
			}
		}
		toSend.send(socket);
	}

	private List<ObjectUsage> refListeners(int type, long id) {
		Map<Long, ObjectInterests> objectListeners = perObjectListeners.get(type);
		if (objectListeners != null) {
			ObjectInterests interests = objectListeners.get(id);
			if (interests != null) {
				return ListExt.from(interests.refListeners, false);
			}
		}
		return Collections.emptyList();
	}

	private List<ObjectUsage> refListeners(int type, long id, int parentType, long parentId, int parentFieldIndex) {
		Map<Long, ObjectInterests> objectListeners = perObjectListeners.get(type);
		if (objectListeners != null) {
			ObjectInterests interests = objectListeners.get(id);
			if (interests != null) {
				return ListExt.where(interests.refListeners, ol -> ol.parentType == parentType
						&& ol.parentId == parentId && ol.fieldIdx == parentFieldIndex);
			}
		}
		return Collections.emptyList();
	}

}
