package guru.springframework.spring6reactivemongo.test.config;

import guru.springframework.spring6reactivemongo.repository.BeerRepository;
import guru.springframework.spring6reactivemongo.repository.CustomerRepository;
import lombok.extern.java.Log;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@Log
@SpringBootTest
public abstract class AbstractMongoExtension {
    
    @Autowired
    CustomerRepository customerRepository;
    
    @Autowired
    BeerRepository beerRepository;

    @BeforeEach
    // TODO: Workaround for MongoDB startup
    void startUp() {
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            log.info("Waiting for data to be inserted...");
            assertThat(customerRepository.count().block().intValue(), greaterThan(2));
        });
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            log.info("Waiting for data to be inserted...");
            assertThat(beerRepository.count().block().intValue(), greaterThan(2));
        });
        log.info("Continue....");
    }
}
