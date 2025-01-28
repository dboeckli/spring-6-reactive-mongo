package guru.springframework.spring6reactivemongo.web.fn;

import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@AllArgsConstructor
public class CustomerRouterConfig {

    public static final String CUSTOMER_PATH = "/api/v3/customer";
    public static final String CUSTOMER_PATH_ID = CUSTOMER_PATH + "/{customerId}";

    private final CustomerHandler customerHandler;

    @Bean
    @RouterOperations({ 
        @RouterOperation(path = CUSTOMER_PATH, beanClass = CustomerHandler.class, beanMethod = "listCustomers"),
        @RouterOperation(path = CUSTOMER_PATH_ID, beanClass = CustomerHandler.class, beanMethod = "getCustomerById"),
        @RouterOperation(path = CUSTOMER_PATH, beanClass = CustomerHandler.class, beanMethod = "createCustomer"),
        @RouterOperation(path = CUSTOMER_PATH_ID, beanClass = CustomerHandler.class, beanMethod = "updateCustomerById"),
        @RouterOperation(path = CUSTOMER_PATH_ID, beanClass = CustomerHandler.class, beanMethod = "patchCustomerById"),
        @RouterOperation(path = CUSTOMER_PATH_ID, beanClass = CustomerHandler.class, beanMethod = "deleteCustomerById")
    })
    public RouterFunction<ServerResponse> customerRoutes() {
        return route()
            .GET(CUSTOMER_PATH, accept(APPLICATION_JSON), customerHandler::listCustomers)
            .GET(CUSTOMER_PATH_ID, accept(APPLICATION_JSON), customerHandler::getCustomerById)
            .POST(CUSTOMER_PATH, accept(APPLICATION_JSON), customerHandler::createCustomer)
            .PUT(CUSTOMER_PATH_ID, accept(APPLICATION_JSON), customerHandler::updateCustomerById)
            .PATCH(CUSTOMER_PATH_ID, accept(APPLICATION_JSON), customerHandler::patchCustomerById)
            .DELETE(CUSTOMER_PATH_ID, accept(APPLICATION_JSON), customerHandler::deleteCustomerById)
            .build();
    }    
}
