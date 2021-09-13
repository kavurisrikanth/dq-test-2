package repository.solr;

@org.springframework.stereotype.Repository
public interface InvoiceSolrRepository
    extends org.springframework.data.solr.repository.SolrCrudRepository<models.Invoice, Long> {}
