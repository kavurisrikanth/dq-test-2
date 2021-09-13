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
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.validation.constraints.NotNull;
import org.apache.solr.client.solrj.beans.Field;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.solr.core.mapping.ChildDocument;
import org.springframework.data.solr.core.mapping.SolrDocument;
import store.Database;
import store.DatabaseObject;
import store.ICloneable;

@SolrDocument(collection = "Invoice")
@Entity
public class Invoice extends CreatableObject {
  public static final int _TOTALAMOUNT = 0;
  public static final int _MOSTEXPENSIVEITEM = 1;
  public static final int _ITEMS = 2;
  public static final int _CUSTOMER = 3;

  @Field
  @ColumnDefault("0.0")
  private double totalAmount = 0.0d;

  @Field
  @ManyToOne(fetch = FetchType.LAZY)
  private InvoiceItem mostExpensiveItem;

  @Field
  @ChildDocument
  @OrderColumn
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  private List<InvoiceItem> items = new ArrayList<>();

  @Field
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  private Customer customer;

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
    return 4;
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

  public double getTotalAmount() {
    return this.totalAmount;
  }

  public void setTotalAmount(double totalAmount) {
    if (Objects.equals(this.totalAmount, totalAmount)) {
      return;
    }
    fieldChanged(_TOTALAMOUNT, this.totalAmount);
    this.totalAmount = totalAmount;
  }

  public InvoiceItem getMostExpensiveItem() {
    return this.mostExpensiveItem;
  }

  public void setMostExpensiveItem(InvoiceItem mostExpensiveItem) {
    if (Objects.equals(this.mostExpensiveItem, mostExpensiveItem)) {
      return;
    }
    fieldChanged(_MOSTEXPENSIVEITEM, this.mostExpensiveItem);
    this.mostExpensiveItem = mostExpensiveItem;
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

  public Customer getCustomer() {
    return this.customer;
  }

  public void setCustomer(Customer customer) {
    if (Objects.equals(this.customer, customer)) {
      return;
    }
    fieldChanged(_CUSTOMER, this.customer);
    if (!(isOld) && this.customer != null) {
      this.customer.removeFromInvoices(this);
    }
    this.customer = customer;
    if (!(isOld) && customer != null && !(customer.getInvoices().contains(this))) {
      customer.addToInvoices(this, -1);
    }
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
    _obj.setTotalAmount(totalAmount);
    _obj.setMostExpensiveItem(ctx.cloneRef(mostExpensiveItem));
    ctx.cloneChildList(items, (v) -> _obj.setItems(v));
    _obj.setCustomer(customer);
  }

  public Invoice cloneInstance(Invoice cloneObj) {
    if (cloneObj == null) {
      cloneObj = new Invoice();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setTotalAmount(this.getTotalAmount());
    cloneObj.setMostExpensiveItem(this.getMostExpensiveItem());
    cloneObj.setItems(
        this.getItems().stream()
            .map((InvoiceItem colObj) -> colObj.cloneInstance(null))
            .collect(Collectors.toList()));
    cloneObj.setCustomer(this.getCustomer());
    return cloneObj;
  }

  public Invoice createNewInstance() {
    return new Invoice();
  }

  public boolean needOldObject() {
    return true;
  }

  public void collectCreatableReferences(List<Object> _refs) {
    super.collectCreatableReferences(_refs);
    _refs.add(this.customer);
    Database.collectCollctionCreatableReferences(_refs, this.items);
  }

  @Override
  public boolean _isEntity() {
    return true;
  }
}
