package rest.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import d3e.core.ListExt;
import d3e.core.MD5Util;
import gqltosql.schema.DChannel;
import gqltosql.schema.DField;
import gqltosql.schema.DMessage;
import gqltosql.schema.DModel;
import gqltosql.schema.DModelType;
import gqltosql.schema.DParam;
import gqltosql.schema.IModelSchema;

@Service
public class MasterTemplate {

	@Autowired
	private IModelSchema schema;

	private Map<String, TemplateType> typesByHash = new HashMap<>();
	private Map<String, TemplateUsage> usageByHash = new HashMap<>();
	private Map<String, TemplateChannel> channelsByHash = new HashMap<>();

	@PostConstruct
	public void init() {
		List<DModel<?>> allTypes = schema.getAllTypes();
		allTypes.forEach(t -> addTemplateType(t));
		
		List<DChannel> allChannels = schema.getAllChannels();
		allChannels.forEach(c -> addTemplateChannel(c));
	}

  	public String getByType(String type) {
		Set<Entry<String, TemplateType>> entrySet = typesByHash.entrySet();
		for (Entry<String, TemplateType> e : entrySet) {
			if (e.getValue().getModel().getType().equals(type)) {
				return e.getKey();
			}
		}
		return null;
	}
  
	private void addTemplateChannel(DChannel c) {
		List<String> md5 = new ArrayList<>();
		md5.add(c.getName());
		List<DMessage> allFields = ListExt.from(c.getMessages(), false);
		TemplateChannel tt = new TemplateChannel(c, allFields.size());
		int i = 0;
		for (DMessage f : allFields) {
			tt.addMessage(i++, f);
			md5.add(f.getName());
			for (DParam p : f.getParams()) {
			  	// TODO: Collection?
				DModel<?> dm = schema.getType(p.getType());
				md5.add(dm.getType());
			}
		}
		String hash = MD5Util.md5(md5);
		tt.setHash(hash);
		channelsByHash.put(hash, tt);
	}

	private void addTemplateType(DModel<?> md) {
		List<String> md5 = new ArrayList<>();
		md5.add(md.getType());
		List<DField<?, ?>> allFields = ListExt.from(md.getFields(), false);
		if (md.getModelType() != DModelType.ENUM) {
			allFields.sort((a, b) -> a.getName().compareTo(b.getName()));
		}
		TemplateType tt = new TemplateType(md, allFields.size());
		int i = 0;
		for (DField<?, ?> f : allFields) {
			tt.addField(i++, f);
			md5.add(f.getName());
		}
		String hash = MD5Util.md5(md5);
		tt.setHash(hash);
		typesByHash.put(hash, tt);
	}

	public TemplateType getTemplateType(String typeHash) {
		return typesByHash.get(typeHash);
	}

	public TemplateUsage getUsageTemplate(String usageHash) {
		return usageByHash.get(usageHash);
	}
	
	public TemplateChannel getChannelTemplate(String channelHash) {
		return channelsByHash.get(channelHash);
	}

	public void addTypeTemplate(TemplateType tt) {
		List<String> md5 = new ArrayList<>();
		md5.add(tt.getModel().getType());
		for (DField<?, ?> f : tt.getFields()) {
			md5.add(f.getName());
		}
		String hash = MD5Util.md5(md5);
		tt.setHash(hash);
		typesByHash.put(hash, tt);
	}

	public void addUsageTemplate(TemplateUsage tu, Template tml) {
		List<String> md5 = new ArrayList<>();
		UsageType[] uts = tu.getTypes();
		for (UsageType ut : uts) {
			addUsageMD5Strings(md5, ut, tml);
		}
		String hash = MD5Util.md5(md5);
		tu.setHash(hash);
		usageByHash.put(hash, tu);
	}
	
	public void addChannelTemplate(TemplateChannel tc, Template tml) {
		/*
			* channel name
			* methods
			*   name
			*   param type
		*/
		List<String> md5 = new ArrayList<>();
		DChannel dc = tc.getChannel();
		md5.add(dc.getName());
		for (DMessage one : tc.getMessages()) {
			md5.add(one.getName());
			for (DParam type : one.getParams()) {
			  // TODO: Collection?
			  DModel<?> dm = tml.getType(type.getType()).getModel();
				md5.add(dm.getType());
			}
		}
		String hash = MD5Util.md5(md5);
		tc.setHash(hash);
		channelsByHash.put(hash, tc);
	}

	private void addUsageMD5Strings(List<String> md5, UsageType ut, Template tml) {
		TemplateType type = tml.getType(ut.getType());
		md5.add(type.getModel().getType());
		for (UsageField f : ut.getFields()) {
			DField<?, ?> df = type.getField(f.getField());
			md5.add(df.getName());
			UsageType[] types = f.getTypes();
			for (UsageType utt : types) {
				addUsageMD5Strings(md5, utt, tml);
			}
		}
	}
}
