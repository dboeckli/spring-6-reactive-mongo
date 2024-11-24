package guru.springframework.spring6reactivemongo.service;

import guru.springframework.spring6reactivemongo.dto.CustomerDto;
import guru.springframework.spring6reactivemongo.mapper.CustomerMapper;
import guru.springframework.spring6reactivemongo.repository.CustomerRepository;
import lombok.extern.java.Log;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log
class CustomerServiceImplTest {

    @Autowired
    CustomerService customerService;

    @Autowired
    CustomerMapper customerMapper;

    @Autowired
    CustomerRepository customerRepository;

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0.3")
        .withExposedPorts(27017)
        .withReuse(false);

    private static final String DATABASE_NAME = "sjdlfsjlfjsldjflsjfdldskjf";

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

    @BeforeEach
        // Workaround for MongoDB startup
    void startUp() {
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            log.info("Waiting for data to be inserted...");
            MatcherAssert.assertThat(customerRepository.count().block().intValue(), greaterThan(2));
        });
        log.info("Continue....");
    }


    @Test
    void listCustomers() {
        List<CustomerDto> customers = customerService.listCustomers().collectList().block();

        assertNotNull(customers);
        assertEquals(3, customers.size());
        assertThat(customers).extracting(CustomerDto::getCustomerName).contains("John Doe", "Fridolin Mann", "Hansjörg Riesen");
    }

    @Test
    void findFirstCustomerByName() {
        Mono<CustomerDto> firstCustomer =  customerService.findFirstByCustomerName("John Doe");
        
        assertNotNull(firstCustomer);
        assertEquals("John Doe", firstCustomer.block().getCustomerName());
    }
    
}
