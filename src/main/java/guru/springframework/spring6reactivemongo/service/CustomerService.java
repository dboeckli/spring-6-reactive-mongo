package guru.springframework.spring6reactivemongo.service;

import guru.springframework.spring6reactivemongo.dto.CustomerDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {
    Mono<CustomerDto> saveCustomer(Mono<CustomerDto> customerDto);

    Mono<CustomerDto> getById(String customerId);

    Flux<CustomerDto> listCustomers();

    Mono<CustomerDto> findFirstByCustomerName(String customerName);

    Mono<CustomerDto> updateCustomer(String customerId, CustomerDto customerDto);

    Mono<CustomerDto> patchCustomer(String customerId, CustomerDto customerDto);

    Mono<Void> deleteCustomerById(String customerId);
    
}
