package gqltosql2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

public class OutObject implements IOutValue {
	private long id;
	private Set<Integer> types = new HashSet<>();
	private Map<String, IOutValue> fields = new HashMap<>();
	private OutObject dup;

	public long getId() {
		return id;
	}

	public int getType() {
		Long index = getLong("__typeindex");
		if(index == null) {
			for(Integer i : types) {
				if(i != -1) {
					return i;
				}
			}
		}
		return index.intValue();
	}

	public void addType(int type) {
		types.add(type);
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int length() {
		return fields.size();
	}

	public void add(String field, IOutValue value) {
		if(field.equals("__typeindex") && value instanceof OutPrimitive) {
			OutPrimitive pri = (OutPrimitive) value;
			if(pri.getVal().toString().equals("-1") && types.contains(225)){
				System.out.println();
			}
		}
		fields.put(field, value);
	}

	public Map<String, IOutValue> getFields() {
		return fields;
	}

	public String getString(String field) {
		IOutValue val = get(field);
		if (val == null) {
			return null;
		}
		OutPrimitive pri = (OutPrimitive) val;
		return (String) pri.getVal();
	}

	public Long getLong(String field) {
		if (field.equals("id")) {
			return id;
		}
		IOutValue val = get(field);
		if (val == null) {
			return null;
		}
		OutPrimitive pri = (OutPrimitive) val;
		Object object = pri.getVal();
		if(object instanceof Integer) {
			return (long)((int)object);
		}
		return (Long) pri.getVal();
	}

	public OutObject getObject(String field) {
		IOutValue val = get(field);
		return (OutObject) val;
	}

	public void remove(String field) {
		fields.remove(field);
	}

	public boolean isOfType(int type) {
		return types.contains(type);
	}

	public void duplicate(OutObject dup) {
		if (this.dup == dup) {
			return;
		}
		if (this.dup != null) {
			this.dup.duplicate(dup);
		} else {
			this.dup = dup;
		}
	}

	public OutObject getDuplicate() {
		return dup;
	}

	public void addCollectionField(String field, OutObjectList val) throws JSONException {
		add(field, val);
		if (dup != null) {
			dup.addCollectionField(field, val);
		}
	}

	public OutPrimitive getPrimitive(String field) {
		return (OutPrimitive) fields.get(field);
	}

	public boolean has(String field) {
		return fields.containsKey(field);
	}

	public IOutValue get(String field) {
		return fields.get(field);
	}
}
