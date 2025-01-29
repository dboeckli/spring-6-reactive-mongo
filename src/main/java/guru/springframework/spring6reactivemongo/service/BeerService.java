package guru.springframework.spring6reactivemongo.service;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BeerService {
    Mono<BeerDto> saveBeer(Mono<BeerDto> beerDto);
    //Mono<BeerDto> saveBeer(BeerDto beerDto);

    Mono<BeerDto> getById(String beerId);

    Flux<BeerDto> listBeers();

    Mono<BeerDto> findFirstByBeerName(String beerName);

    Flux<BeerDto> findByBeerStyle(String beerStyle);

    Mono<BeerDto> updateBeer(String beerId, BeerDto beerDto);

    Mono<BeerDto> patchBeer(String beerId, BeerDto beerDto);

    Mono<Void> deleteBeerById(String beerId);
}
