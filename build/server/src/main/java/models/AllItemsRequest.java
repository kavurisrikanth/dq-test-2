package models;

import d3e.core.CloneContext;
import d3e.core.SchemaConstants;
import java.util.Objects;
import java.util.function.Consumer;
import org.apache.solr.client.solrj.beans.Field;
import store.DatabaseObject;
import store.ICloneable;

public class AllItemsRequest extends CreatableObject {
  public static final int _NAME = 0;
  @Field private String name;
  private transient AllItemsRequest old;

  @Override
  public int _typeIdx() {
    return SchemaConstants.AllItemsRequest;
  }

  @Override
  public String _type() {
    return "AllItemsRequest";
  }

  @Override
  public int _fieldsCount() {
    return 1;
  }

  public void updateMasters(Consumer<DatabaseObject> visitor) {
    super.updateMasters(visitor);
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    if (Objects.equals(this.name, name)) {
      return;
    }
    fieldChanged(_NAME, this.name);
    this.name = name;
  }

  public AllItemsRequest getOld() {
    return this.old;
  }

  public void setOld(DatabaseObject old) {
    this.old = ((AllItemsRequest) old);
  }

  public String displayName() {
    return "AllItemsRequest";
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof AllItemsRequest && super.equals(a);
  }

  public AllItemsRequest deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    AllItemsRequest _obj = ((AllItemsRequest) dbObj);
    _obj.setName(name);
  }

  public AllItemsRequest cloneInstance(AllItemsRequest cloneObj) {
    if (cloneObj == null) {
      cloneObj = new AllItemsRequest();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setName(this.getName());
    return cloneObj;
  }

  public boolean transientModel() {
    return true;
  }

  public AllItemsRequest createNewInstance() {
    return new AllItemsRequest();
  }
}
