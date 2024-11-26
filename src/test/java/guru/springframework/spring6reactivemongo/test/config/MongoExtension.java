package guru.springframework.spring6reactivemongo.test.config;

import guru.springframework.spring6reactivemongo.repository.BeerRepository;
import guru.springframework.spring6reactivemongo.repository.CustomerRepository;
import lombok.extern.java.Log;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@Log
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MongoExtension implements BeforeEachCallback {

    @Override
    // This is a workaround to make sure data is inserted before running tests (inserted by BootStrapData). In ExtensionContext.Callback 
    // interface we cannot autowire repositories here, so we have to do it manually.
    public void beforeEach(ExtensionContext context) {
        CustomerRepository customerRepository = SpringExtension.getApplicationContext(context).getBean(CustomerRepository.class);
        BeerRepository beerRepository = SpringExtension.getApplicationContext(context).getBean(BeerRepository.class);
        
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
