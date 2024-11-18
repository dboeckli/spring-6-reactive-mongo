package guru.springframework.spring6reactivemongo.service;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import guru.springframework.spring6reactivemongo.mapper.BeerMapper;
import guru.springframework.spring6reactivemongo.repository.BeerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
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
    public Mono<BeerDto> saveBeer(BeerDto beerDto) {
        return beerRepository.save(beerMapper.beerDtoToBeer(beerDto))
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
        return beerRepository.deleteById(beerId);
    }
}
