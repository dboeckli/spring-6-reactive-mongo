package guru.springframework.spring6reactivemongo.service;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import reactor.core.publisher.Mono;

public class BeerServiceImpl implements BeerService {
    @Override
    public Mono<BeerDto> saveBeer(BeerDto beerDto) {
        return null;
    }

    @Override
    public Mono<BeerDto> getById(String beerId) {
        return null;
    }
}
