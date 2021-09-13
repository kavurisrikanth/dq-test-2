package repository.jpa;

import java.util.List;
import models.Customer;
import models.Invoice;
import models.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
  public List<Invoice> getByMostExpensiveItem(InvoiceItem mostExpensiveItem);

  public List<Invoice> getByCustomer(Customer customer);
}
