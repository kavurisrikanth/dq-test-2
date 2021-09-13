package models;

import d3e.core.CloneContext;
import d3e.core.SchemaConstants;
import java.util.Objects;
import java.util.function.Consumer;
import org.apache.solr.client.solrj.beans.Field;
import store.DatabaseObject;
import store.ICloneable;

public class AllCustomersWithLargeInvoicesRequest extends CreatableObject {
  public static final int _ITEM = 0;
  @Field private InvoiceItem item;
  private transient AllCustomersWithLargeInvoicesRequest old;

  @Override
  public int _typeIdx() {
    return SchemaConstants.AllCustomersWithLargeInvoicesRequest;
  }

  @Override
  public String _type() {
    return "AllCustomersWithLargeInvoicesRequest";
  }

  @Override
  public int _fieldsCount() {
    return 1;
  }

  public void updateMasters(Consumer<DatabaseObject> visitor) {
    super.updateMasters(visitor);
  }

  public InvoiceItem getItem() {
    return this.item;
  }

  public void setItem(InvoiceItem item) {
    if (Objects.equals(this.item, item)) {
      return;
    }
    fieldChanged(_ITEM, this.item);
    this.item = item;
  }

  public AllCustomersWithLargeInvoicesRequest getOld() {
    return this.old;
  }

  public void setOld(DatabaseObject old) {
    this.old = ((AllCustomersWithLargeInvoicesRequest) old);
  }

  public String displayName() {
    return "AllCustomersWithLargeInvoicesRequest";
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof AllCustomersWithLargeInvoicesRequest && super.equals(a);
  }

  public AllCustomersWithLargeInvoicesRequest deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    AllCustomersWithLargeInvoicesRequest _obj = ((AllCustomersWithLargeInvoicesRequest) dbObj);
    _obj.setItem(ctx.cloneRef(item));
  }

  public AllCustomersWithLargeInvoicesRequest cloneInstance(
      AllCustomersWithLargeInvoicesRequest cloneObj) {
    if (cloneObj == null) {
      cloneObj = new AllCustomersWithLargeInvoicesRequest();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setItem(this.getItem());
    return cloneObj;
  }

  public boolean transientModel() {
    return true;
  }

  public AllCustomersWithLargeInvoicesRequest createNewInstance() {
    return new AllCustomersWithLargeInvoicesRequest();
  }
}
