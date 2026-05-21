package com.crm.customer.repository.search;

import com.crm.customer.entity.document.CustomerDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerSearchRepository extends ElasticsearchRepository<CustomerDocument, String> {
    
    List<CustomerDocument> findByFullNameMatchesRegex(String name);
    List<CustomerDocument> findByEmail(String email);
}
