package guru.springframework.spring6reactivemongo.service;

import com.mongodb.reactivestreams.client.MongoClient;
import guru.springframework.spring6reactivemongo.dto.BeerDto;
import guru.springframework.spring6reactivemongo.mapper.BeerMapper;
import guru.springframework.spring6reactivemongo.model.Beer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Testcontainers
@SpringBootTest
class BeerServiceImplTest {

    @Autowired
    BeerService beerService;

    @Autowired
    BeerMapper beerMapper;
    
    @Autowired
    MongoClient mongoClient;

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0.3").withExposedPorts(27017);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
    
    @BeforeEach
    void setup() {
        System.out.println("### Starting container on port: " + mongoDBContainer.getMappedPort(mongoDBContainer.getExposedPorts().getFirst()));
        mongoDBContainer.start();
        System.out.println("### ConnectionString: " + mongoDBContainer.getConnectionString());
        System.out.println("### ReplicaSetUrl: " + mongoDBContainer.getReplicaSetUrl());
    }
    
    @AfterEach
    void tearDown() {
        System.out.println("################## stopping container ####################");
        mongoDBContainer.close();
    }

    @Test
    void testSaveBeer() throws InterruptedException {
        System.out.println("################## testSaveBeer ####################");
        Mono<BeerDto> savedMono = beerService.saveBeer(Mono.just(beerMapper.beerToBeerDto(getTestBeer())));

        savedMono.subscribe(savedDto -> {
            System.out.println("Save Beer ID: " + savedDto.getId());
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
