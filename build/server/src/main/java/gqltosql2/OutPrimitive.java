package gqltosql2;

import java.math.BigInteger;

public class OutPrimitive implements IOutValue {
	private Object val;

	public OutPrimitive(Object val) {
		if(val instanceof BigInteger) {
			this.val = ((BigInteger) val).longValue();
		} else {
			this.val = val;
		}
	}

	public Object getVal() {
		return val;
	}
}
