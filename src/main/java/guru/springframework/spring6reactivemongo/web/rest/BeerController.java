package guru.springframework.spring6reactivemongo.web.rest;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import guru.springframework.spring6reactivemongo.service.BeerService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@Slf4j
public class BeerController {

    public static final String BEER_PATH = "/api/v3/beer";

    public static final String BEER_PATH_ID = BEER_PATH + "/{beerId}";

    private final BeerService beerService;

    @GetMapping(BEER_PATH)
    public Flux<BeerDto> listBeers(@RequestParam(required = false) String beerStyle,
            @RequestParam(required = false) String beerName) {
        if (beerStyle != null) {
            if (beerStyle.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "BeerStyle must not be empty or contain only whitespace");
            }
            return beerService.findByBeerStyle(beerStyle)
                .doOnSubscribe(s -> log.info("listBeers by style: {}", beerStyle));
        }

        if (beerName != null) {
            if (beerName.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Beer name must not be empty or contain only whitespace");
            }
            return Flux.from(beerService.findFirstByBeerName(beerName))
                .doOnSubscribe(s -> log.info("listBeers by name: {}", beerName));
        }

        return beerService.listBeers().doOnSubscribe(s -> log.info("listBeers"));
    }

    @GetMapping(BEER_PATH_ID)
    public Mono<BeerDto> getBeerById(@PathVariable String beerId) {
        return beerService.getById(beerId).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @PostMapping(BEER_PATH)
    public Mono<ResponseEntity<Void>> createNewBeer(@Valid @RequestBody BeerDto beerDto) {
        return beerService.saveBeer(Mono.just(beerDto))
            .map(savedDto -> ResponseEntity.created(UriComponentsBuilder.fromPath(BEER_PATH_ID).build(savedDto.getId()))
                .build());
    }

    @PutMapping(BEER_PATH_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> updateBeerById(@PathVariable String beerId, @Valid @RequestBody BeerDto beerDto) {
        return beerService.updateBeer(beerId, beerDto)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Beer not found")))
            .then();
    }

    @PatchMapping(BEER_PATH_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> patchBeerById(@PathVariable String beerId, @Valid @RequestBody BeerDto beerDto) {
        return beerService.patchBeer(beerId, beerDto)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Beer not found")))
            .then();
    }

    @DeleteMapping(BEER_PATH_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteBeerById(@PathVariable String beerId) {
        return beerService.getById(beerId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
            .flatMap(beerDto -> beerService.deleteBeerById(beerDto.getId()));
    }

}
