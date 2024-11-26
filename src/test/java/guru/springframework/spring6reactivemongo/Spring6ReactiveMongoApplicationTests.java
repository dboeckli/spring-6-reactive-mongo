package guru.springframework.spring6reactivemongo;

import guru.springframework.spring6reactivemongo.service.BeerService;
import guru.springframework.spring6reactivemongo.service.CustomerService;
import guru.springframework.spring6reactivemongo.test.config.MongoExtension;
import guru.springframework.spring6reactivemongo.test.config.TestMongoDockerContainer;
import lombok.extern.java.Log;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log
@Import(TestMongoDockerContainer.class)
class Spring6ReactiveMongoApplicationTests {

    @Autowired
    BeerService beerService;
    
    @Autowired
    CustomerService customerService;

    @RegisterExtension
    MongoExtension instanceLevelExtension = new MongoExtension();

    @Test
    void contextLoads() {
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertEquals(3, beerService.listBeers().count().block());
            assertEquals(3, customerService.listCustomers().count().block());
        });
    }

}
