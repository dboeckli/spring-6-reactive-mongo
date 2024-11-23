package guru.springframework.spring6reactivemongo.bootstrap;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import guru.springframework.spring6reactivemongo.dto.CustomerDto;
import guru.springframework.spring6reactivemongo.service.BeerService;
import guru.springframework.spring6reactivemongo.service.CustomerService;
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
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Log
class BootstrapDataTest {

    @Autowired
    BeerService beerService;
    
    @Autowired
    CustomerService customerService;

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0.3")
        .withExposedPorts(27017)
        .withReuse(false);

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
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertTrue(mongoDBContainer.isRunning());
            log.info("Container started.");
        });
        log.info("### ConnectionString: " + mongoDBContainer.getConnectionString());
    }

    @AfterAll
    static void tearDown() {
        log.info("Stopping container");
        mongoDBContainer.close();
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertFalse(mongoDBContainer.isRunning());
            log.info("Container stopped.");
        });
    }

    @Test
    void testBootstrapData() {
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            List<BeerDto> beers = beerService.listBeers().collectList().block();
            List<CustomerDto> customers = customerService.listCustomers().collectList().block();
            assertNotNull(beers);
            assertEquals(3, beers.size());
            assertThat(beers).extracting(BeerDto::getBeerName).contains("Galaxy Cat", "Crank", "Sunshine City");
            assertNotNull(customers);
            assertEquals(3, customers.size());
            assertThat(customers).extracting(CustomerDto::getCustomerName).contains("John Doe", "Fridolin Mann", "Hansjörg Riesen");
        });
    }

}
