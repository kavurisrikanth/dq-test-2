package rest.ws;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import classes.LoginResult;
import d3e.core.CurrentUser;
import d3e.core.D3ELogger;
import d3e.core.DFile;
import d3e.core.ListExt;
import d3e.core.MD5Util;
import d3e.core.TransactionWrapper;
import gqltosql.schema.DChannel;
import gqltosql.schema.DField;
import gqltosql.schema.DMessage;
import gqltosql.schema.DModel;
import gqltosql.schema.DParam;
import gqltosql.schema.FieldType;
import gqltosql.schema.IModelSchema;
import gqltosql2.Field;
import gqltosql2.Selection;
import io.reactivex.rxjava3.functions.Cancellable;
import lists.TypeAndId;
import models.CreatableObject;
import security.JwtTokenUtil;
import security.UserProxy;
import store.DBObject;
import store.DatabaseObject;
import store.EntityHelperService;
import store.ListChanges;
import store.ListChanges.Change;
import store.ValidationFailedException;

@Configuration
@EnableWebSocket
public class D3EWebsocket extends BinaryWebSocketHandler implements WebSocketConfigurer {

	private static final int ERROR = 0;
	private static final int CONFIRM_TEMPLATE = 1;
	private static final int HASH_CHECK = 2;
	private static final int TYPE_EXCHANGE = 3;
	private static final int RESTORE = 4;
	private static final int OBJECT_QUERY = 5;
	private static final int DATA_QUERY = 6;
	private static final int SAVE = 7;
	private static final int DELETE = 8;
	private static final int UNSUBSCRIBE = 9;
	private static final int LOGIN = 10;
	private static final int LOGIN_WITH_TOKEN = 11;
	private static final int CONNECT = 12;
	private static final int OBJECTS = -1;
	private static final int CHANNEL_MESSAGE = -2;

	@Autowired
	private TemplateManager templateManager;

	@Autowired
	private TransactionWrapper wrapper;

	@Autowired
	private ObjectFactory<EntityHelperService> helperService;

	@Autowired
	private Channels channels;

	@Autowired
	private RocketQuery query;

	@Autowired
	private RocketMutation mutation;

	@Autowired
	private IModelSchema schema;

	@Autowired
	private MasterTemplate master;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Value("${rocket.reconnectPeriode:300}")
	private int reconnectPeriode;

	private Map<String, ClientSession> sessions = new HashMap<>();
	private Map<String, ClientSession> disconnectedSessions;
	private Map<ClientSession, Map<String, Cancellable>> subscriptions = new HashMap<>();

