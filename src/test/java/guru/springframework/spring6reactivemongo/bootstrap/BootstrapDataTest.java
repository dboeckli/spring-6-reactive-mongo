package guru.springframework.spring6reactivemongo.bootstrap;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import guru.springframework.spring6reactivemongo.service.BeerService;
import lombok.extern.java.Log;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Log
class BootstrapDataTest {

    @Autowired
    BeerService beerService;

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo").withExposedPorts(27017);

    private static final String DATABASE_NAME = BootstrapDataTest.class.getSimpleName();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.database", () -> DATABASE_NAME); // Replace with your desired database name
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl(DATABASE_NAME));
    }

    @BeforeAll
    static void setup() {
        log.info("### Starting container on port: " + mongoDBContainer.getMappedPort(mongoDBContainer.getExposedPorts().getFirst()));
        mongoDBContainer.start();
        System.out.println("### ConnectionString: " + mongoDBContainer.getConnectionString());
        Map<String, String> env = mongoDBContainer.getEnvMap();
    }

    @AfterAll
    static void tearDown() {
        log.info("################## stopping container ####################");
        mongoDBContainer.close();
    }

    @Test
    void testBootstrapData() {
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            List<BeerDto> beers = beerService.listBeers().collectList().block();
            assertNotNull(beers);
            assertEquals(3, beers.size());
            assertThat(beers).extracting(BeerDto::getBeerName).contains("Galaxy Cat", "Crank", "Sunshine City");
        });
    }

}
