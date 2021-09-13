package rest;

import d3e.core.D3ELogger;
import d3e.core.D3ESubscription;
import d3e.core.D3ESubscriptionEvent;
import d3e.core.ListExt;
import gqltosql.GqlToSql;
import gqltosql.SqlRow;
import gqltosql.schema.GraphQLDataFetcher;
import gqltosql.schema.IModelSchema;
import graphql.language.Field;
import io.reactivex.rxjava3.core.Flowable;
import java.util.HashMap;
import java.util.List;
import lists.AllCustomersWithAgedGuardians2SubscriptionHelper;
import lists.AllCustomersWithAgedGuardiansSubscriptionHelper;
import lists.AllCustomersWithLargeInvoicesSubscriptionHelper;
import lists.DataQueryChange;
import models.AllCustomersWithLargeInvoicesRequest;
import org.json.JSONObject;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import store.DatabaseObject;
import store.EntityHelperService;
import store.EntityMutator;

@Component
public class NativeSubscription extends AbstractQueryService {
  @Autowired private EntityMutator mutator;
  @Autowired private ObjectFactory<EntityHelperService> helperService;
  @Autowired private D3ESubscription subscription;
  @Autowired private IModelSchema schema;
  @Autowired private GqlToSql gqltosql;

  @Autowired
  private ObjectFactory<AllCustomersWithAgedGuardiansSubscriptionHelper>
      allCustomersWithAgedGuardians;

  @Autowired
  private ObjectFactory<AllCustomersWithAgedGuardians2SubscriptionHelper>
      allCustomersWithAgedGuardians2;

  @Autowired
  private ObjectFactory<AllCustomersWithLargeInvoicesSubscriptionHelper>
      allCustomersWithLargeInvoices;

  public Flowable<JSONObject> subscribe(JSONObject req) throws Exception {
    List<Field> fields = parseFields(req);
    Field field = fields.get(0);
    JSONObject variables = req.getJSONObject("variables");
    return executeOperation(field, variables);
  }