	@PostConstruct
	public void init() {
		disconnectedSessions = new HashMap<>(); // TODO new
		// MapMaker().concurrencyLevel(4).weakValues().expiration(reconnectPeriode,
		// TimeUnit.SECONDS);
	}

	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(this, "/api/rocket").setAllowedOrigins("*");
	}

	@Override
	public boolean supportsPartialMessages() {
		return true;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		D3ELogger.info("D3EWebsocket connected. " + session.getId());
		sessions.put(session.getId(), new ClientSession(session));
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		D3ELogger.info("D3EWebsocket connection closed. " + status + ", " + session.getId());
		ClientSession cs = sessions.remove(session.getId());
		disconnectedSessions.put(cs.getId(), cs);
		Map<String, Cancellable> map = subscriptions.get(cs);
		if (map != null) {
			for (Cancellable c : map.values()) {
				try {
					c.cancel();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
		wrapper.doInTransaction(() -> {
			if (cs.userId != 0) {
				CurrentUser.set(cs.userType, cs.userId, cs.getId());
			} else {
				SecurityContextHolder.getContext().setAuthentication(null);
			}
			channels.disconnect(cs);
		});
		channels.disconnect(cs);
		cs.session = null;
	}

	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
		ClientSession cs = sessions.get(session.getId());
		ByteBuffer payload = message.getPayload();
		cs.stream.writeBytes(payload.array());
		if (!message.isLast()) {
			return;
		}
		RocketMessage reader = new RocketMessage(cs);
		cs.stream = new ByteArrayOutputStream();
		try {
			wrapper.doInTransaction(() -> {
				if (cs.userId != 0) {
					CurrentUser.set(cs.userType, cs.userId, cs.getId());
				} else {
					SecurityContextHolder.getContext().setAuthentication(null);
				}
				onMessage(cs, reader);
				reader.flush();
				D3ELogger.info("Done");
			});
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cs.isLocked()) {
//				reader.flush();
			}
		}
	}

	private void onMessage(ClientSession ses, RocketMessage msg) {
		int id = msg.readInt();
		if (id == CHANNEL_MESSAGE) {
			D3ELogger.info("Rocket Message: -2");
			msg.writeInt(0);
			onChannelMessage(ses, msg);
			return;
		}
		msg.writeInt(id);
		int type = msg.readByte();
		D3ELogger.info("Rocket Message: " + type);
		switch (type) {
		case CONFIRM_TEMPLATE:
			onConfirmTemplate(ses, msg);
			break;
		case HASH_CHECK:
			onHashCheck(ses, msg);
			break;
		case TYPE_EXCHANGE:
			onTypeExchange(ses, msg);
			break;
		case RESTORE:
			onRestore(ses, msg);
			break;
		case OBJECT_QUERY:
			onObjectQuery(ses, msg);
			break;
		case DATA_QUERY:
			onDataQuery(ses, msg);
			break;
		case SAVE:
			onSave(ses, msg);
			break;
		case DELETE:
			onDelete(ses, msg);
			break;
		case UNSUBSCRIBE:
			onUnsubscribe(ses, msg);
			break;
		case LOGIN:
			onLogin(ses, msg);
			break;
		case CONNECT:
			onChannelConnect(ses, msg);
			break;
		case LOGIN_WITH_TOKEN:
			onLoginWithToken(ses, msg);
			break;
		default:
			msg.writeByte(ERROR);
			msg.writeString("Unsupported type: " + type);
		}
	}

	private void onLogin(ClientSession ses, RocketMessage msg) {
		msg.writeByte(LOGIN);
		D3ELogger.info("Login");
		int usage = msg.readInt();
		String type = msg.readString();
		String email = msg.readString();
		String phone = msg.readString();
		String username = msg.readString();
		String password = msg.readString();
		String deviceToken = msg.readString();
		String token = msg.readString();
		String code = msg.readString();
		try {
			String q;
			if (StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(token)) {
				q = "loginWithOTP";
			} else if (StringUtils.isNotEmpty(email)) {
				// UserTypeWithEmailAndPassword
				q = "login" + type + "WithEmailAndPassword";
			} else if (StringUtils.isNotEmpty(phone)) {
				// UserTypeWithPhoneAndPassword
				q = "login" + type + "WithPhoneAndPassword";
			} else {
				// UserTypeWithUsernameAndPassword
				q = "login" + type + "WithUsernameAndPassword";
			}
			LoginResult res = query.login(q, email, phone, username, password, deviceToken, token, code);
			if (res.isSuccess()) {
				ses.userId = res.getUserObject().getId();
				ses.userType = res.getUserObject().getClass().getSimpleName();
			}
			msg.writeByte(0);
			TemplateUsage usageType = ses.template.getUsageType(usage);
			new RocketObjectDataFetcher(ses.template, msg, (t) -> fromTypeAndId(t)).fetch(usageType, res);
		} catch (ValidationFailedException e) {
			msg.writeByte(1);
			msg.writeStringList(e.getErrors());
		} catch (Exception e) {
			msg.writeByte(1);
			msg.writeStringList(ListExt.asList(e.getMessage()));
			e.printStackTrace();
		}
	}

	private void onLoginWithToken(ClientSession ses, RocketMessage msg) {
		msg.writeByte(LOGIN_WITH_TOKEN);
		D3ELogger.info("Login With Token");
		String token = msg.readString();
		UserProxy userProxy = jwtTokenUtil.validateToken(token);
		if (userProxy != null) {
			ses.userId = userProxy.userId;
			ses.userType = userProxy.type;
			msg.writeByte(0);
		} else {
			msg.writeByte(1);
		}
	}

	private void onChannelMessage(ClientSession ses, RocketMessage msg) {
		int chIdx = msg.readInt();
		int msgIndex = msg.readInt();
		TemplateChannel tc = ses.template.getChannel(chIdx);
		DChannel dm = tc.getChannel();
		DMessage message = dm.getMessage(msgIndex);
		if (message == null) {
			msg.writeByte(1);
			return;
		}
		int msgSrvIdx = message.getIndex();
		RocketInputContext ctx = new RocketInputContext(helperService.getObject(), ses.template, msg);
		try {
			channels.onMessage(dm, msgSrvIdx, ses, ctx);
			msg.writeByte(0);
		} catch (Exception e) {
			msg.writeByte(1);
			msg.writeString(e.getMessage());
			e.printStackTrace();
		}
	}

	private void onChannelConnect(ClientSession ses, RocketMessage msg) {
		D3ELogger.info("Connecting to channel");
		msg.writeByte(CONNECT);
		int chIdx = msg.readInt();
		TemplateChannel tc = ses.template.getChannel(chIdx);
		DChannel dm = tc.getChannel();
		try {
			boolean result = channels.connect(dm, ses, helperService.getObject(), ses.template);
			D3ELogger.info("Channel connect result: " + result);
			if (result) {
				msg.writeByte(0);
			} else {
				msg.writeByte(1);
				msg.writeString("Conenction refused");
			}
		} catch (Exception e) {
			msg.writeByte(1);
			msg.writeString(e.getMessage());
			e.printStackTrace();
		}
	}

	private void onUnsubscribe(ClientSession ses, RocketMessage msg) {
		// msg.writeByte(UNSUBSCRIBE);
		String subId = msg.readString();
		Map<String, Cancellable> map = subscriptions.get(ses);
		if (map == null) {
			return;
		}
		Cancellable subscription = map.remove(subId);
		if (subscription != null) {
			try {
				subscription.cancel();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	private void onDelete(ClientSession ses, RocketMessage msg) {
		msg.writeByte(DELETE);
		int type = msg.readInt();
		long id = msg.readLong();
		try {
			TemplateType tt = ses.template.getType(type);
			D3ELogger.info("Delete: " + tt.getModel().getType() + ", id: " + id);
			DBObject obj = fromTypeAndId(new TypeAndId(tt.getModel().getIndex(), id));
			if (obj instanceof CreatableObject) {
				mutation.delete((CreatableObject) obj, false);
				msg.writeByte(0);
			} else {
				msg.writeByte(1);
				msg.writeStringList(ListExt.asList("Must be Creatable Object"));
			}
		} catch (ValidationFailedException e) {
			msg.writeByte(1);
			msg.writeStringList(e.getErrors());
		} catch (Exception e) {
			msg.writeByte(1);
			msg.writeStringList(ListExt.asList(e.getMessage()));
			e.printStackTrace();
		}
	}

	public void sendChanges(ClientSession session, Map<DBObject, BitSet> objects) {
		D3ELogger.info("Send changes: ");
		RocketMessage msg = new RocketMessage(session);
		msg.writeInt(OBJECTS);
		msg.writeBoolean(true);
		Template template = session.template;
		int count = objects.size();
		msg.writeInt(count);
		for (var entry : objects.entrySet()) {
			DBObject key = entry.getKey();
			D3ELogger.info("Send changes:  for " + key + ", id: " + key.getId());
			writeObject(msg, template, entry.getKey(), entry.getValue());
		}
		msg.flush();
	}

	public void sendEmbeddedChanges(ClientSession session, Map<DBObject, Map<DField, BitSet>> objects) {
		D3ELogger.info("Send embedded changes: ");
		RocketMessage msg = new RocketMessage(session);
		msg.writeInt(OBJECTS);
		msg.writeBoolean(true);
		Template template = session.template;
		int count = objects.size();
		msg.writeInt(count);
		for (var entry : objects.entrySet()) {
			DBObject parent = entry.getKey();
			Map<DField, BitSet> embeddeds = entry.getValue();
			int typeIdx = template.toClientTypeIdx(parent._typeIdx());
			msg.writeInt(typeIdx);
			msg.writeLong(parent.getId());
			TemplateType type = template.getType(typeIdx);
			embeddeds.forEach((f, bits) -> {
				DBObject em = (DBObject) f.getValue(parent);
				int cfid = type.toClientIdx(f.getIndex());
				msg.writeInt(cfid);
				writeObject(msg, template, em, bits);
			});
			msg.writeInt(-1);
		}
		msg.flush();
	}

	private void writeObject(RocketMessage msg, Template template, DBObject object, BitSet fields) {
		int serverType = object._typeIdx();
		int typeIdx = template.toClientTypeIdx(serverType);
		TemplateType type = template.getType(typeIdx);
		msg.writeInt(typeIdx);
		if(!type.getModel().isEmbedded()) {
			msg.writeLong(object.getId());
		}
		fields.stream().forEach(b -> {
			int cidx = type.toClientIdx(b);
			DField f = type.getField(cidx);
			D3ELogger.info("w field: " + f.getName());
			if (f.getType() == FieldType.InverseCollection || f.getType() == FieldType.PrimitiveCollection
					|| f.getType() == FieldType.ReferenceCollection) {
				Object val = f.getValue(object);
//				ListChanges listChanges = (ListChanges) object._oldValue(b);
//				if (listChanges != null) {
//					List<Change> changes = listChanges.compile((List) val);
//					if (changes.isEmpty()) {
//						D3ELogger.info("No coll changes");
//						return;
//					}
//					msg.writeInt(cidx);
//					writeListChanges(msg, template, changes, f);
//				} else {
					msg.writeInt(cidx);
					writeCompleteList(msg, template, (List) val, f);
//				}
			} else {
				msg.writeInt(cidx);
				Object val = f.getValue(object);
				writeChangeVal(msg, template, f, val);
			}
		});
		msg.writeInt(-1);
	}

	private void writeCompleteList(RocketMessage msg, Template template, List newColl, DField field) {
		D3ELogger.info("List Change all: " + newColl.size());
		msg.writeInt(newColl.size());
		for (Object val : newColl) {
			if (field.getType() == FieldType.PrimitiveCollection) {
				msg.writePrimitiveField(val, field, template);
			} else {
				Object object = val;
				DBObject dbObj = null;
				if (object instanceof TypeAndId) {
					dbObj = fromTypeAndId((TypeAndId) object);
				} else {
					dbObj = (DBObject) object;
				}
				int clientType = template.toClientTypeIdx(dbObj._typeIdx());
				msg.writeInt(clientType);
				if (!field.getReference().isEmbedded()) {
					msg.writeLong(dbObj.getId());
				}
				msg.writeInt(-1);
			}
		}
	}

	private void writeListChanges(RocketMessage msg, Template template, List<Change> changes, DField field) {
		D3ELogger.info("List Changes: " + changes.size() + ", " + changes);
		msg.writeInt(-changes.size());
		for (Change change : changes) {
			if (change.type == ListChanges.ChangeType.Added) {
				D3ELogger.info("Added At: " + change.index);
				msg.writeInt(change.index + 1);
				if (field.getType() == FieldType.PrimitiveCollection) {
					msg.writePrimitiveField(change.obj, field, template);
				} else {
					Object object = change.obj;
					DBObject dbObj = null;
					if (object instanceof TypeAndId) {
						dbObj = fromTypeAndId((TypeAndId) object);
					} else {
						dbObj = (DBObject) object;
					}
					int clientType = template.toClientTypeIdx(dbObj._typeIdx());
					msg.writeInt(clientType);
					if (!field.getReference().isEmbedded()) {
						msg.writeLong(dbObj.getId());
					}
					msg.writeInt(-1);
				}
			} else {
				D3ELogger.info("Removed At: " + change.index);
				msg.writeInt(-(change.index + 1));
			}
		}
	}

	private DBObject fromTypeAndId(TypeAndId ti) {
		String type = schema.getType(ti.type).getType();
		return helperService.getObject().get(type, ti.id);
	}

	private void writeChangeVal(RocketMessage msg, Template template, DField field, Object value) {
		if (field.getType() == FieldType.Primitive) {
			msg.writePrimitiveField(value, field, template);
		} else if (field.getType() == FieldType.Reference) {
			if (value == null) {
				msg.writeNull();
			} else {
				if (value instanceof TypeAndId) {
					TypeAndId ti = (TypeAndId) value;
					int typeIdx = template.toClientTypeIdx(ti.type);
					msg.writeInt(typeIdx);
					if (!field.getReference().isEmbedded()) {
						msg.writeLong(ti.id);
					}
				} else if (value instanceof DFile) {
				  DFile file = (DFile) value;
				  RocketInputContext.writeDFile(msg, template, file);
				} else {
					DBObject dbObj = (DBObject) value;
					int typeIdx = template.toClientTypeIdx(dbObj._typeIdx());
					msg.writeInt(typeIdx);
					if (!field.getReference().isEmbedded()) {
						msg.writeLong(dbObj.getId());
					}
				}
				msg.writeInt(-1);
			}
		} else {
			throw new RuntimeException("Unsupported type. " + value.getClass());
		}
	}

	private void onSave(ClientSession ses, RocketMessage msg) {
		msg.writeByte(SAVE);
		RocketInputContext ctx = new RocketInputContext(helperService.getObject(), ses.template, msg);
		DatabaseObject obj = (DatabaseObject) ctx.readObject();
		D3ELogger.info("Save: " + obj._type() + ", LID: " + obj.getLocalId() + ", ID: " + obj.getId());
		try {
			if (obj instanceof CreatableObject) {
				mutation.save((CreatableObject) obj);
				List<long[]> localIds = new ArrayList<>();
				obj.updateMasters(a -> {
					if (a.getId() == 0l) {
						a.setId(a.getLocalId());
					} else if (a.getLocalId() != 0l) {
						localIds.add(
								new long[] { ses.template.toClientTypeIdx(a._typeIdx()), a.getLocalId(), a.getId() });
					}
					a.setLocalId(0);
				});
				if (obj.getId() == 0l) {
					obj.setId(obj.getLocalId());
				} else if (obj.getLocalId() != 0l) {
					localIds.add(
							new long[] { ses.template.toClientTypeIdx(obj._typeIdx()), obj.getLocalId(), obj.getId() });
				}
				obj.setLocalId(0);
				msg.writeByte(0);
				msg.writeInt(localIds.size());
				localIds.forEach((v) -> {
					msg.writeInt((int) v[0]);
					msg.writeLong((int) v[1]);
					msg.writeLong((int) v[2]);
				});
				ctx.writeObject(obj);
			} else {
				msg.writeByte(1);
				msg.writeStringList(ListExt.asList("Only creatable objects can be saved"));
			}
		} catch (ValidationFailedException e) {
			msg.writeByte(1);
			msg.writeStringList(e.getErrors());
		} catch (Exception e) {
			msg.writeByte(1);
			msg.writeStringList(ListExt.asList(e.getMessage()));
			e.printStackTrace();
		}
	}

	private void onObjectQuery(ClientSession ses, RocketMessage msg) {
		msg.writeByte(OBJECT_QUERY);
		D3ELogger.info("Object Query");
		int type = msg.readInt();
		boolean subscribed = msg.readBoolean();
		int usageId = msg.readInt();
		TemplateUsage usage = ses.template.getUsageType(usageId);
		RocketInputContext ctx = new RocketInputContext(helperService.getObject(), ses.template, msg);
		try {
			TemplateType tt = ses.template.getType(type);
			QueryResult queryRes = query.executeOperation("get" + tt.getModel().getType() + "ById",
					convertToField(usage, ses.template), ctx, subscribed, ses);
			msg.writeByte(0);
			if (subscribed) {
				Cancellable sub = queryRes.changeTracker;
				String subId = newSubId();

				Map<String, Cancellable> map = subscriptions.get(ses);
				if (map == null) {
					map = new HashMap<>();
					subscriptions.put(ses, map);
				}
				map.put(subId, sub);
				msg.writeString(subId);
			}
			writeQueryResult(queryRes, usage, ses.template, msg);
		} catch (ValidationFailedException e) {
			msg.writeByte(1);
			msg.writeStringList(e.getErrors());
		} catch (Exception e) {
			msg.writeByte(1);
			msg.writeStringList(ListExt.asList(e.getMessage()));
			e.printStackTrace();
		}
	}

	private void writeQueryResult(QueryResult res, TemplateUsage usage, Template template, RocketMessage msg) {
		if (res.external) {
			new RocketObjectDataFetcher(template, msg, (t) -> fromTypeAndId(t)).fetch(usage, res.value);
		} else {
			new RocketOutObjectFetcher(template, msg).fetch(usage, res.value);
		}
	}

	private void onDataQuery(ClientSession ses, RocketMessage msg) {
		msg.writeByte(DATA_QUERY);
		String query = msg.readString();
		boolean subscribed = msg.readBoolean();
		D3ELogger.info("Data Query: " + query);
		int usage = msg.readInt();
		TemplateUsage tu = ses.template.getUsageType(usage);
		RocketInputContext ctx = new RocketInputContext(helperService.getObject(), ses.template, msg);
		try {
			Field field = convertToField(tu, ses.template);
			QueryResult res = this.query.executeOperation("get" + query, field, ctx, subscribed, ses);
			msg.writeByte(0);
			if (subscribed) {
				Cancellable sub = res.changeTracker;
				String subId = newSubId();
				Map<String, Cancellable> map = subscriptions.get(ses);
				if (map == null) {
					map = new HashMap<>();
					subscriptions.put(ses, map);
				}
				map.put(subId, sub);
				msg.writeString(subId);
			}
			writeQueryResult(res, tu, ses.template, msg);
		} catch (ValidationFailedException e) {
			msg.writeByte(1);
			msg.writeStringList(e.getErrors());
		} catch (Exception e) {
			msg.writeByte(1);
			msg.writeStringList(ListExt.asList(e.getMessage()));
			e.printStackTrace();
		}
	}

	private String newSubId() {
		return UUID.randomUUID().toString();
	}

	private Field convertToField(TemplateUsage tu, Template template) {
		if (tu.getField() != null) {
			return tu.getField();
		}
		Field f = new Field();
		f.setSelections(createSelections(tu.getTypes(), template));
		tu.setField(f);
		return f;
	}

	private List<Selection> createSelections(UsageType[] types, Template template) {
		List<Selection> selections = new ArrayList<>();
		for (UsageType ut : types) {
			selections.add(createSelection(ut, template));
		}
		return selections;
	}

	private Selection createSelection(UsageType ut, Template template) {
		TemplateType type = template.getType(ut.getType());
		List<Field> fields = new ArrayList<>();
		for (UsageField uf : ut.getFields()) {
			Field f = new Field();
			f.setField(type.getField(uf.getField()));
			f.setSelections(createSelections(uf.getTypes(), template));
			fields.add(f);
		}
		return new Selection(type.getModel(), fields);
	}

	private void onRestore(ClientSession ses, RocketMessage msg) {
		msg.writeByte(RESTORE);
		String sessionId = msg.readString();
		D3ELogger.info("Restore: " + sessionId);
		if (sessions.containsKey(sessionId)) {
			msg.writeByte(0);
		} else {
			ClientSession disSes = disconnectedSessions.remove(sessionId);
			if (disSes == null) {
				msg.writeByte(1);
			} else {
				disSes.session = ses.session;
				msg.writeByte(0);
				sessions.put(disSes.getId(), disSes);
			}
		}
	}

	private void onConfirmTemplate(ClientSession ses, RocketMessage msg) {
		msg.writeByte(CONFIRM_TEMPLATE);
		msg.writeString(ses.getId());
		String templateHash = msg.readString();
		if (templateManager.hasTemplate(templateHash)) {
			ses.template = templateManager.getTemplate(templateHash);
			msg.writeByte(0);
			D3ELogger.info("Template matched: " + templateHash);
		} else {
			msg.writeByte(1);
			D3ELogger.info("Template not matched: " + templateHash);
		}
	}

	private void onHashCheck(ClientSession ses, RocketMessage msg) {
		msg.writeByte(HASH_CHECK);
		int types = msg.readInt();
		int usages = msg.readInt();
		int channels = msg.readInt();
		ses.template = new Template(types, usages, channels);

		// Types
		D3ELogger.info("Registering types: " + types);
		List<Integer> unknownTypes = new ArrayList<>();
		for (int i = 0; i < types; i++) {
			String typeHash = msg.readString();
			TemplateType tt = master.getTemplateType(typeHash);
			if (tt == null) {
				unknownTypes.add(i);
			} else {
				ses.template.setTypeTemplate(i, tt);
			}
		}
		D3ELogger.info("Unknown types: " + unknownTypes.size());

		// Usages
		D3ELogger.info("Registering usages: " + usages);
		List<Integer> unknownUsages = new ArrayList<>();
		for (int i = 0; i < usages; i++) {
			String usageHash = msg.readString();
			TemplateUsage ut = master.getUsageTemplate(usageHash);
			ses.template.setUsageTemplate(i, ut);
			if (ut == null) {
				unknownUsages.add(i);
			}
		}
		D3ELogger.info("Unknown usages: " + unknownUsages.size());

		// Channels
		D3ELogger.info("Registering channels: " + channels);
		List<Integer> unknownChannels = new ArrayList<>();
		for (int i = 0; i < channels; i++) {
			String channelHash = msg.readString();
			TemplateChannel tc = master.getChannelTemplate(channelHash);
			if (tc == null) {
				D3ELogger.info("Unknown channel: " + i);
				unknownChannels.add(i);
			} else {
				ses.template.setChannelTemplate(i, tc);
			}
		}
		D3ELogger.info("Unknown channels: " + unknownChannels.size());

		if (unknownTypes.isEmpty() && unknownUsages.isEmpty() && unknownChannels.isEmpty()) {
			msg.writeByte(0);
			computeTemplateMD5AndAddToManager(ses.template);
		} else {
			msg.writeByte(1);
			msg.writeIntegerList(unknownTypes);
			msg.writeIntegerList(unknownUsages);
			msg.writeIntegerList(unknownChannels);
		}
	}

	private void computeTemplateMD5AndAddToManager(Template template) {
		List<String> md5 = new ArrayList<>();
		for (TemplateType tt : template.getTypes()) {
			md5.add(tt.getHash());
		}
		for (TemplateUsage tu : template.getUsages()) {
			md5.add(tu.getHash());
		}
		for (TemplateChannel tc : template.getChannels()) {
			md5.add(tc.getHash());
		}
		template.setHash(MD5Util.md5(md5));
		templateManager.addTemplate(template);
		D3ELogger.info("Template created: " + template.getHash());
	}

	private void onTypeExchange(ClientSession ses, RocketMessage msg) {
		msg.writeByte(TYPE_EXCHANGE);
		Template template = ses.template;
		// Types
		int typesCount = msg.readInt();
		for (int i = 0; i < typesCount; i++) {
			int idx = msg.readInt();
			String type = msg.readString();
			DModel<?> md = schema.getType(type);
			if (md != null) {
				int fieldsCount = msg.readInt();
				TemplateType tt = new TemplateType(md, fieldsCount);
				template.setTypeTemplate(idx, tt);
				for (int j = 0; j < fieldsCount; j++) {
					String field = msg.readString();
					int typeIdx = msg.readInt();
					// TODO check type
					DField<?, ?> df = md.getField(field);
					if (df == null) {
						System.out.println();
					}
					tt.addField(j, df);
				}
				master.addTypeTemplate(tt);
			} else {
				List<String> md5 = new ArrayList<>();
				md5.add(type);
				int fieldsCount = msg.readInt();
				for (int j = 0; j < fieldsCount; j++) {
					String f = msg.readString();
					msg.readInt();
					md5.add(f);
				}
				TemplateType tt = new TemplateType(md, fieldsCount);
				String hash = MD5Util.md5(md5);
				tt.setHash(hash);
				template.setTypeTemplate(idx, tt);
			}
		}
		ses.template.updateParentTypes();

		// Usage
		int usageCount = msg.readInt();
		for (int i = 0; i < usageCount; i++) {
			int idx = msg.readInt();
			int types = msg.readInt();
			UsageType[] tus = new UsageType[types];
			for (int j = 0; j < types; j++) {
				UsageType ut = createUsageType(msg);
				tus[j] = ut;
			}
			TemplateUsage tu = new TemplateUsage(tus);
			template.setUsageTemplate(idx, tu);
			master.addUsageTemplate(tu, template);
		}

		// Channels
		int channelCount = msg.readInt();
		for (int i = 0; i < channelCount; i++) {
			int idx = msg.readInt();
			String name = msg.readString();
			// Check if channel with this name exists
			DChannel channel = schema.getChannel(name);
			if (channel != null) {
				D3ELogger.info("Channel found: " + name);
				int msgCount = msg.readInt();
				TemplateChannel tc = new TemplateChannel(channel, msgCount);
				for (int j = 0; j < msgCount; j++) {
					String messageName = msg.readString();
					DMessage message = channel.getMessage(messageName);
					boolean paramNotFound = false;

					if (message == null) {
						D3ELogger.info("Message not found: " + messageName);
						// TODO: Add empty message and continue

						// TODO: Do we need this Hash?
						List<String> md5 = new ArrayList<>();
						md5.add(messageName);
						int argCount = msg.readInt();
						for (int k = 0; k < argCount; j++) {
							int type = msg.readInt();
							boolean collection = msg.readBoolean();
							DModel<?> dm = template.getType(type).getModel();
							md5.add(dm.getType());
						}
						tc.addMessage(j, message);
						continue;
					} else {
						D3ELogger.info("Message found: " + messageName);
						int paramCount = msg.readInt();
						for (int k = 0; k < paramCount; k++) {
							// Getting the parameter types of the method. These are needed for constructing
							// the hash
							int type = msg.readInt();
							boolean collection = msg.readBoolean();
							TemplateType tt = template.getType(type);
							DModel<?> paramType = tt.getModel();
							if (paramType == null) {
								// TODO: Collect the rest, add empty message
								D3ELogger.info("Param not found: " + k);
								paramNotFound = true;
								break;
							}

							if (!paramNotFound) {
								message.addParam(k, new DParam(type, collection));
							}
						}
					}
					tc.addMessage(j, paramNotFound ? null : message);
				}
				template.setChannelTemplate(idx, tc);
				master.addChannelTemplate(tc, template);
			} else {
				// Reject completely, or do what type is doing?
				List<String> md5 = new ArrayList<>();
				md5.add(name);
				int methodsCount = msg.readInt();
				for (int j = 0; j < methodsCount; j++) {
					String methodName = msg.readString();
					md5.add(methodName);
					int paramsCount = msg.readInt();
					for (int k = 0; k < paramsCount; k++) {
						int type = msg.readInt();
						String f = template.getType(type).getModel().getType();
						msg.readBoolean();
						md5.add(f);
					}
				}
				TemplateChannel tt = new TemplateChannel(channel, methodsCount);
				String hash = MD5Util.md5(md5);
				tt.setHash(hash);
				template.setChannelTemplate(idx, tt);
			}
		}

		computeTemplateMD5AndAddToManager(ses.template);
	}

	private UsageType createUsageType(RocketMessage msg) {
		int typeIdx = msg.readInt();
		int fieldsCount = msg.readInt();
		UsageType ut = new UsageType(typeIdx, fieldsCount);
		for (int j = 0; j < fieldsCount; j++) {
			int f = msg.readInt();
			int refs = msg.readInt();
			UsageType[] tus = new UsageType[refs];
			for (int k = 0; k < refs; k++) {
				UsageType ref = createUsageType(msg);
				tus[k] = ref;
			}
			UsageField uf = new UsageField(f, tus);
			ut.getFields()[j] = uf;
		}
		return ut;
	}

	public void sendDelete(ClientSession session, List<TypeAndId> objects) {
		D3ELogger.info("Send Deletes: ");
		RocketMessage msg = new RocketMessage(session);
		msg.writeInt(OBJECTS);
		msg.writeBoolean(false);
		Template template = session.template;
		int count = objects.size();
		msg.writeInt(count);
		for (var entry : objects) {
			msg.writeInt(template.toClientTypeIdx(entry.type));
			msg.writeLong(entry.id);
		}
		msg.flush();

	}
}
