package models;

import d3e.core.CloneContext;
import d3e.core.SchemaConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import org.apache.solr.client.solrj.beans.Field;
import org.hibernate.annotations.ColumnDefault;
import store.DatabaseObject;
import store.ICloneable;

@Entity
public class InvoiceItem extends DatabaseObject {
  public static final int _NAME = 0;
  public static final int _OTHERNAMES = 1;
  public static final int _COST = 2;
  @Field private String name;
  @Field @ElementCollection private List<String> otherNames = new ArrayList<>();

  @Field
  @ColumnDefault("0.0")
  private double cost = 0.0d;

  @Field @ManyToOne private Invoice masterInvoice;
  private transient InvoiceItem old;

  @Override
  public int _typeIdx() {
    return SchemaConstants.InvoiceItem;
  }

  @Override
  public String _type() {
    return "InvoiceItem";
  }

  @Override
  public int _fieldsCount() {
    return 3;
  }

  public DatabaseObject _masterObject() {
    if (masterInvoice != null) {
      return masterInvoice;
    }
    return null;
  }

  public void addToOtherNames(String val, long index) {
    collFieldChanged(_OTHERNAMES, this.otherNames);
    if (index == -1) {
      this.otherNames.add(val);
    } else {
      this.otherNames.add(((int) index), val);
    }
  }

  public void removeFromOtherNames(String val) {
    collFieldChanged(_OTHERNAMES, this.otherNames);
    this.otherNames.remove(val);
  }

  public void updateMasters(Consumer<DatabaseObject> visitor) {
    super.updateMasters(visitor);
  }

  public void updateFlat(DatabaseObject obj) {
    super.updateFlat(obj);
    if (masterInvoice != null) {
      masterInvoice.updateFlat(obj);
    }
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

  public List<String> getOtherNames() {
    return this.otherNames;
  }

  public void setOtherNames(List<String> otherNames) {
    if (Objects.equals(this.otherNames, otherNames)) {
      return;
    }
    collFieldChanged(_OTHERNAMES, this.otherNames);
    this.otherNames.clear();
    this.otherNames.addAll(otherNames);
  }

  public double getCost() {
    return this.cost;
  }

  public void setCost(double cost) {
    if (Objects.equals(this.cost, cost)) {
      return;
    }
    fieldChanged(_COST, this.cost);
    this.cost = cost;
  }

  public Invoice getMasterInvoice() {
    return this.masterInvoice;
  }

  public void setMasterInvoice(Invoice masterInvoice) {
    this.masterInvoice = masterInvoice;
  }

  public InvoiceItem getOld() {
    return this.old;
  }

  public void setOld(DatabaseObject old) {
    this.old = ((InvoiceItem) old);
  }

  public String displayName() {
    return "InvoiceItem";
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof InvoiceItem && super.equals(a);
  }

  public InvoiceItem deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    InvoiceItem _obj = ((InvoiceItem) dbObj);
    _obj.setName(name);
    _obj.setOtherNames(otherNames);
    _obj.setCost(cost);
  }

  public InvoiceItem cloneInstance(InvoiceItem cloneObj) {
    if (cloneObj == null) {
      cloneObj = new InvoiceItem();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setName(this.getName());
    cloneObj.setOtherNames(new ArrayList<>(this.getOtherNames()));
    cloneObj.setCost(this.getCost());
    return cloneObj;
  }

  public InvoiceItem createNewInstance() {
    return new InvoiceItem();
  }

  public boolean needOldObject() {
    return true;
  }

  @Override
  public boolean _isEntity() {
    return true;
  }
}
