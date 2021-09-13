package rest.ws;

import classes.LoginResult;
import d3e.core.CurrentUser;
import d3e.core.D3ELogger;
import gqltosql2.Field;
import gqltosql2.GqlToSql;
import gqltosql2.OutObject;
import java.util.UUID;
import lists.AllCustomersWithAgedGuardians2Impl;
import lists.AllCustomersWithAgedGuardiansImpl;
import lists.AllCustomersWithLargeInvoices2Impl;
import lists.AllCustomersWithLargeInvoicesImpl;
import lists.AllItemsImpl;
import models.OneTimePassword;
import models.User;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import repository.jpa.OneTimePasswordRepository;
import security.AppSessionProvider;
import security.JwtTokenUtil;
import security.UserProxy;

@Service
public class RocketQuery extends AbstractRocketQuery {
  @Autowired private GqlToSql gqlToSql;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private ObjectFactory<AppSessionProvider> provider;
  @Autowired private JwtTokenUtil jwtTokenUtil;
  @Autowired private OneTimePasswordRepository oneTimePasswordRepository;
  @Autowired private AllCustomersWithAgedGuardiansImpl allCustomersWithAgedGuardiansImpl;
  @Autowired private AllCustomersWithAgedGuardians2Impl allCustomersWithAgedGuardians2Impl;
  @Autowired private AllCustomersWithLargeInvoicesImpl allCustomersWithLargeInvoicesImpl;
  @Autowired private AllCustomersWithLargeInvoices2Impl allCustomersWithLargeInvoices2Impl;
  @Autowired private AllItemsImpl allItemsImpl;
  @Autowired private DataChangeTracker dataChangeTracker;

  protected LoginResult login(
      String query,
      String email,
      String phone,
      String username,
      String password,
      String deviceToken,
      String token,
      String code)
      throws Exception {
    D3ELogger.displayGraphQL(query, query, null);
    switch (query) {
      case "loginWithOTP":
        {
          return loginWithOTP(token, code, deviceToken);
        }
    }
    D3ELogger.info("Query Not found");
    return null;
  }

  protected QueryResult executeOperation(
      String query, Field field, RocketInputContext ctx, boolean subscribed, ClientSession session)
      throws Exception {
    D3ELogger.displayGraphQL(query, query, null);
    User currentUser = CurrentUser.get();
    switch (query) {
      case "getAnonymousUserById":
        {
          OutObject one = gqlToSql.execute("AnonymousUser", field, ctx.readLong());
          if (subscribed) {
            OutObjectTracker tracker = new OutObjectTracker(dataChangeTracker, session, field);
            tracker.init(one);
            return singleResult("AnonymousUser", false, one, tracker);
          }
          return singleResult("AnonymousUser", false, one);
        }
      case "getCustomerById":
        {
          OutObject one = gqlToSql.execute("Customer", field, ctx.readLong());
          if (subscribed) {
            OutObjectTracker tracker = new OutObjectTracker(dataChangeTracker, session, field);
            tracker.init(one);
            return singleResult("Customer", false, one, tracker);
          }
          return singleResult("Customer", false, one);
        }
      case "getInvoiceById":
        {
          OutObject one = gqlToSql.execute("Invoice", field, ctx.readLong());
          if (subscribed) {
            OutObjectTracker tracker = new OutObjectTracker(dataChangeTracker, session, field);
            tracker.init(one);
            return singleResult("Invoice", false, one, tracker);
          }
          return singleResult("Invoice", false, one);
        }
      case "getOneTimePasswordById":
        {
          OutObject one = gqlToSql.execute("OneTimePassword", field, ctx.readLong());
          if (subscribed) {
            OutObjectTracker tracker = new OutObjectTracker(dataChangeTracker, session, field);
            tracker.init(one);
            return singleResult("OneTimePassword", false, one, tracker);
          }
          return singleResult("OneTimePassword", false, one);
        }
    }
    D3ELogger.info("Query Not found");
    return null;
  }

  private LoginResult loginWithOTP(String token, String code, String deviceToken) throws Exception {
    OneTimePassword otp = oneTimePasswordRepository.getByToken(token);
    LoginResult loginResult = new LoginResult();
    if (otp == null) {
      loginResult.setSuccess(false);
      loginResult.setFailureMessage("Invalid token.");
      return loginResult;
    }
    if (otp.getExpiry().isBefore(java.time.LocalDateTime.now())) {
      loginResult.setSuccess(false);
      loginResult.setFailureMessage("OTP validity has expired.");
      return loginResult;
    }
    if (!(code.equals(otp.getCode()))) {
      loginResult.setSuccess(false);
      loginResult.setFailureMessage("Invalid code.");
      return loginResult;
    }
    User user = ((User) org.hibernate.Hibernate.unproxy(otp.getUser()));
    if (user == null) {
      loginResult.setSuccess(false);
      loginResult.setFailureMessage("Invalid user.");
      return loginResult;
    }
    loginResult.setSuccess(true);
    loginResult.setUserObject(user);
    String type = ((String) user.getClass().getSimpleName());
    String id = String.valueOf(user.getId());
    String finalToken =
        jwtTokenUtil.generateToken(
            id, new UserProxy(type, user.getId(), UUID.randomUUID().toString()));
    loginResult.setToken(finalToken);
    if (deviceToken != null) {
      user.setDeviceToken(deviceToken);
      store.Database.get().save(user);
    }
    return loginResult;
  }
}
