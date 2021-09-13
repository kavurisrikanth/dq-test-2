package models;

import d3e.core.CloneContext;
import d3e.core.SchemaConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import org.apache.solr.client.solrj.beans.Field;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.solr.core.mapping.SolrDocument;
import store.DatabaseObject;
import store.ICloneable;

@SolrDocument(collection = "Customer")
@Entity
public class Customer extends CreatableObject {
  public static final int _NAME = 0;
  public static final int _AGE = 1;
  public static final int _UNDERAGE = 2;
  public static final int _GUARDIAN = 3;
  public static final int _INVOICES = 4;
  @Field @NotNull private String name;

  @Field
  @ColumnDefault("0")
  private long age = 0l;

  @Field
  @ColumnDefault("false")
  private boolean underAge = false;

  @Field
  @ManyToOne(fetch = FetchType.LAZY)
  private Customer guardian;

  @Field
  @ManyToMany(mappedBy = "customer")
  private List<Invoice> invoices = new ArrayList<>();

  private transient Customer old;

  @Override
  public int _typeIdx() {
    return SchemaConstants.Customer;
  }

  @Override
  public String _type() {
    return "Customer";
  }

  @Override
  public int _fieldsCount() {
    return 5;
  }

  public void addToInvoices(Invoice val, long index) {
    collFieldChanged(_INVOICES, this.invoices);
    if (index == -1) {
      this.invoices.add(val);
    } else {
      this.invoices.add(((int) index), val);
    }
  }

  public void removeFromInvoices(Invoice val) {
    collFieldChanged(_INVOICES, this.invoices);
    this.invoices.remove(val);
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

  public long getAge() {
    return this.age;
  }

  public void setAge(long age) {
    if (Objects.equals(this.age, age)) {
      return;
    }
    fieldChanged(_AGE, this.age);
    this.age = age;
  }

  public boolean isUnderAge() {
    return this.underAge;
  }

  public void setUnderAge(boolean underAge) {
    if (Objects.equals(this.underAge, underAge)) {
      return;
    }
    fieldChanged(_UNDERAGE, this.underAge);
    this.underAge = underAge;
  }

  public Customer getGuardian() {
    return this.guardian;
  }

  public void setGuardian(Customer guardian) {
    if (Objects.equals(this.guardian, guardian)) {
      return;
    }
    fieldChanged(_GUARDIAN, this.guardian);
    this.guardian = guardian;
  }

  public List<Invoice> getInvoices() {
    return this.invoices;
  }

  public void setInvoices(List<Invoice> invoices) {
    if (Objects.equals(this.invoices, invoices)) {
      return;
    }
    collFieldChanged(_INVOICES, this.invoices);
    this.invoices.clear();
    this.invoices.addAll(invoices);
  }

  public Customer getOld() {
    return this.old;
  }

  public void setOld(DatabaseObject old) {
    this.old = ((Customer) old);
  }

  public String displayName() {
    return "Customer";
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof Customer && super.equals(a);
  }

  public Customer deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    Customer _obj = ((Customer) dbObj);
    _obj.setName(name);
    _obj.setAge(age);
    _obj.setUnderAge(underAge);
    _obj.setGuardian(guardian);
    _obj.setInvoices(invoices);
  }

  public Customer cloneInstance(Customer cloneObj) {
    if (cloneObj == null) {
      cloneObj = new Customer();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setName(this.getName());
    cloneObj.setAge(this.getAge());
    cloneObj.setUnderAge(this.isUnderAge());
    cloneObj.setGuardian(this.getGuardian());
    cloneObj.setInvoices(new ArrayList<>(this.getInvoices()));
    return cloneObj;
  }

  public Customer createNewInstance() {
    return new Customer();
  }

  public boolean needOldObject() {
    return true;
  }

  public void collectCreatableReferences(List<Object> _refs) {
    super.collectCreatableReferences(_refs);
    _refs.add(this.guardian);
  }

  @Override
  public boolean _isEntity() {
    return true;
  }
}
