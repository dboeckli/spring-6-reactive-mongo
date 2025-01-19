package guru.springframework.spring6reactivemongo.service;

import guru.springframework.spring6reactivemongo.dto.CustomerDto;
import guru.springframework.spring6reactivemongo.mapper.CustomerMapper;
import guru.springframework.spring6reactivemongo.model.Customer;
import lombok.extern.java.Log;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("docker-with-compose")
@Log
class CustomerServiceImplDockerComposeIT {

    @Autowired
    CustomerService customerService;

    @Autowired
    CustomerMapper customerMapper;

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> "mongodb://localhost:27018/sfg");
        registry.add("spring.data.mongodb.database", () -> "sfg");
    }

    @Test
    @Order(1)
    void listCustomers() {
        Awaitility.await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(100))
            .untilAsserted(() -> {
                List<CustomerDto> customers = customerService.listCustomers().collectList().block();

                assertNotNull(customers);
                assertThat(customers)
                    .hasSizeGreaterThanOrEqualTo(3)
                    .hasSizeLessThanOrEqualTo(4);
                assertThat(customers)
                    .extracting(CustomerDto::getCustomerName)
                    .contains("John Doe", "Fridolin Mann", "Hansjörg Riesen");
            });
    }

    @Test
    @Order(1)
    @Disabled("This test is disabled due to potential race conditions")
    void listCustomers2() {
        StepVerifier.create(customerService.listCustomers())
            .expectNextCount(3)
            .thenConsumeWhile(customer -> true, customer -> {
                assertThat(customer).isNotNull();
                assertThat(customer.getCustomerName()).isIn("John Doe", "Fridolin Mann", "Hansjörg Riesen");
            })
            .verifyComplete();
    }

    @Test
    @Order(2)
    void findFirstCustomerByName() {
        CustomerDto customer = customerMapper.customerToCustomerDto(getTestCustomer());
        customer.setCustomerName("customer to find");
        customerService.saveCustomer(Mono.just(customer)).block();

        Mono<CustomerDto> foundCustomer =  customerService.findFirstByCustomerName(customer.getCustomerName());

        AtomicBoolean waitingForSearch = new AtomicBoolean(false);
        AtomicReference<CustomerDto> waitingForSearchedCustomer = new AtomicReference<>();
        foundCustomer.subscribe(foundDto -> {
            System.out.println("Found Customer ID: " + foundDto.getId());
            waitingForSearch.set(true);
            waitingForSearchedCustomer.set(foundDto);
        });

        await().untilTrue(waitingForSearch);
        assertNotNull(waitingForSearchedCustomer.get());
        assertEquals(customer.getCustomerName(), waitingForSearchedCustomer.get().getCustomerName());
    }

    @Test
    @Order(2)
    @Disabled("This test is disabled due to potential race conditions")
    void findFirstCustomerByName2() {
        CustomerDto customer = customerMapper.customerToCustomerDto(getTestCustomer());
        customer.setCustomerName("customer to find");

        StepVerifier.create(customerService.saveCustomer(Mono.just(customer))
                .then(customerService.findFirstByCustomerName(customer.getCustomerName())))
            .assertNext(foundCustomer -> {
                assertThat(foundCustomer).isNotNull();
                assertThat(foundCustomer.getCustomerName()).isEqualTo(customer.getCustomerName());
                assertThat(foundCustomer.getId()).isNotNull();
                log.info("Found Customer ID: " + foundCustomer.getId());
            })
            .verifyComplete();
    }

    private static Customer getTestCustomer() {
        return Customer.builder()
            .customerName("Fridolina")
            .build();
    }
    
}
