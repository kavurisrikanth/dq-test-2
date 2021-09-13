package classes;

import d3e.core.ListExt;
import d3e.core.SchemaConstants;
import java.util.List;
import lists.TypeAndId;
import models.Invoice;
import store.DBObject;

public class AllItems extends DBObject {
  public static final int _ITEMS = 0;
  private long id;
  private List<Invoice> items = ListExt.List();
  private List<TypeAndId> itemsRef;

  public AllItems() {}

  public AllItems(List<Invoice> items) {
    this.items = items;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public List<Invoice> getItems() {
    return items;
  }

  public List<TypeAndId> getItemsRef() {
    return itemsRef;
  }

  public void setItems(List<Invoice> items) {
    collFieldChanged(_ITEMS, this.items);
    this.items = items;
  }

  public void setItemsRef(List<TypeAndId> itemsRef) {
    collFieldChanged(_ITEMS, this.itemsRef);
    this.itemsRef = itemsRef;
  }

  public void addToItems(Invoice val, long index) {
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

  public void removeFromItems(Invoice val) {
    collFieldChanged(_ITEMS, this.items);
    this.items.remove(val);
  }

  public void removeFromItemsRef(TypeAndId val) {
    collFieldChanged(_ITEMS, this.itemsRef);
    this.itemsRef.remove(val);
  }

  @Override
  public int _typeIdx() {
    return SchemaConstants.AllItems;
  }

  @Override
  public String _type() {
    return "AllItems";
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
