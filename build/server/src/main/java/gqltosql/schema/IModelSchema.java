package gqltosql.schema;

import java.util.List;

public interface IModelSchema {

	public List<DModel<?>> getAllTypes();
	
	public DModel<?> getType(String type);
	
	public DModel<?> getType(int index);

  public List<DChannel> getAllChannels();
  
  public DChannel getChannel(String name);
}
