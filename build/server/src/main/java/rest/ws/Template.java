package rest.ws;

import d3e.core.ChannelConstants;
import d3e.core.SchemaConstants;
import gqltosql.schema.DChannel;
import gqltosql.schema.DModel;

public class Template {

	private String hash;
	private TemplateType[] types;
	private int[] mapping;
	private int[] channelMapping;

	private TemplateUsage[] usages;
	private TemplateChannel[] channelInfo;

	public Template(int types, int usages, int channels) {
		this.usages = new TemplateUsage[usages];
		this.types = new TemplateType[types];
		this.mapping = new int[SchemaConstants._TOTAL_COUNT];
		this.channelInfo = new TemplateChannel[channels];
		this.channelMapping = new int[ChannelConstants._CHANNEL_COUNT];
	}

	public void updateParentTypes() {
		for (TemplateType tt : types) {
			if (tt.getModel() != null) {
				DModel<?> parent = tt.getModel().getParent();
				if (parent != null) {
					int parentClientIdx = toClientTypeIdx(parent.getIndex());
					TemplateType parentType = getType(parentClientIdx);
					if (parentType != null) {
						tt.setParent(parentType);
					}
				}
			}
		}
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getHash() {
		return hash;
	}

	public TemplateType[] getTypes() {
		return types;
	}

	public TemplateUsage[] getUsages() {
		return usages;
	}

	public TemplateChannel[] getChannels() {
		return channelInfo;
	}

	public TemplateType getType(int idx) {
		return types[idx];
	}

	public TemplateUsage getUsageType(int idx) {
		return usages[idx];
	}

	public void setTypeTemplate(int idx, TemplateType tt) {
		types[idx] = tt;
		if (tt.getModel() != null) {
			mapping[tt.getModel().getIndex()] = idx;
		}
	}

	public void setUsageTemplate(int idx, TemplateUsage ut) {
		usages[idx] = ut;
	}

	public int toClientTypeIdx(int serverIdx) {
		return mapping[serverIdx];
	}

	public void setChannelTemplate(int i, TemplateChannel tc) {
		channelInfo[i] = tc;
		DChannel ch = tc.getChannel();
		if (ch != null) {
			channelMapping[ch.getIndex()] = i;
		}
	}

	public int getClientChannelIndex(int serverIdx) {
		return channelMapping[serverIdx];
	}

	public TemplateChannel getChannel(int chIdx) {
		TemplateChannel channel = channelInfo[chIdx];
		return channel;
	}
}