  private JSONObject fromDataQueryDataChange(DataQueryChange<?> event, Field field) {
    JSONObject data = new JSONObject();
    JSONObject opData = new JSONObject();
    try {
      opData.put("changeType", event.changeType.name());
      opData.put("path", event.path);
      opData.put("data", event.data);
      opData.put("position", event.index);
      data.put(field.getName(), opData);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return data;
  }

  private <T> JSONObject fromD3ESubscriptionEventExternal(
      D3ESubscriptionEvent<T> event, Field field, String type) {
    JSONObject data = new JSONObject();
    JSONObject opData = new JSONObject();
    try {
      opData.put("changeType", event.changeType.name());
      opData.put(
          "model",
          new GraphQLDataFetcher(schema).fetch(inspect(field, "model"), type, event.model));
      data.put(field.getName(), opData);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return data;
  }

  private <T> JSONObject fromD3ESubscriptionEvent(
      D3ESubscriptionEvent<T> event, Field field, String type) {
    JSONObject data = new JSONObject();
    JSONObject opData = new JSONObject();
    try {
      opData.put("changeType", event.changeType.name());
      if (event.model instanceof DatabaseObject) {
        long id = ((DatabaseObject) event.model).getId();
        SqlRow row = new SqlRow();
        row.put("id", id);
        opData.put("model", row);
        gqltosql.execute(type, inspect(field, "model"), ListExt.asList(row));
      }
      data.put(field.getName(), opData);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return data;
  }

  protected Flowable<JSONObject> executeOperation(Field field, JSONObject variables)
      throws Exception {
    GraphQLInputContext ctx =
        new ArgumentInputContext(
            field.getArguments(),
            helperService.getObject(),
            new HashMap<>(),
            new HashMap<>(),
            variables);
    D3ELogger.info("Subscription: " + field.getName());
    switch (field.getName()) {
      case "onAnonymousUserChangeEvent":
        {
          return subscription
              .onAnonymousUserChangeEvent()
              .map((e) -> fromD3ESubscriptionEvent(e, field, "AnonymousUser"));
        }
      case "onAnonymousUserChangeEventById":
        {
          List<Long> ids = ctx.readLongColl("ids");
          return subscription
              .onAnonymousUserChangeEvent()
              .filter((e) -> ids.contains(e.model.getId()))
              .map((e) -> fromD3ESubscriptionEvent(e, field, "AnonymousUser"));
        }
      case "onCustomerChangeEvent":
        {
          return subscription
              .onCustomerChangeEvent()
              .map((e) -> fromD3ESubscriptionEvent(e, field, "Customer"));
        }
      case "onCustomerChangeEventById":
        {
          List<Long> ids = ctx.readLongColl("ids");
          return subscription
              .onCustomerChangeEvent()
              .filter((e) -> ids.contains(e.model.getId()))
              .map((e) -> fromD3ESubscriptionEvent(e, field, "Customer"));
        }
      case "onCustomerInvoicesChange":
        {
          Long id = ctx.readLong("id");
          return subscription
              .onInvoiceChangeEvent()
              .filter((e) -> e.model.getCustomer() != null && e.model.getCustomer().getId() == id)
              .map((e) -> fromD3ESubscriptionEvent(e, field, "Invoice"));
        }
      case "onInvoiceChangeEvent":
        {
          return subscription
              .onInvoiceChangeEvent()
              .map((e) -> fromD3ESubscriptionEvent(e, field, "Invoice"));
        }
      case "onInvoiceChangeEventById":
        {
          List<Long> ids = ctx.readLongColl("ids");
          return subscription
              .onInvoiceChangeEvent()
              .filter((e) -> ids.contains(e.model.getId()))
              .map((e) -> fromD3ESubscriptionEvent(e, field, "Invoice"));
        }
      case "onOneTimePasswordChangeEvent":
        {
          return subscription
              .onOneTimePasswordChangeEvent()
              .map((e) -> fromD3ESubscriptionEvent(e, field, "OneTimePassword"));
        }
      case "onOneTimePasswordChangeEventById":
        {
          List<Long> ids = ctx.readLongColl("ids");
          return subscription
              .onOneTimePasswordChangeEvent()
              .filter((e) -> ids.contains(e.model.getId()))
              .map((e) -> fromD3ESubscriptionEvent(e, field, "OneTimePassword"));
        }
      case "onUserChangeEvent":
        {
          return subscription
              .onUserChangeEvent()
              .map((e) -> fromD3ESubscriptionEvent(e, field, "User"));
        }
      case "onUserChangeEventById":
        {
          List<Long> ids = ctx.readLongColl("ids");
          return subscription
              .onUserChangeEvent()
              .filter((e) -> ids.contains(e.model.getId()))
              .map((e) -> fromD3ESubscriptionEvent(e, field, "User"));
        }
      case "onUserSessionChangeEvent":
        {
          return subscription
              .onUserSessionChangeEvent()
              .map((e) -> fromD3ESubscriptionEvent(e, field, "UserSession"));
        }
      case "onUserSessionChangeEventById":
        {
          List<Long> ids = ctx.readLongColl("ids");
          return subscription
              .onUserSessionChangeEvent()
              .filter((e) -> ids.contains(e.model.getId()))
              .map((e) -> fromD3ESubscriptionEvent(e, field, "UserSession"));
        }
      case "onAllCustomersWithAgedGuardiansChange":
        {
          return allCustomersWithAgedGuardians
              .getObject()
              .subscribe(inspect(field, "data.items"))
              .map((e) -> fromDataQueryDataChange(e, field));
        }
      case "onAllCustomersWithAgedGuardians2Change":
        {
          return allCustomersWithAgedGuardians2
              .getObject()
              .subscribe(inspect(field, "data.items"))
              .map((e) -> fromDataQueryDataChange(e, field));
        }
      case "onAllCustomersWithLargeInvoicesChange":
        {
          AllCustomersWithLargeInvoicesRequest req =
              ctx.readChild("in", "AllCustomersWithLargeInvoicesRequest");
          return allCustomersWithLargeInvoices
              .getObject()
              .subscribe(inspect(field, "data.items"), req)
              .map((e) -> fromDataQueryDataChange(e, field));
        }
    }
    D3ELogger.info("Subscription Not found");
    return null;
  }
}
