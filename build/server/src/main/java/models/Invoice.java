package models;

import d3e.core.CloneContext;
import d3e.core.SchemaConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.solr.core.mapping.ChildDocument;
import org.springframework.data.solr.core.mapping.SolrDocument;
import store.Database;
import store.DatabaseObject;
import store.ICloneable;

@SolrDocument(collection = "Invoice")
@Entity
public class Invoice extends CreatableObject {
  public static final int _NAME = 0;
  public static final int _ITEMS = 1;
  @Field private String name;

  @Field
  @ChildDocument
  @OrderColumn
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  private List<InvoiceItem> items = new ArrayList<>();

  private transient Invoice old;

  @Override
  public int _typeIdx() {
    return SchemaConstants.Invoice;
  }

  @Override
  public String _type() {
    return "Invoice";
  }

  @Override
  public int _fieldsCount() {
    return 2;
  }

  public void addToItems(InvoiceItem val, long index) {
    collFieldChanged(_ITEMS, this.items);
    val.setMasterInvoice(this);
    if (index == -1) {
      this.items.add(val);
    } else {
      this.items.add(((int) index), val);
    }
  }

  public void removeFromItems(InvoiceItem val) {
    collFieldChanged(_ITEMS, this.items);
    this.items.remove(val);
  }

  public void updateMasters(Consumer<DatabaseObject> visitor) {
    super.updateMasters(visitor);
    for (InvoiceItem obj : this.getItems()) {
      visitor.accept(obj);
      obj.setMasterInvoice(this);
      obj.updateMasters(visitor);
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

  public List<InvoiceItem> getItems() {
    return this.items;
  }

  public void setItems(List<InvoiceItem> items) {
    if (Objects.equals(this.items, items)) {
      return;
    }
    collFieldChanged(_ITEMS, this.items);
    this.items.clear();
    this.items.addAll(items);
  }

  public Invoice getOld() {
    return this.old;
  }

  public void setOld(DatabaseObject old) {
    this.old = ((Invoice) old);
  }

  public void recordOld(CloneContext ctx) {
    super.recordOld(ctx);
    this.getItems().forEach((one) -> one.recordOld(ctx));
  }

  public String displayName() {
    return "Invoice";
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof Invoice && super.equals(a);
  }

  public Invoice deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void collectChildValues(CloneContext ctx) {
    super.collectChildValues(ctx);
    ctx.collectChilds(items);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    Invoice _obj = ((Invoice) dbObj);
    _obj.setName(name);
    ctx.cloneChildList(items, (v) -> _obj.setItems(v));
  }

  public Invoice cloneInstance(Invoice cloneObj) {
    if (cloneObj == null) {
      cloneObj = new Invoice();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setName(this.getName());
    cloneObj.setItems(
        this.getItems().stream()
            .map((InvoiceItem colObj) -> colObj.cloneInstance(null))
            .collect(Collectors.toList()));
    return cloneObj;
  }

  public Invoice createNewInstance() {
    return new Invoice();
  }

  public void collectCreatableReferences(List<Object> _refs) {
    super.collectCreatableReferences(_refs);
    Database.collectCollctionCreatableReferences(_refs, this.items);
  }

  @Override
  public boolean _isEntity() {
    return true;
  }
}
