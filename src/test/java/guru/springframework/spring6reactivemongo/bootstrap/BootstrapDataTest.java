package guru.springframework.spring6reactivemongo.bootstrap;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import guru.springframework.spring6reactivemongo.dto.CustomerDto;
import guru.springframework.spring6reactivemongo.service.BeerService;
import guru.springframework.spring6reactivemongo.service.CustomerService;
import guru.springframework.spring6reactivemongo.test.helper.AbstractBaseMongoTestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BootstrapDataTest extends AbstractBaseMongoTestUtil {

    @Autowired
    BeerService beerService;
    
    @Autowired
    CustomerService customerService;

    private static final String DATABASE_NAME = BootstrapDataTest.class.getSimpleName();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.database", () -> DATABASE_NAME); // Replace with your desired database name
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl(DATABASE_NAME));
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
