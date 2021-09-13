package helpers;

import models.Invoice;
import models.InvoiceItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.jpa.InvoiceRepository;
import rest.GraphQLInputContext;
import store.EntityHelper;
import store.EntityMutator;
import store.EntityValidationContext;

@Service("Invoice")
public class InvoiceEntityHelper<T extends Invoice> implements EntityHelper<T> {
  @Autowired protected EntityMutator mutator;
  @Autowired private InvoiceRepository invoiceRepository;

  public void setMutator(EntityMutator obj) {
    mutator = obj;
  }

  public Invoice newInstance() {
    return new Invoice();
  }

  @Override
  public void fromInput(T entity, GraphQLInputContext ctx) {
    if (ctx.has("name")) {
      entity.setName(ctx.readString("name"));
    }
    if (ctx.has("items")) {
      entity.setItems(ctx.readChildColl("items", "InvoiceItem"));
    }
    entity.updateMasters((o) -> {});
  }

  public void referenceFromValidations(T entity, EntityValidationContext validationContext) {}

  public void validateInternal(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
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
    for (InvoiceItem obj : entity.getItems()) {
      InvoiceItemEntityHelper helper = mutator.getHelperByInstance(obj);
      helper.compute(obj);
    }
  }

  public Boolean onDelete(T entity, boolean internal, EntityValidationContext deletionContext) {
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
