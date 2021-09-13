package lists;

import classes.AllCustomersWithAgedGuardians2;
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
import models.Customer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rest.AbstractQueryService;
import rest.ws.RocketQuery;

@Service
public class AllCustomersWithAgedGuardians2Impl extends AbsDataQueryImpl {
  @Autowired private EntityManager em;
  @Autowired private GqlToSql gqlToSql;
  @Autowired private gqltosql2.GqlToSql gqlToSql2;

  public AllCustomersWithAgedGuardians2 get() {
    List<NativeObj> rows = getNativeResult();
    return getAsStruct(rows);
  }

  public AllCustomersWithAgedGuardians2 getAsStruct(List<NativeObj> rows) {
    List<Customer> result = new ArrayList<>();
    for (NativeObj _r1 : rows) {
      result.add(NativeSqlUtil.get(em, _r1.getRef(2), Customer.class));
    }
    AllCustomersWithAgedGuardians2 wrap = new AllCustomersWithAgedGuardians2();
    wrap.setItems(result);
    return wrap;
  }

  public JSONObject getAsJson(Field field) throws Exception {
    List<NativeObj> rows = getNativeResult();
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

  public OutObject getAsJson(gqltosql2.Field field) throws Exception {
    List<NativeObj> rows = getNativeResult();
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
    result.addType(SchemaConstants.AllCustomersWithAgedGuardians2);
    result.add("items", array);
    return result;
  }

  public List<NativeObj> getNativeResult() {
    String sql =
        "select a._guardian_id a0, b._age a1, a._id a2 from _customer a left join _customer b on b._id = a._guardian_id where a._guardian_id != null and b._age >= 65";
    Query query = em.createNativeQuery(sql);
    this.logQuery(sql, query);
    List<NativeObj> result = NativeSqlUtil.createNativeObj(query.getResultList(), 2);
    return result;
  }
}
