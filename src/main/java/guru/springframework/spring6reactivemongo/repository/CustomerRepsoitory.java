package guru.springframework.spring6reactivemongo.repository;

import guru.springframework.spring6reactivemongo.model.Customer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CustomerRepsoitory extends ReactiveMongoRepository<Customer, String> {
}
