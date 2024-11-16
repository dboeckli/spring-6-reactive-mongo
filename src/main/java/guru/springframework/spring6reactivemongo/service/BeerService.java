package guru.springframework.spring6reactivemongo.service;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import reactor.core.publisher.Mono;

public interface BeerService {
    Mono<BeerDto> saveBeer(Mono<BeerDto> beerDto);

    Mono<BeerDto> getById(String beerId);
}
