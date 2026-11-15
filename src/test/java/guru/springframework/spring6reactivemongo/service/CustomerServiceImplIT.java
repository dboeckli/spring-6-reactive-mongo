package guru.springframework.spring6reactivemongo.service;

import guru.springframework.spring6reactivemongo.dto.CustomerDto;
import guru.springframework.spring6reactivemongo.mapper.CustomerMapper;
import guru.springframework.spring6reactivemongo.test.config.AuthServerDockerContainer;
import guru.springframework.spring6reactivemongo.test.config.MongoExtension;
import guru.springframework.spring6reactivemongo.test.config.TestMongoDockerContainer;
import lombok.extern.java.Log;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log
@Import({ TestMongoDockerContainer.class, AuthServerDockerContainer.class })
@ExtendWith(MongoExtension.class)
class CustomerServiceImplIT {

    @Autowired
    CustomerService customerService;

    @Autowired
    CustomerMapper customerMapper;

    @Test
    @Order(1)
    void listCustomers() {
        List<CustomerDto> customers = customerService.listCustomers().collectList().block();

        assertNotNull(customers);
        assertEquals(3, customers.size());
        assertThat(customers).extracting(CustomerDto::getCustomerName)
            .contains("John Doe", "Fridolin Mann", "Hansjörg Riesen");
    }

    @Test
    @Order(2)
    void findFirstCustomerByName() {
        Mono<CustomerDto> firstCustomer = customerService.findFirstByCustomerName("John Doe");

        assertNotNull(firstCustomer);
        assertEquals("John Doe", firstCustomer.block().getCustomerName());
    }

    @Test
    @Order(3)
    void patchCustomerWithEmptyNameKeepsExistingName() {
        CustomerDto savedCustomer = customerService
            .saveCustomer(Mono.just(CustomerDto.builder().customerName("Patch Me").build()))
            .block();

        CustomerDto patchedCustomer = customerService.patchCustomer(savedCustomer.getId(), new CustomerDto()).block();

        assertNotNull(patchedCustomer);
        assertEquals("Patch Me", patchedCustomer.getCustomerName());
    }

    @Test
    @Order(4)
    void patchCustomerWithNameUpdatesName() {
        CustomerDto savedCustomer = customerService
            .saveCustomer(Mono.just(CustomerDto.builder().customerName("Patch Me").build()))
            .block();

        CustomerDto patch = new CustomerDto();
        patch.setCustomerName("Patched Customer");

        CustomerDto patchedCustomer = customerService.patchCustomer(savedCustomer.getId(), patch).block();

        assertNotNull(patchedCustomer);
        assertEquals("Patched Customer", patchedCustomer.getCustomerName());
    }

}
