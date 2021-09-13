package lists;

import classes.AllCustomersWithLargeInvoices;
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
import java.util.Objects;
import java.util.stream.Collectors;
import models.AllCustomersWithLargeInvoicesRequest;
import models.Customer;
import models.Invoice;
import models.User;
import org.springframework.beans.factory.annotation.Autowired;
import repository.jpa.CustomerRepository;
import rest.ws.ClientSession;
import rest.ws.DataChangeTracker;
import store.StoreEventType;

public class AllCustomersWithLargeInvoicesChangeTracker implements Cancellable {
  private class AllCustomersWithLargeInvoicesData {
    long id;
    List<Invoice> _value0;
    long _value1;

    public AllCustomersWithLargeInvoicesData(long id, List<Invoice> _value0, long _value1) {
      this.id = id;
      this._value0 = _value0;
      this._value1 = _value1;
    }
  }

  private AllCustomersWithLargeInvoices root;
  private DataChangeTracker tracker;
  private ClientSession clientSession;
  private List<Disposable> disposables = ListExt.List();
  private AllCustomersWithLargeInvoicesRequest inputs;
  @Autowired private AllCustomersWithLargeInvoicesImpl allCustomersWithLargeInvoicesImpl;
  @Autowired private CustomerRepository customerRepository;
  private Field field;

  public AllCustomersWithLargeInvoicesChangeTracker(
      DataChangeTracker tracker, ClientSession clientSession, Field field) {
    this.tracker = tracker;
    this.clientSession = clientSession;
    this.field = field;
  }

  public void init(
      OutObject out,
      AllCustomersWithLargeInvoices initialData,
      AllCustomersWithLargeInvoicesRequest inputs) {
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

  private void storeInitialData(AllCustomersWithLargeInvoices initialData) {
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
    Disposable invoiceSubscribe =
        tracker.listen(
            SchemaConstants.Invoice,
            null,
            (obj, type) -> {
              if (type != StoreEventType.Update) {
                return;
              }
              Invoice model = ((Invoice) obj);
              if (model.getOld() == null) {
                return;
              }
              long id = model.getId();
              List<AllCustomersWithLargeInvoicesData> existing =
                  this.data.stream()
                      .filter(
                          (x) -> {
                            /*
                            TODO
                            */
                            return x._value1 == id;
                          })
                      .collect(Collectors.toList());
              if (existing.isEmpty()) {
                /*
                TODO: Caching
                */
              }
              existing.forEach(
                  (x) -> {
                    if (!(applyWhereI(x, model))) {
                      return;
                    }
                  });
            });
    disposables.add(invoiceSubscribe);
  }

  private AllCustomersWithLargeInvoicesData find(long id) {
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
          return Objects.equals(i.getMostExpensiveItem(), inputs.getItem());
        });
  }

  private void createInsertChange(Customer model) {
    root.addToItemsRef(
        new TypeAndId(
            model._typeIdx(),
            new AllCustomersWithLargeInvoicesData(
                model.getId(),
                model.getInvoices().stream().map((m) -> m.getId()).collect(Collectors.toList()),
                i.getId())),
        -1);
    fire();
  }

  private void createDeleteChange(Customer model) {
    long id = model.getId();
    AllCustomersWithLargeInvoicesData existing = find(id);
    if (existing == null) {
      return;
    }
    root.removeFromItemsRef(existing);
    fire();
  }

  private boolean applyWhereI(AllCustomersWithLargeInvoicesData data, Invoice invoice) {
    boolean noChange =
        ListExt.any(
            data._value0,
            (i) -> {
              return Objects.equals(invoice.getMostExpensiveItem(), inputs.getItem());
            });
    if (noChange) {
      return true;
    }
    Customer one = customerRepository.getOne(data.id);
    createDeleteChange(one);
    return false;
  }
}
