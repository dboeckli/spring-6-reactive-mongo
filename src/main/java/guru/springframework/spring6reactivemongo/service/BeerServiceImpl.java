package guru.springframework.spring6reactivemongo.service;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import guru.springframework.spring6reactivemongo.mapper.BeerMapper;
import guru.springframework.spring6reactivemongo.repository.BeerRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static guru.springframework.spring6reactivemongo.config.health.OpenApiConfiguration.SECURITY_SCHEME_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class BeerServiceImpl implements BeerService {

    private final BeerMapper beerMapper;
    
    private final BeerRepository beerRepository;

    @Override
    public Mono<BeerDto> saveBeer(Mono<BeerDto> beerDto) {
        return beerDto.map(beerMapper::beerDtoToBeer)
            .flatMap(beerRepository::save)
            .map(beerMapper::beerToBeerDto);
    }

    @Override
    public Mono<BeerDto> getById(String beerId) {
        return beerRepository.findById(beerId)
            .map(beerMapper::beerToBeerDto);
    }

    @Override
    public Flux<BeerDto> listBeers() {
        return beerRepository.findAll()
            .map(beerMapper::beerToBeerDto);
    }

    @Override
    public Mono<BeerDto> findFirstByBeerName(String beerName) {
        return beerRepository.findFirstByBeerName(beerName)
            .map(beerMapper::beerToBeerDto);
    }

    @Override
    public Flux<BeerDto> findByBeerStyle(String beerStyle) {
        return beerRepository.findByBeerStyle(beerStyle)
            .map(beerMapper::beerToBeerDto);
    }

    @Override
    public Mono<BeerDto> updateBeer(String beerId, BeerDto beerDto) {
        return beerRepository.findById(beerId)
            .map(foundBeer -> {
                //update properties
                foundBeer.setBeerName(beerDto.getBeerName());
                foundBeer.setBeerStyle(beerDto.getBeerStyle());
                foundBeer.setPrice(beerDto.getPrice());
                foundBeer.setUpc(beerDto.getUpc());
                foundBeer.setQuantityOnHand(beerDto.getQuantityOnHand());

                return foundBeer;
            }).flatMap(beerRepository::save)
            .map(beerMapper::beerToBeerDto);
    }

    @Override
    public Mono<BeerDto> patchBeer(String beerId, BeerDto beerDto) {
        return beerRepository.findById(beerId)
            .map(foundBeer -> {
                if(StringUtils.hasText(beerDto.getBeerName())){
                    foundBeer.setBeerName(beerDto.getBeerName());
                }

                if(StringUtils.hasText(beerDto.getBeerStyle())){
                    foundBeer.setBeerStyle(beerDto.getBeerStyle());
                }

                if(beerDto.getPrice() != null){
                    foundBeer.setPrice(beerDto.getPrice());
                }

                if(StringUtils.hasText(beerDto.getUpc())){
                    foundBeer.setUpc(beerDto.getUpc());
                }

                if(beerDto.getQuantityOnHand() != null){
                    foundBeer.setQuantityOnHand(beerDto.getQuantityOnHand());
                }
                return foundBeer;
            }).flatMap(beerRepository::save)
            .map(beerMapper::beerToBeerDto);
    }

    @Override
    public Mono<Void> deleteBeerById(String beerId) {
        log.info("Deleting beer with id: " + beerId);  
        return beerRepository.deleteById(beerId);
    }
}
