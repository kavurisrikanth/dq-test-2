package helpers;

import models.Invoice;
import models.InvoiceItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.jpa.InvoiceItemRepository;
import repository.jpa.InvoiceRepository;
import rest.GraphQLInputContext;
import store.EntityHelper;
import store.EntityMutator;
import store.EntityValidationContext;

@Service("InvoiceItem")
public class InvoiceItemEntityHelper<T extends InvoiceItem> implements EntityHelper<T> {
  @Autowired protected EntityMutator mutator;
  @Autowired private InvoiceItemRepository invoiceItemRepository;
  @Autowired private InvoiceRepository invoiceRepository;

  public void setMutator(EntityMutator obj) {
    mutator = obj;
  }

  public InvoiceItem newInstance() {
    return new InvoiceItem();
  }

  @Override
  public void fromInput(T entity, GraphQLInputContext ctx) {
    if (ctx.has("name")) {
      entity.setName(ctx.readString("name"));
    }
    if (ctx.has("otherNames")) {
      entity.setOtherNames(ctx.readStringColl("otherNames"));
    }
    if (ctx.has("cost")) {
      entity.setCost(ctx.readDouble("cost"));
    }
  }

  public void referenceFromValidations(T entity, EntityValidationContext validationContext) {}

  public void validateFieldCost(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    double it = entity.getCost();
  }

  public void validateInternal(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    validateFieldCost(entity, validationContext, onCreate, onUpdate);
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
    return id == 0l ? null : ((T) invoiceItemRepository.findById(id).orElse(null));
  }

  @Override
  public void setDefaults(T entity) {}

  @Override
  public void compute(T entity) {}

  private void deleteMostExpensiveItemInInvoice(T entity, EntityValidationContext deletionContext) {
    if (EntityHelper.haveUnDeleted(this.invoiceRepository.getByMostExpensiveItem(entity))) {
      deletionContext.addEntityError(
          "This InvoiceItem cannot be deleted as it is being referred to by Invoice.");
    }
  }

  public Boolean onDelete(T entity, boolean internal, EntityValidationContext deletionContext) {
    return true;
  }

  public void validateOnDelete(T entity, EntityValidationContext deletionContext) {
    this.deleteMostExpensiveItemInInvoice(entity, deletionContext);
  }

  @Override
  public Boolean onCreate(T entity, boolean internal) {
    return true;
  }

  @Override
  public Boolean onUpdate(T entity, boolean internal) {
    return true;
  }
}
