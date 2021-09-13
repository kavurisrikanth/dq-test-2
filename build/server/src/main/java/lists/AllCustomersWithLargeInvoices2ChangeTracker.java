package lists;

import classes.AllCustomersWithLargeInvoices2;
import classes.IdGenerator;
import d3e.core.CurrentUser;
import d3e.core.ListExt;
import d3e.core.SchemaConstants;
import d3e.core.TransactionManager;
import gqltosql2.Field;
import gqltosql2.OutObject;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Cancellable;
import java.util.List;
import models.AllCustomersWithLargeInvoices2Request;
import models.Customer;
import models.Invoice;
import models.InvoiceItem;
import models.User;
import rest.ws.ClientSession;
import rest.ws.DataChangeTracker;
import store.StoreEventType;

public class AllCustomersWithLargeInvoices2ChangeTracker implements Cancellable {
  private AllCustomersWithLargeInvoices2 root;
  private DataChangeTracker tracker;
  private ClientSession clientSession;
  private List<Disposable> disposables = ListExt.List();
  private AllCustomersWithLargeInvoices2Request inputs;
  private Field field;

  public AllCustomersWithLargeInvoices2ChangeTracker(
      DataChangeTracker tracker, ClientSession clientSession, Field field) {
    this.tracker = tracker;
    this.clientSession = clientSession;
    this.field = field;
  }

  public void init(
      OutObject out,
      AllCustomersWithLargeInvoices2 initialData,
      AllCustomersWithLargeInvoices2Request inputs) {
    {
      User currentUser = CurrentUser.get();
    }
    initialData._clearChanges();
    this.inputs = inputs;
    storeInitialData(initialData);
    addSubscriptions();
    out.setId(root.getId());
    disposables.add(tracker.listen(out, field, clientSession));
  }

  @Override
  public void cancel() {
    disposables.forEach((d) -> d.dispose());
  }

  private void storeInitialData(AllCustomersWithLargeInvoices2 initialData) {
    this.root = initialData;
    long id = IdGenerator.getNext();
    this.root.setId(id);
    initialData._convertToObjectRef();
  }

  private void addSubscriptions() {
    /*
    This method will register listeners on each reference that is referred to in the DataQuery expression.
    A listener is added by default on the Table from which we pull the data, since any change in that must trigger a subscription change.
    */
    Disposable baseSubscribe =
        tracker.listen(
            SchemaConstants.Customer, null, (obj, type) -> applyCustomer(((Customer) obj), type));
    disposables.add(baseSubscribe);
  }

  private TypeAndId find(long id) {
    /*
    TODO: Maybe remove
    */
    return this.root.getItemsRef().stream().filter((x) -> x.id == id).findFirst().orElse(null);
  }

  private boolean has(long id) {
    return this.root.getItemsRef().stream().anyMatch((x) -> x.id == id);
  }

  private void fire() {
    TransactionManager.get().update(root);
  }

  public void applyCustomer(Customer model, StoreEventType type) {
    if (type == StoreEventType.Insert) {
      /*
      New data is inserted
      So we just insert the new data depending on the clauses.
      */
      if (applyWhere(model)) {
        createInsertChange(model);
      }
    } else if (type == StoreEventType.Delete) {
      /*
      Existing data is deleted
      */
      createDeleteChange(model);
    } else {
      /*
      Existing data is updated
      */
      Customer old = model.getOld();
      if (old == null) {
        return;
      }
      boolean currentMatch = applyWhere(model);
      boolean oldMatch = has(old.getId());
      if (currentMatch == oldMatch) {
        if (!(currentMatch) && !(oldMatch)) {
          return;
        }
      } else {
        if (oldMatch) {
          createDeleteChange(model);
        }
        if (currentMatch) {
          createInsertChange(model);
        }
      }
    }
  }

  private boolean applyWhere(Customer model) {
    return ListExt.any(
        model.getInvoices(),
        (i) -> {
          return i.getMostExpensiveItem().getCost() == inputs.getItem();
        });
  }

  private void createInsertChange(Customer model) {
    root.addToItemsRef(new TypeAndId(model._typeIdx(), model.getId()), -1);
    fire();
  }

  private void createDeleteChange(Customer model) {
    long id = model.getId();
    TypeAndId existing = find(id);
    if (existing == null) {
      return;
    }
    root.removeFromItemsRef(existing);
    fire();
  }
}
