package com.hadiid.erp.customer;

import com.hadiid.erp.common.exception.NotFoundException;
import com.hadiid.erp.common.web.PageResult;
import com.hadiid.erp.customer.dto.CustomerDto;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public PageResult<CustomerDto> search(String query, int page, int pageSize) {
        var items = repository.search(query, pageSize, page * pageSize);
        long total = repository.countSearch(query);
        return new PageResult<>(items, page, pageSize, total);
    }

    public CustomerDto getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Customer not found: " + id));
    }

    /**
     * Rule 15: do not duplicate a customer per job if the same customer already
     * exists. Looked up by registration number when supplied; created otherwise.
     */
    public Long findOrCreate(String name, String registrationNo, String phone, String address) {
        if (registrationNo != null && !registrationNo.isBlank()) {
            var existing = repository.findByRegistrationNo(registrationNo);
            if (existing.isPresent()) {
                return existing.get().getId();
            }
        }
        return repository.insert(name, registrationNo, phone, address);
    }
}
