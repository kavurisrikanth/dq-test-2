package lists;

import classes.AllCustomersWithAgedGuardians;
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
import java.util.stream.Collectors;
import models.Customer;
import models.User;
import org.springframework.beans.factory.annotation.Autowired;
import repository.jpa.CustomerRepository;
import rest.ws.ClientSession;
import rest.ws.DataChangeTracker;
import store.StoreEventType;

public class AllCustomersWithAgedGuardiansChangeTracker implements Cancellable {
  private class AllCustomersWithAgedGuardiansData {
    long id;
    boolean _value0;
    long _value1;
    long _value2;

    public AllCustomersWithAgedGuardiansData(long id, boolean _value0, long _value1, long _value2) {
      this.id = id;
      this._value0 = _value0;
      this._value1 = _value1;
      this._value2 = _value2;
    }
  }

  private AllCustomersWithAgedGuardians root;
  private DataChangeTracker tracker;
  private ClientSession clientSession;
  private List<Disposable> disposables = ListExt.List();
  @Autowired private AllCustomersWithAgedGuardiansImpl allCustomersWithAgedGuardiansImpl;
  @Autowired private CustomerRepository customerRepository;
  private Field field;

  public AllCustomersWithAgedGuardiansChangeTracker(
      DataChangeTracker tracker, ClientSession clientSession, Field field) {
    this.tracker = tracker;
    this.clientSession = clientSession;
    this.field = field;
  }

  public void init(OutObject out, AllCustomersWithAgedGuardians initialData) {
    {
      User currentUser = CurrentUser.get();
    }
    initialData._clearChanges();
    storeInitialData(initialData);
    addSubscriptions();
    out.setId(root.getId());
    disposables.add(tracker.listen(out, field, clientSession));
  }

  @Override
  public void cancel() {
    disposables.forEach((d) -> d.dispose());
  }

  private void storeInitialData(AllCustomersWithAgedGuardians initialData) {
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
    Disposable customerSubscribe =
        tracker.listen(
            SchemaConstants.Customer,
            null,
            (obj, type) -> {
              if (type != StoreEventType.Update) {
                return;
              }
              Customer model = ((Customer) obj);
              if (model.getOld() == null) {
                return;
              }
              long id = model.getId();
              List<AllCustomersWithAgedGuardiansData> existing =
                  this.data.stream()
                      .filter(
                          (x) -> {
                            /*
                            TODO
                            */
                            return x._value2 == id;
                          })
                      .collect(Collectors.toList());
              if (existing.isEmpty()) {
                /*
                TODO: Caching
                */
              }
              existing.forEach(
                  (x) -> {
                    if (!(applyWhereGuardian(x, model))) {
                      return;
                    }
                  });
            });
    disposables.add(customerSubscribe);
  }

  private AllCustomersWithAgedGuardiansData find(long id) {
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
    return model.isUnderAge() && model.getGuardian().getAge() >= 65l;
  }

  private void createInsertChange(Customer model) {
    root.addToItemsRef(
        new TypeAndId(
            model._typeIdx(),
            new AllCustomersWithAgedGuardiansData(
                model.getId(),
                model.isUnderAge(),
                model.getGuardian().getAge(),
                model.getGuardian().getId())),
        -1);
    fire();
  }

  private void createDeleteChange(Customer model) {
    long id = model.getId();
    AllCustomersWithAgedGuardiansData existing = find(id);
    if (existing == null) {
      return;
    }
    root.removeFromItemsRef(existing);
    fire();
  }

  private boolean applyWhereGuardian(AllCustomersWithAgedGuardiansData data, Customer customer) {
    boolean noChange = data._value0 && customer.getAge() >= 65l;
    if (noChange) {
      return true;
    }
    Customer one = customerRepository.getOne(data.id);
    createDeleteChange(one);
    return false;
  }
}
