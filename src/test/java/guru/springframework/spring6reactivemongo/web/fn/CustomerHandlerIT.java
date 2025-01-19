package guru.springframework.spring6reactivemongo.web.fn;

import guru.springframework.spring6reactivemongo.dto.CustomerDto;
import guru.springframework.spring6reactivemongo.test.config.MongoExtension;
import guru.springframework.spring6reactivemongo.test.config.TestMongoDockerContainer;
import lombok.extern.java.Log;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOAuth2Login;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log
@Import(TestMongoDockerContainer.class)
@ExtendWith(MongoExtension.class)
class CustomerHandlerIT {
    
    @Autowired
    WebTestClient webTestClient;

    @Test
    @Order(1)
    void testListCustomers() {
        webTestClient
            .mutateWith(mockOAuth2Login())
            .get().uri(CustomerRouterConfig.CUSTOMER_PATH)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            .expectBody().jsonPath("$.size()").value(equalTo(3));
    }

    @Test
    @Order(1)
    void testListCustomers2() {
        webTestClient
            .mutateWith(mockOAuth2Login())
            .get().uri(CustomerRouterConfig.CUSTOMER_PATH)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            .expectBodyList(CustomerDto.class).hasSize(3);
    }

    @Test
    @Order(1)
    void testFindFirstCustomerByCustomerName() {
        CustomerDto existingCustomer = getAnyExistingCustomer();

        webTestClient
            .mutateWith(mockOAuth2Login())
            .get().uri(UriComponentsBuilder
                .fromPath(CustomerRouterConfig.CUSTOMER_PATH)
                .queryParam("customerName", existingCustomer.getCustomerName()).build().toUri())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            .expectBody().jsonPath("$.size()").value(equalTo(1));
    }

