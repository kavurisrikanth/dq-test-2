package lists;

import classes.AllCustomersWithLargeInvoices;
import classes.AllCustomersWithLargeInvoicesIn;
import d3e.core.SchemaConstants;
import gqltosql.GqlToSql;
import gqltosql.SqlRow;
import gqltosql2.OutObject;
import gqltosql2.OutObjectList;
import graphql.language.Field;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import models.AllCustomersWithLargeInvoicesRequest;
import models.Customer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rest.AbstractQueryService;
import rest.ws.RocketQuery;

@Service
public class AllCustomersWithLargeInvoicesImpl extends AbsDataQueryImpl {
  @Autowired private EntityManager em;
  @Autowired private GqlToSql gqlToSql;
  @Autowired private gqltosql2.GqlToSql gqlToSql2;

  public AllCustomersWithLargeInvoicesRequest inputToRequest(
      AllCustomersWithLargeInvoicesIn inputs) {
    AllCustomersWithLargeInvoicesRequest request = new AllCustomersWithLargeInvoicesRequest();
    request.setItem(inputs.item);
    return request;
  }

  public AllCustomersWithLargeInvoices get(AllCustomersWithLargeInvoicesIn inputs) {
    AllCustomersWithLargeInvoicesRequest request = inputToRequest(inputs);
    return get(request);
  }

  public AllCustomersWithLargeInvoices get(AllCustomersWithLargeInvoicesRequest request) {
    List<NativeObj> rows = getNativeResult(request);
    return getAsStruct(rows);
  }

  public AllCustomersWithLargeInvoices getAsStruct(List<NativeObj> rows) {
    List<Customer> result = new ArrayList<>();
    for (NativeObj _r1 : rows) {
      result.add(NativeSqlUtil.get(em, _r1.getRef(2), Customer.class));
    }
    AllCustomersWithLargeInvoices wrap = new AllCustomersWithLargeInvoices();
    wrap.setItems(result);
    return wrap;
  }

  public JSONObject getAsJson(Field field, AllCustomersWithLargeInvoicesIn inputs)
      throws Exception {
    AllCustomersWithLargeInvoicesRequest request = inputToRequest(inputs);
    return getAsJson(field, request);
  }

  public JSONObject getAsJson(Field field, AllCustomersWithLargeInvoicesRequest request)
      throws Exception {
    List<NativeObj> rows = getNativeResult(request);
    return getAsJson(field, rows);
  }

  public JSONObject getAsJson(Field field, List<NativeObj> rows) throws Exception {
    JSONArray array = new JSONArray();
    List<SqlRow> sqlDecl0 = new ArrayList<>();
    for (NativeObj _r1 : rows) {
      array.put(NativeSqlUtil.getJSONObject(_r1, sqlDecl0));
    }
    gqlToSql.execute("Customer", AbstractQueryService.inspect(field, ""), sqlDecl0);
    JSONObject result = new JSONObject();
    result.put("items", array);
    return result;
  }

  public OutObject getAsJson(gqltosql2.Field field, AllCustomersWithLargeInvoicesRequest request)
      throws Exception {
    List<NativeObj> rows = getNativeResult(request);
    return getAsJson(field, rows);
  }

  public OutObject getAsJson(gqltosql2.Field field, List<NativeObj> rows) throws Exception {
    OutObjectList array = new OutObjectList();
    OutObjectList sqlDecl0 = new OutObjectList();
    for (NativeObj _r1 : rows) {
      array.add(NativeSqlUtil.getOutObject(_r1, SchemaConstants.Customer, sqlDecl0));
    }
    gqlToSql2.execute("Customer", RocketQuery.inspect2(field, ""), sqlDecl0);
    OutObject result = new OutObject();
    result.addType(SchemaConstants.AllCustomersWithLargeInvoices);
    result.add("items", array);
    return result;
  }

  public List<NativeObj> getNativeResult(AllCustomersWithLargeInvoicesRequest request) {
    String sql =
        "select cast(array_agg(b._id) as text) a0, c._id a1, a._id a2 from _customer a left join _invoice b on b._customer_id = a._id where true in (select c._most_expensive_item_id = :param_0 a0 from _invoice c where c._customer_id = a._id) group by a._id";
    Query query = em.createNativeQuery(sql);
    setParameter(query, "param_0", request.getItem());
    this.logQuery(sql, query);
    List<NativeObj> result = NativeSqlUtil.createNativeObj(query.getResultList(), 2);
    return result;
  }
}
