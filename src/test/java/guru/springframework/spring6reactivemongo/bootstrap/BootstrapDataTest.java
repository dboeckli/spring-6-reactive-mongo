package guru.springframework.spring6reactivemongo.bootstrap;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import guru.springframework.spring6reactivemongo.dto.CustomerDto;
import guru.springframework.spring6reactivemongo.repository.BeerRepository;
import guru.springframework.spring6reactivemongo.repository.CustomerRepository;
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

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log
@Import(TestMongoDockerContainer.class)
class BootstrapDataTest {

    @Autowired
    BeerService beerService;
    
    @Autowired
    CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private BeerRepository beerRepository;

    @RegisterExtension
    MongoExtension instanceLevelExtension = new MongoExtension();

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
