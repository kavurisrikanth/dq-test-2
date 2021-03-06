package gqltosql2;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryReader {

	private List<QueryTypeReader> byType = new ArrayList<>();
	private int id;
	private boolean embedded;

	public QueryReader(int id, boolean embedded) {
		this.id = id;
		this.embedded = embedded;
	}

	public OutObject read(Object val, Map<Long, OutObject> byId) throws Exception {
		Object[] row;
		if (val.getClass().isArray()) {
			row = (Object[]) val;
		} else {
			row = new Object[] { val };
		}
		Long rowId = readId(row, this.id);
		if (rowId == null) {
			for (QueryTypeReader r : byType) {
				Long id = readId(row, r.getId());
				if (id != null) {
					rowId = id;
					break;
				}
			}
		}
		OutObject obj = null;
		if (rowId != null) {
			obj = byId.get(rowId);
		}
		if (obj == null) {
			obj = new OutObject();
		}
		if (rowId != null) {
			obj.setId(rowId);
		}
		readIntoObj(row, obj);
		if(embedded) {
			if(obj.length() == 0) {
				return null;
			}
		} else {
			if(rowId == null && !isCollection()) {
				return null;
			}
		}
		return obj;
	}
	
	private boolean isCollection() {
		for (QueryTypeReader tr : byType) {
			if (tr.getType() == -1) {
				return true;
			}
		}
		return false;
	}

	private void readIntoObj(Object[] row, OutObject obj) throws Exception {
		for (QueryTypeReader tr : byType) {
			tr.read(row, obj);
		}
		OutObject dup = obj.getDuplicate();
		if (dup != null) {
			readIntoObj(row, dup);
		}
	}

	private Long readId(Object[] row, int id) {
		if (id == -1) {
			return null;
		}
		if (!(row[id] instanceof BigInteger)) {
			return null;
		}
		BigInteger bid = ((BigInteger) row[id]);
		if (bid == null) {
			return null;
		}
		return ((BigInteger) row[id]).longValue();
	}

	public QueryTypeReader getTypeReader(int type) {
		for (QueryTypeReader tr : byType) {
			if (tr.getType() == type) {
				return tr;
			}
		}
		QueryTypeReader tr = new QueryTypeReader(type);
		byType.add(tr);
		return tr;
	}
}
