package repository.solr;

@org.springframework.stereotype.Repository
public interface InvoiceItemSolrRepository
    extends org.springframework.data.solr.repository.SolrCrudRepository<models.InvoiceItem, Long> {}
