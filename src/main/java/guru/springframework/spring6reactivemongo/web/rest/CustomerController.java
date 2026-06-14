package guru.springframework.spring6reactivemongo.web.rest;

import guru.springframework.spring6reactivemongo.dto.CustomerDto;
import guru.springframework.spring6reactivemongo.service.CustomerService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class CustomerController {

    public static final String CUSTOMER_PATH = "/api/v3/customer";

    public static final String CUSTOMER_PATH_ID = CUSTOMER_PATH + "/{customerId}";

    private final CustomerService customerService;

    @GetMapping(CUSTOMER_PATH)
    public Flux<CustomerDto> listCustomers(@RequestParam(required = false) String customerName) {
        if (customerName != null) {
            if (customerName.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Customer name must not be empty or contain only whitespace");
            }
            return Flux.from(customerService.findFirstByCustomerName(customerName));
        }

        return customerService.listCustomers();
    }

    @GetMapping(CUSTOMER_PATH_ID)
    public Mono<CustomerDto> getCustomerById(@PathVariable String customerId) {
        return customerService.getById(customerId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @PostMapping(CUSTOMER_PATH)
    public Mono<ResponseEntity<Void>> createCustomer(@Valid @RequestBody CustomerDto customerDto) {
        return customerService.saveCustomer(Mono.just(customerDto))
            .map(savedDto -> ResponseEntity
                .created(UriComponentsBuilder.fromPath(CUSTOMER_PATH_ID).build(savedDto.getId()))
                .build());
    }

    @PutMapping(CUSTOMER_PATH_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> updateCustomerById(@PathVariable String customerId, @Valid @RequestBody CustomerDto customerDto) {
        return customerService.updateCustomer(customerId, customerDto)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found")))
            .then();
    }

    @PatchMapping(CUSTOMER_PATH_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> patchCustomerById(@PathVariable String customerId, @Valid @RequestBody CustomerDto customerDto) {
        return customerService.patchCustomer(customerId, customerDto)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found")))
            .then();
    }

    @DeleteMapping(CUSTOMER_PATH_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteCustomerById(@PathVariable String customerId) {
        return customerService.getById(customerId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
            .flatMap(customerDto -> customerService.deleteCustomerById(customerDto.getId()));
    }

}
