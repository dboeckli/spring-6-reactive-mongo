package guru.springframework.spring6reactivemongo.service;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import guru.springframework.spring6reactivemongo.mapper.BeerMapper;
import guru.springframework.spring6reactivemongo.model.Beer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Testcontainers
@SpringBootTest
public class DummyTest {

    @Autowired
    BeerService beerService;

    @Autowired
    BeerMapper beerMapper;

    MongoDBContainer mongoDBContainer;
    @BeforeEach
    void setup() throws Exception {
        mongoDBContainer = new MongoDBContainer("mongo:7.0").withExposedPorts(27017);
        mongoDBContainer.start();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        mongoDBContainer.close();
    }

    @Test
        // TODO: THIS TEST REQUIRES RUNNING MONGO DB
    void testSaveBeer() throws InterruptedException {
        Mono<BeerDto> savedMono = beerService.saveBeer(Mono.just(beerMapper.beerToBeerDto(getTestBeer())));

        savedMono.subscribe(savedDto -> {
            System.out.println(savedDto.getId());
        });

        Thread.sleep(1000l);
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
