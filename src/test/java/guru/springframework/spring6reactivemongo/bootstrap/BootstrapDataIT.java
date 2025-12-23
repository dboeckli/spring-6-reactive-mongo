package guru.springframework.spring6reactivemongo.bootstrap;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import guru.springframework.spring6reactivemongo.dto.CustomerDto;
import guru.springframework.spring6reactivemongo.service.BeerService;
import guru.springframework.spring6reactivemongo.service.CustomerService;
import guru.springframework.spring6reactivemongo.test.config.AuthServerDockerContainer;
import guru.springframework.spring6reactivemongo.test.config.MongoExtension;
import guru.springframework.spring6reactivemongo.test.config.TestMongoDockerContainer;
import lombok.extern.java.Log;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
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
@Import({AuthServerDockerContainer.class, TestMongoDockerContainer.class})
@ExtendWith(MongoExtension.class)
class BootstrapDataIT {

    @Autowired
    BeerService beerService;
    
    @Autowired
    CustomerService customerService;

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
