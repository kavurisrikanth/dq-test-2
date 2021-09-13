package store;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DBObject {

	protected transient long localId;
	private transient BitSet _changes;
	private transient Map<Integer, Object> _oldValues = new HashMap<>();

	public DBObject() {
		this._changes = new BitSet(_fieldsCount());
	}

	public void setLocalId(long localId) {
		this.localId = localId;
	}

	public long getLocalId() {
		return localId;
	}

	protected abstract int _fieldsCount();

	public long getId() {
	  return 0;
	}

	public void setId(long id) {}
	
	public abstract int _typeIdx();

	public abstract String _type();

	public BitSet _changes() {
		return _changes;
	}

	public void fieldChanged(int field, Object oldValue) {
		this._changes.set(field);
		onPropertySet();
	}
	
	public void collFieldChanged(int field, Object oldValue) {
		this._changes.set(field);
		ListChanges lc = (ListChanges) _oldValues.get(field);
		if (lc == null) {
			lc = new ListChanges(new ArrayList((List) oldValue));
			_oldValues.put(field, lc);
		}
		onPropertySet();
	}
	
	public Object _oldValue(int field) {
		return _oldValues.get(field);
	}

	public void _clearChanges() {
		_oldValues.clear();
		_changes.clear();
	}
	
	public void _updateChanges(DBObject other) {
		this._changes = other._changes;
		this._oldValues = other._oldValues;
	}
	
	protected void onPropertySet() {

	}
}
