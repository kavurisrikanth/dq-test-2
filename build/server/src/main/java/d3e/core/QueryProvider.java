package d3e.core;

import classes.AllCustomersWithAgedGuardians;
import classes.AllCustomersWithAgedGuardians2;
import classes.AllCustomersWithLargeInvoices;
import classes.AllItems;
import classes.LoginResult;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lists.AllCustomersWithAgedGuardians2Impl;
import lists.AllCustomersWithAgedGuardiansImpl;
import lists.AllCustomersWithLargeInvoicesImpl;
import lists.AllItemsImpl;
import models.AllCustomersWithLargeInvoicesRequest;
import models.AllItemsRequest;
import models.AnonymousUser;
import models.Customer;
import models.Invoice;
import models.OneTimePassword;
import models.User;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.jpa.AnonymousUserRepository;
import repository.jpa.AvatarRepository;
import repository.jpa.CustomerRepository;
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
  @Autowired private CustomerRepository customerRepository;
  @Autowired private InvoiceRepository invoiceRepository;
  @Autowired private InvoiceItemRepository invoiceItemRepository;
  @Autowired private OneTimePasswordRepository oneTimePasswordRepository;
  @Autowired private ReportConfigRepository reportConfigRepository;
  @Autowired private ReportConfigOptionRepository reportConfigOptionRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private UserSessionRepository userSessionRepository;
  @Autowired private AllCustomersWithAgedGuardiansImpl allCustomersWithAgedGuardiansImpl;
  @Autowired private AllCustomersWithAgedGuardians2Impl allCustomersWithAgedGuardians2Impl;
  @Autowired private AllCustomersWithLargeInvoicesImpl allCustomersWithLargeInvoicesImpl;
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

  public Customer getCustomerById(long id) {
    Optional<Customer> findById = customerRepository.findById(id);
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

  public AllCustomersWithAgedGuardians getAllCustomersWithAgedGuardians() {
    return allCustomersWithAgedGuardiansImpl.get();
  }

  public AllCustomersWithAgedGuardians2 getAllCustomersWithAgedGuardians2() {
    return allCustomersWithAgedGuardians2Impl.get();
  }

  public AllCustomersWithLargeInvoices getAllCustomersWithLargeInvoices(
      AllCustomersWithLargeInvoicesRequest inputs) {
    return allCustomersWithLargeInvoicesImpl.get(inputs);
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
