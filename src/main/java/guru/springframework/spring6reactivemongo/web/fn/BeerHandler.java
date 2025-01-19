package guru.springframework.spring6reactivemongo.web.fn;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import guru.springframework.spring6reactivemongo.service.BeerService;
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
public class BeerHandler {
    
    private final BeerService beerService;
    
    private Validator validator;
    
    public Mono<ServerResponse> listBeers(ServerRequest request) {
        Flux<BeerDto> beerDtoFlux;

        if (request.queryParam("beerStyle").isPresent()) {
            String beerStyle = request.queryParam("beerStyle")
                .filter(name -> !name.trim().isEmpty())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "BeerStyle must not be empty or contain only whitespace"));
            beerDtoFlux = beerService.findByBeerStyle(beerStyle);
        } else if (request.queryParam("beerName").isPresent()) {
            String beerName = request.queryParam("beerName")
                .filter(name -> !name.trim().isEmpty())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Beer name must not be empty or contain only whitespace"));
            Mono<BeerDto> firstBeerMono = beerService.findFirstByBeerName(beerName);
            beerDtoFlux = Flux.from(firstBeerMono);
        } else {
            beerDtoFlux = beerService.listBeers();
        }
        
        return ServerResponse.ok()
            .body(beerDtoFlux, BeerDto.class);
    }

    public Mono<ServerResponse> getBeerById(ServerRequest request){
        return ServerResponse
            .ok()
            .body(beerService.getById(request.pathVariable("beerId"))
                    .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND))),
                BeerDto.class);
    }

    public Mono<ServerResponse> createNewBeer(ServerRequest request){
        return beerService.saveBeer(request.bodyToMono(BeerDto.class).doOnNext(this::validate))
            .flatMap(beerDTO -> ServerResponse
                .created(UriComponentsBuilder
                    .fromPath(BeerRouterConfig.BEER_PATH_ID)
                    .build(beerDTO.getId()))
                .build());
    }

    public Mono<ServerResponse> updateBeerById(ServerRequest request) {
        return request.bodyToMono(BeerDto.class)
            .doOnNext(this::validate)
            .flatMap(beerDto -> beerService
                .updateBeer(request.pathVariable("beerId"), beerDto))
            .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND, "Beer not found")))
            .flatMap(savedDto -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> patchBeerById(ServerRequest request) {
        return request.bodyToMono(BeerDto.class)
            .doOnNext(this::validate)
            .flatMap(beerDto -> beerService
                .patchBeer(request.pathVariable("beerId"), beerDto))
            .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND, "Beer not found")))
            .flatMap(savedDto -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> deleteBeerById(ServerRequest request){
        return beerService.getById(request.pathVariable("beerId"))
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
            .flatMap(beerDTO -> beerService.deleteBeerById(beerDTO.getId()))
            .then(ServerResponse.noContent().build());
    }

    private void validate(BeerDto beerDto) {
        Errors errors = new BeanPropertyBindingResult(beerDto, BeerDto.class.getSimpleName());
        validator.validate(beerDto, errors);

        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());        }
    }
}
