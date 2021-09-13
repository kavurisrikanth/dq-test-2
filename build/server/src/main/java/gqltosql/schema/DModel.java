package gqltosql.schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import lists.TypeAndId;

public class DModel<T> {

	public static final byte NORMAL = 0x00;
	public static final byte EMBEDDED = 0x01;
	public static final byte EXTERNAL = 0x02;
	public static final byte TRANSIENT = 0x04;
	public static final byte DOCUMENT = 0x08;
	
	private int index;
	private String type;
	private String table;
	private DField<?, ?>[] fields;
	private Map<String, DField<T, ?>> fieldsByName = new HashMap<>();
	private DModel<?> parent;
	private int parentCount;
	private boolean entity;
	private boolean document;
	private DModelType modelType;
	private Supplier<T> ins;
	private int[] allTypes;
	private byte flags;
	private String external;

	public DModel(String type, int index, int count, int parentCount, String table, DModelType modelType, byte flags,
			Supplier<T> ins) {
		this.type = type;
		this.index = index;
		this.flags = flags;
		this.fields = new DField<?, ?>[count];
		this.parentCount = parentCount;
		this.table = table;
		this.modelType = modelType;
		this.ins = ins;
	}
	public DModel(String type, int index, int count, int parentCount, String table, DModelType modelType,
			Supplier<T> ins) {
		this(type, index, count, parentCount, table, modelType, NORMAL, ins);
	}

	public DModel(String type, int index, int count, int parentCount, String table, DModelType modelType, byte flags) {
		this(type, index, count, parentCount, table, modelType, flags, null);
	}
	
	public DModel(String type, int index, int count, int parentCount, String table, DModelType modelType) {
		this(type, index, count, parentCount, table, modelType, NORMAL, null);
	}
	
	public DModel<T> trans() {
		this.flags |= TRANSIENT;
		return this;
	}
	
	public DModel<T> external(String name) {
		this.flags |= EXTERNAL;
		this.external = name;
		return this;
	}
	
	public DModel<T> emb() {
		this.flags |= EMBEDDED;
		return this;
	}
	
	public DModel<T> document() {
		this.flags |= DOCUMENT;
		return this;
	}

	public int getIndex() {
		return index;
	}

	public DField<?, ?>[] getFields() {
		return fields;
	}

	public DModelType getModelType() {
		return modelType;
	}

	public void setEntity(boolean entity) {
		this.entity = entity;
	}

	public boolean checkFlag(byte val) {
		return (flags & val) != 0;
	}
	
	public boolean isEmbedded() {
		return checkFlag(EMBEDDED);
	}

	public boolean isExternal() {
		return checkFlag(EXTERNAL);
	}

	public boolean isDocument() {
		return checkFlag(DOCUMENT);
	}

	public boolean isTransient() {
		return checkFlag(TRANSIENT);
	}

	public String getTableName() {
		return table;
	}

	public DField<?, ?> getField(String name) {
		DField<?, ?> f = fieldsByName.get(name);
		if (f != null) {
			return f;
		}
		if (parent != null) {
			return parent.getField(name);
		}
		return null;
	}

	public DField<?, ?> getField(int index) {
		if (index < parentCount) {
			return parent.getField(index);
		}
		return fields[index - parentCount];
	}

	public boolean hasField(String name) {
		return getField(name) != null;
	}

	public String getType() {
		return type;
	}

	public boolean hasDeclField(String name) {
		return fieldsByName.containsKey(name);
	}

	public void setParent(DModel<?> parent) {
		this.parent = parent;
	}

	public DModel<?> getParent() {
		return parent;
	}

	public void addField(DField<T, ?> field) {
		fields[field.getIndex() - parentCount] = field;
		fieldsByName.put(field.getName(), field);
	}

	public int getParentCount() {
		return parentCount;
	}

	public int getFieldsCount() {
		return fields.length + parentCount;
	}

	public T newInstance() {
		return ins.get();
	}

	public <R> void addEnum(String name, int index, String column, int enumClss, Function<T, R> getter,
			BiConsumer<T, R> setter) {
		DPrimField<T, R> df = new DPrimField<T, R>(this, index, name, column, FieldPrimitiveType.Enum, getter, setter);
		df.setEnumType(enumClss);
		addField(df);
	}

	public <R> void addPrimitive(String name, int index, String column, FieldPrimitiveType primType,
			Function<T, R> getter, BiConsumer<T, R> setter) {
		addField(new DPrimField<T, R>(this, index, name, column, primType, getter, setter));
	}

	public <R> void addReference(String name, int index, String column, boolean child, DModel<?> ref,
			Function<T, R> getter, BiConsumer<T, R> setter) {
		addField(new DRefField<T, R>(this, index, name, column, child, ref, getter, setter));
	}

	public <R> void addReference(String name, int index, String column, boolean child, DModel<?> ref,
			Function<T, R> getter, BiConsumer<T, R> setter, Function<T, TypeAndId> refGetter) {
		addField(new DRefField2<T, R>(this, index, name, column, child, ref, getter, setter, refGetter));
	}

	public <R> void addEnumCollection(String name, int index, String column, String collTable, int enumClss,
			Function<T, List<R>> getter, BiConsumer<T, List<R>> setter) {
		DPrimCollField<T, R> df = new DPrimCollField<T, R>(this, index, name, column, collTable,
				FieldPrimitiveType.Enum, getter, setter);
		df.setEnumType(enumClss);
		addField(df);
	}

	public <R> void addPrimitiveCollection(String name, int index, String column, String collTable,
			FieldPrimitiveType primType, Function<T, List<R>> getter, BiConsumer<T, List<R>> setter) {
		addField(new DPrimCollField<T, R>(this, index, name, column, collTable, primType, getter, setter));
	}

	public <R> void addReferenceCollection(String name, int index, String column, String collTable, boolean child,
			DModel<?> ref, Function<T, List<R>> getter, BiConsumer<T, List<R>> setter) {
		if (ref.isEmbedded()) {
			addField(new DEmbCollField<T, R>(this, index, name, column, collTable, ref, getter, setter));
		} else {
			addField(new DRefCollField<T, R>(this, index, name, column, child, collTable, ref, getter, setter));
		}
	}

	public <R> void addReferenceCollection(String name, int index, String column, String collTable, boolean child,
			DModel<?> ref, Function<T, List<R>> getter, BiConsumer<T, List<R>> setter,
			Function<T, List<TypeAndId>> refGetter) {
		addField(new DRefCollField2<T, R>(this, index, name, column, child, collTable, ref, getter, setter, refGetter));
	}

	public <R> void addFlatCollection(String name, int index, String column, String collTable, DModel<?> ref,
			Function<T, List<R>> getter, String... flatPaths) {
		addField(new DFlatField<T, R>(this, index, name, column, collTable, ref, getter, flatPaths));
	}

	public <R> void addInverseCollection(String name, int index, String column, DModel<?> ref,
			Function<T, List<R>> getter) {
		addField(new DInverseCollField<T, R>(this, index, name, column, ref, getter));
	}

	public int[] getAllTypes() {
		return allTypes;
	}

	public void setAllTypes(int[] allTypes) {
		this.allTypes = allTypes;
	}

	public String getExternal() {
		return this.external;
	}
	
	@Override
	public String toString() {
		return type;
	}
	public void addAllParents(List<Integer> allParents) {
		if(parent != null) {
			allParents.add(parent.index);
			parent.addAllParents(allParents);
		}		
	}
}
