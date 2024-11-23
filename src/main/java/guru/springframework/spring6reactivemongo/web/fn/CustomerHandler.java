package guru.springframework.spring6reactivemongo.web.fn;

import guru.springframework.spring6reactivemongo.dto.CustomerDto;
import guru.springframework.spring6reactivemongo.service.CustomerService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
@AllArgsConstructor
public class CustomerHandler {

    private final CustomerService customerService;

    private Validator validator;

    public Mono<ServerResponse> listCustomers(ServerRequest request) {
        return ServerResponse.ok()
            .body(customerService.listCustomers(), CustomerDto.class);
    }

    public Mono<ServerResponse> getCustomerById(ServerRequest request){
        return ServerResponse
            .ok()
            .body(customerService.getById(request.pathVariable("customerId"))
                    .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND))),
                CustomerDto.class);
    }

    private void validate(CustomerDto customerDto) {
        Errors errors = new BeanPropertyBindingResult(customerDto, CustomerDto.class.getSimpleName());
        validator.validate(customerDto, errors);

        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());        }
    }
    
}
