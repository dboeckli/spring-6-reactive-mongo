package guru.springframework.spring6reactivemongo.repository;

import guru.springframework.spring6reactivemongo.model.Customer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {

    Mono<Customer> findFirstByCustomerName(String customerName);
    
}
