package guru.springframework.spring6reactivemongo.web.fn;

import guru.springframework.spring6reactivemongo.dto.CustomerDto;
import guru.springframework.spring6reactivemongo.repository.CustomerRepository;
import lombok.extern.java.Log;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log
class CustomerHandlerTest {
    
    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    WebTestClient webTestClient;

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0.3")
        .withExposedPorts(27017)
        .withReuse(false);

    private static final String DATABASE_NAME = CustomerHandlerTest.class.getSimpleName();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.database", () -> DATABASE_NAME); // Replace with your desired database name
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl(DATABASE_NAME));
        //registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
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
    
    @BeforeEach
    // Workaround for MongoDB startup
    void startUp() {
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            log.info("Waiting for data to be inserted...");
            assertThat(customerRepository.count().block().intValue(), greaterThan(2));
        });
        log.info("Continue....");
    }

    @Test
    @Order(1)
    void listCustomers() {
        webTestClient.get().uri(CustomerRouterConfig.CUSTOMER_PATH)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            .expectBody().jsonPath("$.size()").value(equalTo(3));
    }

    @Test
    @Order(1)
    void testListCustomers2() {
        webTestClient.get().uri(CustomerRouterConfig.CUSTOMER_PATH)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            .expectBodyList(CustomerDto.class).hasSize(3);
    }

    @Test
    @Order(2)
    void getCustomerById() {
        CustomerDto givenCustomer = getAnyExistingCustomer();

        CustomerDto gotCustomer = webTestClient.get().uri(CustomerRouterConfig.CUSTOMER_PATH_ID, givenCustomer.getId())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            .expectBody(CustomerDto.class).returnResult().getResponseBody();

        assertEquals(givenCustomer.getId(), gotCustomer.getId());
    }

    @Test
    @Order(3)
    void testCreateCustomer() {
        CustomerDto customerToCreate = CustomerDto.builder().customerName("New Customer").build();

        String location = webTestClient.post().uri(CustomerRouterConfig.CUSTOMER_PATH)
            .body(Mono.just(customerToCreate), CustomerDto.class)
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().valueMatches("location", "/api/v3/customer/[a-f0-9]{24}$")
            .returnResult(CustomerDto.class)
            .getResponseHeaders()
            .getLocation()
            .toString();

        System.out.println("Location: " + location);
        assertNotNull(location);

        CustomerDto createdCustomer = getCustomerByLocation(location);
        assertNotNull(createdCustomer);
    }

    @Test
    @Order(3)
    void testCreateCustomerEmptyName() {
        CustomerDto customerToCreate = CustomerDto.builder().customerName("").build();

        webTestClient.post().uri(CustomerRouterConfig.CUSTOMER_PATH)
            .body(Mono.just(customerToCreate), CustomerDto.class)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @Order(3)
    void testCreateCustomerTooShortName() {
        CustomerDto customerToCreate = CustomerDto.builder().customerName("1").build();

        webTestClient.post().uri(CustomerRouterConfig.CUSTOMER_PATH)
            .body(Mono.just(customerToCreate), CustomerDto.class)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @Order(3)
    void testCreateCustomerNullName() {
        CustomerDto customerToCreate = CustomerDto.builder().customerName(null).build();

        webTestClient.post().uri(CustomerRouterConfig.CUSTOMER_PATH)
            .body(Mono.just(customerToCreate), CustomerDto.class)
            .exchange()
            .expectStatus().isBadRequest();
    }

    private CustomerDto getCustomerByLocation(String location) {
        return webTestClient.get().uri(location)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            .expectBody(CustomerDto.class).returnResult().getResponseBody();
    }

    private CustomerDto getAnyExistingCustomer() {
        return webTestClient.get().uri(CustomerRouterConfig.CUSTOMER_PATH)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            .expectBodyList(CustomerDto.class)
            .returnResult()
            .getResponseBody()
            .stream()
            .findFirst()
            .orElse(null);
    }
}
