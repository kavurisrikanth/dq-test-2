package rest.ws;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import d3e.core.DFile;
import gqltosql.schema.DField;
import gqltosql.schema.DModel;
import gqltosql2.IOutValue;
import gqltosql2.OutObject;
import gqltosql2.OutPrimitive;

public class RocketOutObjectFetcher {

	private RocketMessage msg;
	private Template template;

	public RocketOutObjectFetcher(Template template, RocketMessage msg) {
		this.template = template;
		this.msg = msg;
	}

	public void fetch(TemplateUsage usage, Object value) {
		fetchValue(usage, value);
	}

	private void fetchValue(TemplateUsage usage, Object value) {
		if (value == null) {
			DModel<?> ref = usage.getField().getField().getReference();
			if (ref != null && ref.isEmbedded()) {
				msg.writeInt(template.toClientTypeIdx(ref.getIndex()));
				msg.writeNull();
			} else {
				msg.writeNull();
			}
		} else if (value instanceof Collection) {
			List<?> coll = new ArrayList<>((Collection<?>) value);
			// D3ELogger.info("w coll: " + coll.size());
			msg.writeInt(coll.size());
			coll.forEach(v -> fetchValue(usage, v));
		} else if (value instanceof OutObject) {
			OutObject db = (OutObject) value;
			// D3ELogger.info("w ref: " + db.getTypes());
			int clientType = db.getType();
			int typeIdx = template.toClientTypeIdx(clientType);
			msg.writeInt(typeIdx);
			msg.writeLong(db.getId());
			for (UsageType ut : usage.getTypes()) {
				TemplateType tt = template.getType(ut.getType());
				fetchReferenceInternal(tt, ut, db);
			}
			msg.writeInt(-1);
		} else {
			throw new RuntimeException("Unsupported type. " + value.getClass());
		}
	}

	private void fetchPrimitive(OutPrimitive value, DField df) {
		Object val = value.getVal();
		msg.writePrimitiveField(val, df, template);
	}

	private void fetchDFile(DFile value) {
		if (value == null) {
			msg.writeNull();
		}
		msg.writeString(value.getId());
		msg.writeString(value.getName());
		msg.writeLong(value.getSize());
	}

	private void fetchReferenceInternal(TemplateType type, UsageType usage, OutObject value) {
		for (UsageField f : usage.getFields()) {
			msg.writeInt(f.getField());
			DField df = type.getField(f.getField());
			// D3ELogger.info("w field: " + df.getName());
			IOutValue val = value.get(df.getName());
			fetchFieldValue(f, val, df);
		}
	}

	private void fetchFieldValue(UsageField field, IOutValue value, DField df) {
		if (value == null) {
			DModel<?> ref = df.getReference();
			if (ref != null && ref.isEmbedded()) {
				msg.writeInt(template.toClientTypeIdx(ref.getIndex()));
				msg.writeNull();
			} else {
				msg.writeNull();
			}
		} else if (value instanceof OutPrimitive) {
			UsageType[] types = field.getTypes();
			fetchPrimitive((OutPrimitive) value, df);
		} else if (value instanceof Collection) {
			List<?> coll = new ArrayList<>((Collection<?>) value);
			// D3ELogger.info("w coll: " + coll.size());
			msg.writeInt(coll.size());
			coll.forEach(v -> fetchFieldValue(field, (IOutValue) v, df));
		} else if (value instanceof OutObject) {
			OutObject db = (OutObject) value;
			// D3ELogger.info("w ref: " + db.getTypes());
			int typeIdx = template.toClientTypeIdx(db.getType());
			msg.writeInt(typeIdx);
			DModel<?> ref = df.getReference();
			if (!ref.isEmbedded()) {
				msg.writeLong(db.getId());
			}
			UsageType[] types = field.getTypes();
			for (UsageType type : types) {
				TemplateType tt = template.getType(type.getType());
				fetchReferenceInternal(tt, type, db);
			}
			msg.writeInt(-1);
		} else {
			throw new RuntimeException("Unsupported type. " + value.getClass());
		}
	}
}
