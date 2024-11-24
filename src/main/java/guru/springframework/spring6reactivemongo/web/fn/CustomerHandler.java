package guru.springframework.spring6reactivemongo.web.fn;

import guru.springframework.spring6reactivemongo.dto.CustomerDto;
import guru.springframework.spring6reactivemongo.service.CustomerService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
@AllArgsConstructor
public class CustomerHandler {

    private final CustomerService customerService;

    private Validator validator;

    public Mono<ServerResponse> listCustomers(ServerRequest request) {
        Flux<CustomerDto> customerDtoFlux;

        if (request.queryParam("customerName").isPresent()) {
            Mono<CustomerDto> firstCustomerMono = customerService.findFirstByCustomerName(request.queryParam("customerName").get());
            customerDtoFlux = Flux.from(firstCustomerMono);
        } else {
            customerDtoFlux = customerService.listCustomers();
        }
        return ServerResponse.ok()
            .body(customerDtoFlux, CustomerDto.class);
    }

    public Mono<ServerResponse> getCustomerById(ServerRequest request){
        return ServerResponse
            .ok()
            .body(customerService.getById(request.pathVariable("customerId"))
                    .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND))),
                CustomerDto.class);
    }
    
    public Mono<ServerResponse> createCustomer(ServerRequest request) {
        return customerService.saveCustomer(request.bodyToMono(CustomerDto.class).doOnNext(customerDto -> validate(customerDto)))
            .flatMap(customerDTO -> ServerResponse
                .created(UriComponentsBuilder
                    .fromPath(CustomerRouterConfig.CUSTOMER_PATH_ID)
                    .build(customerDTO.getId()))
                .build());
    }

    public Mono<ServerResponse> updateCustomerById(ServerRequest request) {
        return request.bodyToMono(CustomerDto.class)
            .doOnNext(customerDTO -> validate(customerDTO))
            .flatMap(customerDTO -> customerService
                .updateCustomer(request.pathVariable("customerId"), customerDTO))
            .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND, "Customer not found")))
            .flatMap(savedDto -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> patchCustomerById(ServerRequest request) {
        return request.bodyToMono(CustomerDto.class)
            .doOnNext(customerDTO -> validate(customerDTO))
            .flatMap(customerDTO -> customerService
                .patchCustomer(request.pathVariable("customerId"), customerDTO))
            .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND, "Customer not found")))
            .flatMap(savedDto -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> deleteCustomerById(ServerRequest request){
        return customerService.getById(request.pathVariable("customerId"))
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
            .flatMap(customerDTO -> customerService.deleteCustomerById(customerDTO.getId()))
            .then(ServerResponse.noContent().build());
    }

    private void validate(CustomerDto customerDto) {
        Errors errors = new BeanPropertyBindingResult(customerDto, CustomerDto.class.getSimpleName());
        validator.validate(customerDto, errors);

        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());        }
    }
    
}
