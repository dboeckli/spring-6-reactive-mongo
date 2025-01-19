package guru.springframework.spring6reactivemongo.web.fn;

import guru.springframework.spring6reactivemongo.dto.CustomerDto;
import guru.springframework.spring6reactivemongo.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.Validator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class CustomerHandlerTest {

    CustomerService customerServiceMock;
    Validator validatorMock;

    CustomerRouterConfig customerRouterConfig;

    CustomerHandler customerHandler;

    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        customerServiceMock = mock(CustomerService.class);
        validatorMock = mock(Validator.class);

        customerHandler = new CustomerHandler(customerServiceMock, validatorMock);
        customerRouterConfig = new CustomerRouterConfig(customerHandler);

        webTestClient = WebTestClient.bindToRouterFunction(customerRouterConfig.customerRoutes()).build();
    }

    @Test
    void testListCustomersNoParams() {
        CustomerDto customer1 = CustomerDto.builder().id("1").customerName("Customer 1").build();
        CustomerDto customer2 = CustomerDto.builder().id("2").customerName("Customer 2").build();

        given(customerServiceMock.listCustomers()).willReturn(Flux.just(customer1, customer2));

        webTestClient.get().uri(CustomerRouterConfig.CUSTOMER_PATH)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.size()").isEqualTo(2)
            .jsonPath("$[0].id").isEqualTo("1")
            .jsonPath("$[0].customerName").isEqualTo("Customer 1")
            .jsonPath("$[1].id").isEqualTo("2")
            .jsonPath("$[1].customerName").isEqualTo("Customer 2");
    }

    @Test
    void testListCustomersByName() {
        CustomerDto customer = CustomerDto.builder().id("1").customerName("Test Customer").build();

        given(customerServiceMock.findFirstByCustomerName(any())).willReturn(Mono.just(customer));

        webTestClient.get().uri(uriBuilder ->
                uriBuilder.path(CustomerRouterConfig.CUSTOMER_PATH)
                    .queryParam("customerName", "Test Customer")
                    .build())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.size()").isEqualTo(1)
            .jsonPath("$[0].id").isEqualTo("1")
            .jsonPath("$[0].customerName").isEqualTo("Test Customer");
    }

    @Test
    void testListCustomersByNameNotFound() {
        given(customerServiceMock.findFirstByCustomerName(any())).willReturn(Mono.empty());

        webTestClient.get().uri(uriBuilder ->
                uriBuilder.path(CustomerRouterConfig.CUSTOMER_PATH)
                    .queryParam("customerName", "Non-existent Customer")
                    .build())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.size()").isEqualTo(0);
    }

    @Test
    void testListCustomersEmptyName() {
        webTestClient.get().uri(uriBuilder ->
                uriBuilder.path(CustomerRouterConfig.CUSTOMER_PATH)
                    .queryParam("customerName", "")
                    .build())
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void testListCustomersByNameEmpty() {
        webTestClient.get().uri(CustomerRouterConfig.CUSTOMER_PATH + "?customerName=")
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void testListCustomersByNameBlank() {
        webTestClient.get().uri(CustomerRouterConfig.CUSTOMER_PATH + "?customerName=   ")
            .exchange()
            .expectStatus().isBadRequest();
    }

    
}
