package guru.springframework.spring6reactivemongo.service;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import guru.springframework.spring6reactivemongo.mapper.BeerMapper;
import guru.springframework.spring6reactivemongo.model.Beer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@SpringBootTest
class BeerServiceImplTest {
    
    @Autowired
    BeerService beerService;
    
    @Autowired
    BeerMapper beerMapper;

    BeerDto beerDto;


    @BeforeEach
    void setup() throws Exception {
        beerDto = beerMapper.beerToBeerDto(getTestBeer());
    }

    @Test
    // TODO: THIS TEST REQUIRES RUNNING MONGO DB
    void testSaveBeer() throws InterruptedException {
        Mono<BeerDto> savedMono = beerService.saveBeer(Mono.just(beerDto));

        savedMono.subscribe(savedDto -> {
            System.out.println(savedDto.getId());
        });

        Thread.sleep(1000l);
    }

    @Test
    void testGetById() {
    }

    private static Beer getTestBeer() {
        return Beer.builder()
            .beerName("Space Dust")
            .beerStyle("IPA")
            .price(BigDecimal.TEN)
            .quantityOnHand(12)
            .upc("123213")
            .build();
    }
}
