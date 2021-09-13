package lists;

import classes.AllItems;
import classes.AllItemsIn;
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
import models.AllItemsRequest;
import models.Invoice;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rest.AbstractQueryService;
import rest.ws.RocketQuery;

@Service
public class AllItemsImpl extends AbsDataQueryImpl {
  @Autowired private EntityManager em;
  @Autowired private GqlToSql gqlToSql;
  @Autowired private gqltosql2.GqlToSql gqlToSql2;

  public AllItemsRequest inputToRequest(AllItemsIn inputs) {
    AllItemsRequest request = new AllItemsRequest();
    request.setName(inputs.name);
    return request;
  }

  public AllItems get(AllItemsIn inputs) {
    AllItemsRequest request = inputToRequest(inputs);
    return get(request);
  }

  public AllItems get(AllItemsRequest request) {
    List<NativeObj> rows = getNativeResult(request);
    return getAsStruct(rows);
  }

  public AllItems getAsStruct(List<NativeObj> rows) {
    List<Invoice> result = new ArrayList<>();
    for (NativeObj _r1 : rows) {
      result.add(NativeSqlUtil.get(em, _r1.getRef(0), Invoice.class));
    }
    AllItems wrap = new AllItems();
    wrap.setItems(result);
    return wrap;
  }

  public JSONObject getAsJson(Field field, AllItemsIn inputs) throws Exception {
    AllItemsRequest request = inputToRequest(inputs);
    return getAsJson(field, request);
  }

  public JSONObject getAsJson(Field field, AllItemsRequest request) throws Exception {
    List<NativeObj> rows = getNativeResult(request);
    return getAsJson(field, rows);
  }

  public JSONObject getAsJson(Field field, List<NativeObj> rows) throws Exception {
    JSONArray array = new JSONArray();
    List<SqlRow> sqlDecl0 = new ArrayList<>();
    for (NativeObj _r1 : rows) {
      array.put(NativeSqlUtil.getJSONObject(_r1, sqlDecl0));
    }
    gqlToSql.execute("Invoice", AbstractQueryService.inspect(field, ""), sqlDecl0);
    JSONObject result = new JSONObject();
    result.put("items", array);
    return result;
  }

  public OutObject getAsJson(gqltosql2.Field field, AllItemsRequest request) throws Exception {
    List<NativeObj> rows = getNativeResult(request);
    return getAsJson(field, rows);
  }

  public OutObject getAsJson(gqltosql2.Field field, List<NativeObj> rows) throws Exception {
    OutObjectList array = new OutObjectList();
    OutObjectList sqlDecl0 = new OutObjectList();
    for (NativeObj _r1 : rows) {
      array.add(NativeSqlUtil.getOutObject(_r1, SchemaConstants.Invoice, sqlDecl0));
    }
    gqlToSql2.execute("Invoice", RocketQuery.inspect2(field, ""), sqlDecl0);
    OutObject result = new OutObject();
    result.addType(SchemaConstants.AllItems);
    result.add("items", array);
    return result;
  }

  public List<NativeObj> getNativeResult(AllItemsRequest request) {
    String sql =
        "select a._id a0 from _invoice a left join _customer b on b._id = a._customer_id where b._name = :param_0";
    Query query = em.createNativeQuery(sql);
    setParameter(query, "param_0", request.getName());
    this.logQuery(sql, query);
    List<NativeObj> result = NativeSqlUtil.createNativeObj(query.getResultList(), 0);
    return result;
  }
}
