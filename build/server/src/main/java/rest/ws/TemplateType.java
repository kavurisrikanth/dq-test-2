package rest.ws;

import gqltosql.schema.DField;
import gqltosql.schema.DModel;

public class TemplateType {

	private DModel<?> model;
	private DField<?, ?>[] fields;
	private int[] mapping;
	private String hash;
	private int parentServerCount;
	private int parentClientCount;
	private TemplateType parentType;

	public TemplateType(DModel<?> model, int length) {
		this.model = model;
		if (model != null) {
			this.fields = new DField<?, ?>[length];
			this.mapping = new int[model.getFields().length];
			this.parentServerCount = model.getParentCount();
		}
	}

	public void setParentClientCount(int parentClientCount) {
		this.parentClientCount = parentClientCount;
	}

	public int getParentClientCount() {
		return parentClientCount;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getHash() {
		return hash;
	}

	public void addField(int idx, DField<?, ?> field) {
		fields[idx] = field;
		mapping[field.getIndex() - parentServerCount] = idx;
	}

	public DModel<?> getModel() {
		return model;
	}

	public DField<?, ?>[] getFields() {
		return fields;
	}

	public DField<?, ?> getField(int idx) {
		if (idx < parentClientCount) {
			return parentType.getField(idx);
		}
		return fields[idx - parentClientCount];
	}

	public int toClientIdx(int serverIdx) {
		if (serverIdx < parentServerCount) {
			return parentType.toClientIdx(serverIdx);
		}
		return mapping[serverIdx - parentServerCount] + parentClientCount;
	}

	public void setParent(TemplateType parentType) {
		this.parentType = parentType;
		this.parentClientCount = parentType.fields.length;
	}

	public TemplateType getParentType() {
		return parentType;
	}

	@Override
	public String toString() {
		return model.toString();
	}
}
