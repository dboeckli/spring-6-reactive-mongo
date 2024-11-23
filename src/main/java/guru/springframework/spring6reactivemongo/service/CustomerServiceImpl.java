package guru.springframework.spring6reactivemongo.service;

import guru.springframework.spring6reactivemongo.dto.CustomerDto;
import guru.springframework.spring6reactivemongo.mapper.CustomerMapper;
import guru.springframework.spring6reactivemongo.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    
    CustomerRepository customerRepository;

    CustomerMapper customerMapper;
    
    @Override
    public Mono<CustomerDto> saveCustomer(Mono<CustomerDto> customerDto) {
        return customerDto.map(customerMapper::customerDtoToCustomer)
            .flatMap(customerRepository::save)
            .map(customerMapper::customerToCustomerDto);
    }

    @Override
    public Mono<CustomerDto> getById(String customerId) {
        return customerRepository.findById(customerId)
            .map(customerMapper::customerToCustomerDto);
    }

    @Override
    public Flux<CustomerDto> listCustomers() {
        return customerRepository.findAll()
            .map(customerMapper::customerToCustomerDto);
    }

    @Override
    public Mono<CustomerDto> findFirstByCustomerName(String customerName) {
        return null;
    }

    @Override
    public Mono<CustomerDto> updateCustomer(String customerId, CustomerDto customerDto) {
        return null;
    }

    @Override
    public Mono<CustomerDto> patchCustomer(String customerId, CustomerDto customerDto) {
        return null;
    }

    @Override
    public Mono<Void> deleteCustomerById(String customerId) {
        return null;
    }
}
