package lists;

import classes.AllCustomersWithLargeInvoices2;
import classes.AllCustomersWithLargeInvoices2In;
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
import models.AllCustomersWithLargeInvoices2Request;
import models.Customer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rest.AbstractQueryService;
import rest.ws.RocketQuery;

@Service
public class AllCustomersWithLargeInvoices2Impl extends AbsDataQueryImpl {
  @Autowired private EntityManager em;
  @Autowired private GqlToSql gqlToSql;
  @Autowired private gqltosql2.GqlToSql gqlToSql2;

  public AllCustomersWithLargeInvoices2Request inputToRequest(
      AllCustomersWithLargeInvoices2In inputs) {
    AllCustomersWithLargeInvoices2Request request = new AllCustomersWithLargeInvoices2Request();
    request.setItem(inputs.item);
    return request;
  }

  public AllCustomersWithLargeInvoices2 get(AllCustomersWithLargeInvoices2In inputs) {
    AllCustomersWithLargeInvoices2Request request = inputToRequest(inputs);
    return get(request);
  }

  public AllCustomersWithLargeInvoices2 get(AllCustomersWithLargeInvoices2Request request) {
    List<NativeObj> rows = getNativeResult(request);
    return getAsStruct(rows);
  }

  public AllCustomersWithLargeInvoices2 getAsStruct(List<NativeObj> rows) {
    List<Customer> result = new ArrayList<>();
    for (NativeObj _r1 : rows) {
      result.add(NativeSqlUtil.get(em, _r1.getRef(1), Customer.class));
    }
    AllCustomersWithLargeInvoices2 wrap = new AllCustomersWithLargeInvoices2();
    wrap.setItems(result);
    return wrap;
  }

  public JSONObject getAsJson(Field field, AllCustomersWithLargeInvoices2In inputs)
      throws Exception {
    AllCustomersWithLargeInvoices2Request request = inputToRequest(inputs);
    return getAsJson(field, request);
  }

  public JSONObject getAsJson(Field field, AllCustomersWithLargeInvoices2Request request)
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

  public OutObject getAsJson(gqltosql2.Field field, AllCustomersWithLargeInvoices2Request request)
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
    result.addType(SchemaConstants.AllCustomersWithLargeInvoices2);
    result.add("items", array);
    return result;
  }

  public List<NativeObj> getNativeResult(AllCustomersWithLargeInvoices2Request request) {
    String sql =
        "select cast(array_agg(b._id) as text) a0, a._id a1 from _customer a left join _invoice b on b._customer_id = a._id where true in (select e._cost = :param_0 a0 from _invoice d left join _invoice_item e on e._id = d._most_expensive_item_id where d._customer_id = a._id) group by a._id";
    Query query = em.createNativeQuery(sql);
    setParameter(query, "param_0", request.getItem());
    this.logQuery(sql, query);
    List<NativeObj> result = NativeSqlUtil.createNativeObj(query.getResultList(), 1);
    return result;
  }
}
