package classes;

import d3e.core.ListExt;
import d3e.core.SchemaConstants;
import java.util.List;
import lists.TypeAndId;
import models.Customer;
import store.DBObject;

public class AllCustomersWithAgedGuardians extends DBObject {
  public static final int _ITEMS = 0;
  private long id;
  private List<Customer> items = ListExt.List();
  private List<TypeAndId> itemsRef;

  public AllCustomersWithAgedGuardians() {}

  public AllCustomersWithAgedGuardians(List<Customer> items) {
    this.items = items;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public List<Customer> getItems() {
    return items;
  }

  public List<TypeAndId> getItemsRef() {
    return itemsRef;
  }

  public void setItems(List<Customer> items) {
    collFieldChanged(_ITEMS, this.items);
    this.items = items;
  }

  public void setItemsRef(List<TypeAndId> itemsRef) {
    collFieldChanged(_ITEMS, this.itemsRef);
    this.itemsRef = itemsRef;
  }

  public void addToItems(Customer val, long index) {
    collFieldChanged(_ITEMS, this.items);
    if (index == -1) {
      this.items.add(val);
    } else {
      this.items.add(((int) index), val);
    }
  }

  public void addToItemsRef(TypeAndId val, long index) {
    collFieldChanged(_ITEMS, this.itemsRef);
    if (index == -1) {
      this.itemsRef.add(val);
    } else {
      this.itemsRef.add(((int) index), val);
    }
  }

  public void removeFromItems(Customer val) {
    collFieldChanged(_ITEMS, this.items);
    this.items.remove(val);
  }

  public void removeFromItemsRef(TypeAndId val) {
    collFieldChanged(_ITEMS, this.itemsRef);
    this.itemsRef.remove(val);
  }

  @Override
  public int _typeIdx() {
    return SchemaConstants.AllCustomersWithAgedGuardians;
  }

  @Override
  public String _type() {
    return "AllCustomersWithAgedGuardians";
  }

  @Override
  public int _fieldsCount() {
    return 1;
  }

  public void _convertToObjectRef() {
    this.itemsRef = TypeAndId.fromList(this.items);
    this.items.clear();
  }
}