    @Test
    @Order(2)
    void testGetCustomerById() {
        CustomerDto givenCustomer = getAnyExistingCustomer();

        CustomerDto gotCustomer = webTestClient
            .mutateWith(mockOAuth2Login())
            .get().uri(CustomerRouterConfig.CUSTOMER_PATH_ID, givenCustomer.getId())
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

        String location = webTestClient
            .mutateWith(mockOAuth2Login())
            .post().uri(CustomerRouterConfig.CUSTOMER_PATH)
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

        webTestClient
            .mutateWith(mockOAuth2Login())
            .post().uri(CustomerRouterConfig.CUSTOMER_PATH)
            .body(Mono.just(customerToCreate), CustomerDto.class)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @Order(3)
    void testCreateCustomerTooShortName() {
        CustomerDto customerToCreate = CustomerDto.builder().customerName("1").build();

        webTestClient
            .mutateWith(mockOAuth2Login())
            .post().uri(CustomerRouterConfig.CUSTOMER_PATH)
            .body(Mono.just(customerToCreate), CustomerDto.class)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @Order(3)
    void testCreateCustomerNullName() {
        CustomerDto customerToCreate = CustomerDto.builder().customerName(null).build();

        webTestClient
            .mutateWith(mockOAuth2Login())
            .post().uri(CustomerRouterConfig.CUSTOMER_PATH)
            .body(Mono.just(customerToCreate), CustomerDto.class)
            .exchange()
            .expectStatus().isBadRequest();
    }

    private CustomerDto getCustomerByLocation(String location) {
        return webTestClient
            .mutateWith(mockOAuth2Login())
            .get().uri(location)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            .expectBody(CustomerDto.class).returnResult().getResponseBody();
    }
    

    @Test
    @Order(4)
    void testUpdateCustomer() {
        CustomerDto customerToUpdate = this.getAnyExistingCustomer();
        customerToUpdate.setCustomerName("UpdatedCustomer");

        webTestClient
            .mutateWith(mockOAuth2Login())
            .put()
            .uri(CustomerRouterConfig.CUSTOMER_PATH_ID, customerToUpdate.getId())
            .body(Mono.just(customerToUpdate), CustomerDto.class)
            .exchange()
            .expectStatus().isNoContent();

        CustomerDto updatedCustomer = getCustomerById(customerToUpdate.getId());
        assertNotNull(updatedCustomer);
        assertEquals(customerToUpdate.getId(), updatedCustomer.getId());
        assertEquals(customerToUpdate.getCustomerName(), updatedCustomer.getCustomerName());
    }

    @Test
    @Order(4)
    void testUpdateCustomerToShortName() {
        CustomerDto customerToUpdate = this.getAnyExistingCustomer();
        customerToUpdate.setCustomerName("1");

        webTestClient
            .mutateWith(mockOAuth2Login())
            .put()
            .uri(CustomerRouterConfig.CUSTOMER_PATH_ID, customerToUpdate.getId())
            .body(Mono.just(customerToUpdate), CustomerDto.class)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @Order(4)
    void testUpdateCustomerCustomerNotFound() {
        CustomerDto customerToUpdate = this.getAnyExistingCustomer();
        customerToUpdate.setCustomerName("updatedCustomer");
        customerToUpdate.setId("9999");

        webTestClient
            .mutateWith(mockOAuth2Login())
            .put()
            .uri(CustomerRouterConfig.CUSTOMER_PATH_ID, customerToUpdate.getId())
            .body(Mono.just(customerToUpdate), CustomerDto.class)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    @Order(5)
    void testPatchCustomer() {
        CustomerDto customerToUpdate = this.getAnyExistingCustomer();
        customerToUpdate.setCustomerName("PatchedCustomer");

        webTestClient
            .mutateWith(mockOAuth2Login())
            .patch()
            .uri(CustomerRouterConfig.CUSTOMER_PATH_ID, customerToUpdate.getId())
            .body(Mono.just(customerToUpdate), CustomerDto.class)
            .exchange()
            .expectStatus().isNoContent();

        CustomerDto updatedCustomer = getCustomerById(customerToUpdate.getId());
        assertNotNull(updatedCustomer);
        assertEquals(customerToUpdate.getId(), updatedCustomer.getId());
        assertEquals(customerToUpdate.getCustomerName(), updatedCustomer.getCustomerName());
    }

    @Test
    @Order(5)
    void testPatchCustomerEmtpyName() {
        CustomerDto customerToUpdate = this.getAnyExistingCustomer();
        customerToUpdate.setCustomerName("");

        webTestClient
            .mutateWith(mockOAuth2Login())
            .patch()
            .uri(CustomerRouterConfig.CUSTOMER_PATH_ID, customerToUpdate.getId())
            .body(Mono.just(customerToUpdate), CustomerDto.class)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @Order(5)
    void testPatchCustomerTooShortName() {
        CustomerDto customerToUpdate = this.getAnyExistingCustomer();
        customerToUpdate.setCustomerName("1");

        webTestClient
            .mutateWith(mockOAuth2Login())
            .patch()
            .uri(CustomerRouterConfig.CUSTOMER_PATH_ID, customerToUpdate.getId())
            .body(Mono.just(customerToUpdate), CustomerDto.class)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @Order(5)
    void testPatchCustomerNullNameNotFound() {
        CustomerDto customerToUpdate = this.getAnyExistingCustomer();
        customerToUpdate.setId("999999");

        webTestClient
            .mutateWith(mockOAuth2Login())
            .patch()
            .uri(CustomerRouterConfig.CUSTOMER_PATH_ID, customerToUpdate.getId())
            .body(Mono.just(customerToUpdate), CustomerDto.class)
            .exchange()
            .expectStatus().isNotFound();
    }
    
    private CustomerDto getCustomerById(String id) {
        try {
            return webTestClient
                .mutateWith(mockOAuth2Login())
                .get().uri(CustomerRouterConfig.CUSTOMER_PATH_ID, id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody(CustomerDto.class).returnResult().getResponseBody();
        } catch (AssertionError ex) {
            if (ex.getMessage().contains("Status expected:<200 OK> but was:<404 NOT_FOUND>")) {
                return null;
            }
            throw ex;
        }
    }

    @Test
    @Order(99)
    void testDeleteCustomer() {
        CustomerDto customerToDelete = this.getAnyExistingCustomer();

        webTestClient
            .mutateWith(mockOAuth2Login())
            .delete()
            .uri(CustomerRouterConfig.CUSTOMER_PATH_ID, customerToDelete.getId())
            .exchange()
            .expectStatus().isNoContent();

        CustomerDto deletedCustomer = getCustomerById(customerToDelete.getId());
        assertNull(deletedCustomer);
    }

    @Test
    @Order(99)
    void testDeleteCustomerNotFound() {
        CustomerDto customerToDelete = this.getAnyExistingCustomer();
        customerToDelete.setId("88888888888888");

        webTestClient
            .mutateWith(mockOAuth2Login())
            .delete()
            .uri(CustomerRouterConfig.CUSTOMER_PATH_ID, customerToDelete.getId())
            .exchange()
            .expectStatus().isNotFound();
    }

    private CustomerDto getAnyExistingCustomer() {
        return webTestClient
            .mutateWith(mockOAuth2Login())
            .get().uri(CustomerRouterConfig.CUSTOMER_PATH)
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
