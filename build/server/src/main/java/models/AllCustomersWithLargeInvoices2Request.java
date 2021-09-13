package models;

import d3e.core.CloneContext;
import d3e.core.SchemaConstants;
import java.util.Objects;
import java.util.function.Consumer;
import org.apache.solr.client.solrj.beans.Field;
import store.DatabaseObject;
import store.ICloneable;

public class AllCustomersWithLargeInvoices2Request extends CreatableObject {
  public static final int _ITEM = 0;
  @Field private double item = 0.0d;
  private transient AllCustomersWithLargeInvoices2Request old;

  @Override
  public int _typeIdx() {
    return SchemaConstants.AllCustomersWithLargeInvoices2Request;
  }

  @Override
  public String _type() {
    return "AllCustomersWithLargeInvoices2Request";
  }

  @Override
  public int _fieldsCount() {
    return 1;
  }

  public void updateMasters(Consumer<DatabaseObject> visitor) {
    super.updateMasters(visitor);
  }

  public double getItem() {
    return this.item;
  }

  public void setItem(double item) {
    if (Objects.equals(this.item, item)) {
      return;
    }
    fieldChanged(_ITEM, this.item);
    this.item = item;
  }

  public AllCustomersWithLargeInvoices2Request getOld() {
    return this.old;
  }

  public void setOld(DatabaseObject old) {
    this.old = ((AllCustomersWithLargeInvoices2Request) old);
  }

  public String displayName() {
    return "AllCustomersWithLargeInvoices2Request";
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof AllCustomersWithLargeInvoices2Request && super.equals(a);
  }

  public AllCustomersWithLargeInvoices2Request deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    AllCustomersWithLargeInvoices2Request _obj = ((AllCustomersWithLargeInvoices2Request) dbObj);
    _obj.setItem(item);
  }

  public AllCustomersWithLargeInvoices2Request cloneInstance(
      AllCustomersWithLargeInvoices2Request cloneObj) {
    if (cloneObj == null) {
      cloneObj = new AllCustomersWithLargeInvoices2Request();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setItem(this.getItem());
    return cloneObj;
  }

  public boolean transientModel() {
    return true;
  }

  public AllCustomersWithLargeInvoices2Request createNewInstance() {
    return new AllCustomersWithLargeInvoices2Request();
  }
}
