package helpers;

import models.Customer;
import models.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.jpa.CustomerRepository;
import repository.jpa.InvoiceRepository;
import rest.GraphQLInputContext;
import store.EntityHelper;
import store.EntityMutator;
import store.EntityValidationContext;

@Service("Customer")
public class CustomerEntityHelper<T extends Customer> implements EntityHelper<T> {
  @Autowired protected EntityMutator mutator;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private InvoiceRepository invoiceRepository;

  public void setMutator(EntityMutator obj) {
    mutator = obj;
  }

  public Customer newInstance() {
    return new Customer();
  }

  @Override
  public void fromInput(T entity, GraphQLInputContext ctx) {
    if (ctx.has("name")) {
      entity.setName(ctx.readString("name"));
    }
    if (ctx.has("age")) {
      entity.setAge(ctx.readInteger("age"));
    }
    if (ctx.has("guardian")) {
      entity.setGuardian(ctx.readRef("guardian", "Customer"));
    }
    entity.updateMasters((o) -> {});
  }

  public void referenceFromValidations(T entity, EntityValidationContext validationContext) {}

  public void validateFieldName(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    String it = entity.getName();
    if (it == null) {
      validationContext.addFieldError("name", "name is required.");
      return;
    }
  }

  public void validateFieldAge(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    long it = entity.getAge();
  }

  public void validateFieldGuardian(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    Customer it = entity.getGuardian();
    if (it == null) {
      if (isGuardianExists(entity)) {
        validationContext.addFieldError("guardian", "guardian is required.");
      } else {
        entity.setGuardian(null);
      }
      return;
    }
  }

  public void validateInternal(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    validateFieldName(entity, validationContext, onCreate, onUpdate);
    validateFieldAge(entity, validationContext, onCreate, onUpdate);
    if (isGuardianExists(entity)) {
      validateFieldGuardian(entity, validationContext, onCreate, onUpdate);
    }
    isGuardianExists(entity);
  }

  public void validateOnCreate(T entity, EntityValidationContext validationContext) {
    validateInternal(entity, validationContext, true, false);
  }

  public void validateOnUpdate(T entity, EntityValidationContext validationContext) {
    validateInternal(entity, validationContext, false, true);
  }

  public void computeUnderAge(T entity) {
    try {
      entity.setUnderAge(false);
    } catch (RuntimeException e) {
    }
  }

  public boolean isGuardianExists(T entity) {
    try {
      if (entity.isUnderAge()) {
        return true;
      } else {
        entity.setGuardian(null);
        return false;
      }
    } catch (RuntimeException e) {
      return false;
    }
  }

  @Override
  public T clone(T entity) {
    return null;
  }

  @Override
  public T getById(long id) {
    return id == 0l ? null : ((T) customerRepository.findById(id).orElse(null));
  }

  @Override
  public void setDefaults(T entity) {}

  @Override
  public void compute(T entity) {
    this.computeUnderAge(entity);
  }

  private void deleteGuardianInCustomer(T entity, EntityValidationContext deletionContext) {
    if (EntityHelper.haveUnDeleted(this.customerRepository.getByGuardian(entity))) {
      deletionContext.addEntityError(
          "This Customer cannot be deleted as it is being referred to by Customer.");
    }
  }

  private void deleteCustomerInInvoice(T entity, EntityValidationContext deletionContext) {
    if (EntityHelper.haveUnDeleted(this.invoiceRepository.getByCustomer(entity))) {
      deletionContext.addEntityError(
          "This Customer cannot be deleted as it is being referred to by Invoice.");
    }
  }

  public Boolean onDelete(T entity, boolean internal, EntityValidationContext deletionContext) {
    return true;
  }

  public void validateOnDelete(T entity, EntityValidationContext deletionContext) {
    this.deleteGuardianInCustomer(entity, deletionContext);
    this.deleteCustomerInInvoice(entity, deletionContext);
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
