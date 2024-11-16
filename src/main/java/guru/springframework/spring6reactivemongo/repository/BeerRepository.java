package guru.springframework.spring6reactivemongo.repository;

import guru.springframework.spring6reactivemongo.model.Beer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface BeerRepository extends ReactiveMongoRepository<Beer, String> {
}
