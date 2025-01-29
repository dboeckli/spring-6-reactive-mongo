package guru.springframework.spring6reactivemongo.service;

import guru.springframework.spring6reactivemongo.dto.CustomerDto;
import guru.springframework.spring6reactivemongo.mapper.CustomerMapper;
import guru.springframework.spring6reactivemongo.repository.CustomerRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static guru.springframework.spring6reactivemongo.config.OpenApiConfiguration.SECURITY_SCHEME_NAME;

@Service
@RequiredArgsConstructor
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    private final CustomerMapper customerMapper;
    
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
        return customerRepository.findFirstByCustomerName(customerName)
            .map(customerMapper::customerToCustomerDto);
    }

    @Override
    public Mono<CustomerDto> updateCustomer(String customerId, CustomerDto customerDto) {
        return customerRepository.findById(customerId)
            .map(foundCustomer -> {
                //update properties
                foundCustomer.setCustomerName(customerDto.getCustomerName());
                return foundCustomer;
            }).flatMap(customerRepository::save)
            .map(customerMapper::customerToCustomerDto);
    }

    @Override
    public Mono<CustomerDto> patchCustomer(String customerId, CustomerDto customerDto) {
        return customerRepository.findById(customerId)
            .map(foundCustomer -> {
                if(StringUtils.hasText(customerDto.getCustomerName())){
                    foundCustomer.setCustomerName(customerDto.getCustomerName());
                }
                return foundCustomer;
            }).flatMap(customerRepository::save)
            .map(customerMapper::customerToCustomerDto);
    }

    @Override
    public Mono<Void> deleteCustomerById(String customerId) {
        return customerRepository.deleteById(customerId);
    }
}
