package com.crm.customer.service.elasticsearch;

import com.crm.customer.entity.document.CustomerDocument;
import com.crm.customer.repository.search.CustomerSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerSearchService {

    private final CustomerSearchRepository searchRepository;

    public List<CustomerDocument> searchCustomers(String keyword) {
        // Simple regex search, can be enhanced with bool queries
        return searchRepository.findByFullNameMatchesRegex(".*" + keyword + ".*");
    }
}
