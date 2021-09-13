package rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import d3e.core.DFile;
import d3e.core.SchemaConstants;
import gqltosql.schema.DChannel;
import gqltosql.schema.DMessage;
import gqltosql.schema.DModel;
import gqltosql.schema.DModelType;
import gqltosql.schema.DParam;
import gqltosql.schema.FieldPrimitiveType;
import gqltosql.schema.IModelSchema;

public abstract class AbstractModelSchema implements IModelSchema{
	protected Map<String, DModel<?>> allTypes = new HashMap<>();
	private DModel<?>[] all;
	private Map<String, DChannel> allChannels = new HashMap<>();
	private DChannel[] channels;

	@PostConstruct
	public void init() {
		createAllPrimitives();
		createAllEnums();
		createAllTables();
		addFields();
		computeHierarchyTypes();
		recordAllChannels();
	}
	  
	private void computeHierarchyTypes() {
		all = new DModel<?>[SchemaConstants._TOTAL_COUNT];
		for(DModel m : allTypes.values()) {
			all[m.getIndex()] = m;
		}
		for(DModel m : all) {
			if(m == null) {
				continue;
			}
			List<Integer> types = new ArrayList<Integer>();
			DModel temp = m;
			while(temp != null) {
				types.add(0, temp.getIndex());
				temp = temp.getParent();
			}
			addSubTypes(m, types);
			int[] allTypes = new int[types.size()];
			int index = 0;
			while(index < allTypes.length) {
				allTypes[index] =types.get(index);
				index++;
			}
			m.setAllTypes(allTypes);
		}
	}
	  
	private void addSubTypes(DModel model, List<Integer> types) {
		for(DModel m : all) {
			if(m != null && m.getParent() == model) {
				types.add(m.getIndex());
				addSubTypes(m, types);
			}
		}
	}

	protected abstract void addFields();

	protected abstract void createAllTables();

	protected abstract void createAllEnums();
	
	protected void recordAllChannels() {}

	public List<DModel<?>> getAllTypes() {
	    return new ArrayList<>(allTypes.values());
	}
	
	public DModel<?> getType(String type) {
	    return allTypes.get(type);
	}

	public DModel<?> getType(int index) {
	    return all[index];
	}

	public <T> DModel<T> getType2(String type) {
	    return ((DModel<T>) allTypes.get(type));
	}

	protected void addTable(DModel<?> model) {
	    all = new DModel<?>[SchemaConstants._TOTAL_COUNT];
	    allTypes.put(model.getType(), model);
	}

	protected void addEnum(Class<? extends Enum<?>> enm, int index) {
	    Enum<?>[] constants = enm.getEnumConstants();
	    DModel<?> dm =
	        new DModel<>(enm.getSimpleName(), index, constants.length, 0, null, DModelType.ENUM);
	    int idx = 0;
	    for (Enum<?> e : constants) {
	      dm.addPrimitive(e.name(), idx++, null, FieldPrimitiveType.Enum, (s) -> e, null);
	    }
	    addTable(dm);
	}

	private void createAllPrimitives() {
	    addTable(new DModel<>("String", SchemaConstants.String, 0, 0, null, DModelType.PRIMITIVE));
	    addTable(new DModel<>("Integer", SchemaConstants.Integer, 0, 0, null, DModelType.PRIMITIVE));
	    addTable(new DModel<>("Double", SchemaConstants.Double, 0, 0, null, DModelType.PRIMITIVE));
	    addTable(new DModel<>("Boolean", SchemaConstants.Boolean, 0, 0, null, DModelType.PRIMITIVE));
	    addTable(new DModel<>("Date", SchemaConstants.Date, 0, 0, null, DModelType.PRIMITIVE));
	    addTable(new DModel<>("DateTime", SchemaConstants.DateTime, 0, 0, null, DModelType.PRIMITIVE));
	    addTable(new DModel<>("Time", SchemaConstants.Time, 0, 0, null, DModelType.PRIMITIVE));
	    addTable(new DModel<>("Duration", SchemaConstants.Duration, 0, 0, null, DModelType.PRIMITIVE));
	}
	protected void addDFileFields() {
	    DModel<DFile> m = getType2("DFile");
	    m.addPrimitive(
	        "id", 0, "_id", FieldPrimitiveType.String, (s) -> s.getId(), (s, v) -> s.setId(v));
	    m.addPrimitive(
	        "name", 1, "_name", FieldPrimitiveType.String, (s) -> s.getName(), (s, v) -> s.setName(v));
	    m.addPrimitive(
	        "size", 2, "_size", FieldPrimitiveType.Integer, (s) -> s.getSize(), (s, v) -> s.setSize(v));
	}
	  
	protected void recordNumChannels(int num) {
	    this.channels = new DChannel[num];
	}
	  
	protected DChannel addChannel(String name, int index, int numMsgs) {
	    DChannel channel = new DChannel(name, index, numMsgs);
	    channels[index] = channel;
	    allChannels.put(name, channel);
	    return channel;
	}
	  
	protected void populateChannel(DChannel channel, int msgIndex, String msgName, DParam... params) {
	    channel.addMessage(msgIndex, new DMessage(msgName, msgIndex, params));
	}
	  
	@Override
	public List<DChannel> getAllChannels() {
	    return Arrays.asList(this.channels);
	}
	  
	@Override
	public DChannel getChannel(String name) {
	    return this.allChannels.get(name);
	}
}
