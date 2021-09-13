package d3e.core;

import classes.AllItems;
import classes.LoginResult;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lists.AllItemsImpl;
import models.AllItemsRequest;
import models.AnonymousUser;
import models.Invoice;
import models.OneTimePassword;
import models.User;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.jpa.AnonymousUserRepository;
import repository.jpa.AvatarRepository;
import repository.jpa.InvoiceItemRepository;
import repository.jpa.InvoiceRepository;
import repository.jpa.OneTimePasswordRepository;
import repository.jpa.ReportConfigOptionRepository;
import repository.jpa.ReportConfigRepository;
import repository.jpa.UserRepository;
import repository.jpa.UserSessionRepository;
import security.AppSessionProvider;
import security.JwtTokenUtil;

@Service
public class QueryProvider {
  public static QueryProvider instance;
  @Autowired private JwtTokenUtil jwtTokenUtil;
  @Autowired private AnonymousUserRepository anonymousUserRepository;
  @Autowired private AvatarRepository avatarRepository;
  @Autowired private InvoiceRepository invoiceRepository;
  @Autowired private InvoiceItemRepository invoiceItemRepository;
  @Autowired private OneTimePasswordRepository oneTimePasswordRepository;
  @Autowired private ReportConfigRepository reportConfigRepository;
  @Autowired private ReportConfigOptionRepository reportConfigOptionRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private UserSessionRepository userSessionRepository;
  @Autowired private AllItemsImpl allItemsImpl;
  @Autowired private ObjectFactory<AppSessionProvider> provider;

  @PostConstruct
  public void init() {
    instance = this;
  }

  public static QueryProvider get() {
    return instance;
  }

  public AnonymousUser getAnonymousUserById(long id) {
    Optional<AnonymousUser> findById = anonymousUserRepository.findById(id);
    return findById.orElse(null);
  }

  public Invoice getInvoiceById(long id) {
    Optional<Invoice> findById = invoiceRepository.findById(id);
    return findById.orElse(null);
  }

  public OneTimePassword getOneTimePasswordById(long id) {
    Optional<OneTimePassword> findById = oneTimePasswordRepository.findById(id);
    return findById.orElse(null);
  }

  public boolean checkTokenUniqueInOneTimePassword(long oneTimePasswordId, String token) {
    return oneTimePasswordRepository.checkTokenUnique(oneTimePasswordId, token);
  }

  public AllItems getAllItems(AllItemsRequest inputs) {
    return allItemsImpl.get(inputs);
  }

  public LoginResult loginWithOTP(String token, String code, String deviceToken) {
    OneTimePassword otp = oneTimePasswordRepository.getByToken(token);
    User user = otp.getUser();
    LoginResult loginResult = new LoginResult();
    if (deviceToken != null) {
      user.setDeviceToken(deviceToken);
    }
    loginResult.setSuccess(true);
    loginResult.setUserObject(otp.getUser());
    loginResult.setToken(token);
    return loginResult;
  }
}
