package helpers;

import d3e.core.IterableExt;
import java.util.List;
import models.Customer;
import models.Invoice;
import models.InvoiceItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.jpa.CustomerRepository;
import repository.jpa.InvoiceRepository;
import rest.GraphQLInputContext;
import store.EntityHelper;
import store.EntityMutator;
import store.EntityValidationContext;

@Service("Invoice")
public class InvoiceEntityHelper<T extends Invoice> implements EntityHelper<T> {
  @Autowired protected EntityMutator mutator;
  @Autowired private InvoiceRepository invoiceRepository;
  @Autowired private CustomerRepository customerRepository;

  public void setMutator(EntityMutator obj) {
    mutator = obj;
  }

  public Invoice newInstance() {
    return new Invoice();
  }

  @Override
  public void fromInput(T entity, GraphQLInputContext ctx) {
    if (ctx.has("mostExpensiveItem")) {
      entity.setMostExpensiveItem(ctx.readRef("mostExpensiveItem", "InvoiceItem"));
    }
    if (ctx.has("items")) {
      entity.setItems(ctx.readChildColl("items", "InvoiceItem"));
    }
    if (ctx.has("customer")) {
      entity.setCustomer(ctx.readRef("customer", "Customer"));
    }
    entity.updateMasters((o) -> {});
  }

  public List<InvoiceItem> computeMostExpensiveItemReferenceFrom(T entity) {
    return entity.getItems();
  }

  public void referenceFromValidations(T entity, EntityValidationContext validationContext) {
    if (entity.getMostExpensiveItem() != null
        && !(IterableExt.contains(entity.getItems(), entity.getMostExpensiveItem()))) {
      validationContext.addFieldError(
          "mostExpensiveItem", "mostExpensiveItem referenceFrom validation error");
    }
  }

  public void validateFieldCustomer(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    Customer it = entity.getCustomer();
    if (it == null) {
      validationContext.addFieldError("customer", "customer is required.");
      return;
    }
  }

  public void validateInternal(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    referenceFromValidations(entity, validationContext);
    validateFieldCustomer(entity, validationContext, onCreate, onUpdate);
    long itemsIndex = 0l;
    for (InvoiceItem obj : entity.getItems()) {
      InvoiceItemEntityHelper helper = mutator.getHelperByInstance(obj);
      if (onCreate) {
        helper.validateOnCreate(obj, validationContext.child("items", null, itemsIndex++));
      } else {
        helper.validateOnUpdate(obj, validationContext.child("items", null, itemsIndex++));
      }
    }
  }

  public void validateOnCreate(T entity, EntityValidationContext validationContext) {
    validateInternal(entity, validationContext, true, false);
  }

  public void validateOnUpdate(T entity, EntityValidationContext validationContext) {
    validateInternal(entity, validationContext, false, true);
  }

  public void computeTotalAmount(T entity) {
    try {
      entity.setTotalAmount(0.0d);
    } catch (RuntimeException e) {
    }
  }

  @Override
  public T clone(T entity) {
    return null;
  }

  @Override
  public T getById(long id) {
    return id == 0l ? null : ((T) invoiceRepository.findById(id).orElse(null));
  }

  @Override
  public void setDefaults(T entity) {
    for (InvoiceItem obj : entity.getItems()) {
      InvoiceItemEntityHelper helper = mutator.getHelperByInstance(obj);
      helper.setDefaults(obj);
    }
  }

  @Override
  public void compute(T entity) {
    this.computeTotalAmount(entity);
    for (InvoiceItem obj : entity.getItems()) {
      InvoiceItemEntityHelper helper = mutator.getHelperByInstance(obj);
      helper.compute(obj);
    }
  }

  public Boolean onDelete(T entity, boolean internal, EntityValidationContext deletionContext) {
    if (entity.getCustomer() != null) {
      entity.getCustomer().removeFromInvoices(entity);
    }
    return true;
  }

  @Override
  public Boolean onCreate(T entity, boolean internal) {
    return true;
  }

  @Override
  public Boolean onUpdate(T entity, boolean internal) {
    return true;
  }

  public T getOld(long id) {
    return ((T) getById(id).clone());
  }
}
