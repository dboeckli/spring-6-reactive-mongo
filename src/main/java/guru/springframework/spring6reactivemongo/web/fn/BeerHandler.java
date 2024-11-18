package guru.springframework.spring6reactivemongo.web.fn;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import guru.springframework.spring6reactivemongo.service.BeerService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class BeerHandler {
    
    private final BeerService beerService;
    
    public Mono<ServerResponse> listBeers(ServerRequest request) {
        return ServerResponse.ok()
            .body(beerService.listBeers(), BeerDto.class);
    }

    public Mono<ServerResponse> getBeerById(ServerRequest request){
        return ServerResponse
            .ok()
            .body(beerService.getById(request.pathVariable("beerId")), BeerDto.class);
    }

    public Mono<ServerResponse> createNewBeer(ServerRequest request){
        return beerService.saveBeer(request.bodyToMono(BeerDto.class))
            .flatMap(beerDTO -> ServerResponse
                .created(UriComponentsBuilder
                    .fromPath(BeerRouterConfig.BEER_PATH_ID)
                    .build(beerDTO.getId()))
                .build());
    }

    public Mono<ServerResponse> updateBeerById(ServerRequest request) {
        return request.bodyToMono(BeerDto.class)
            .flatMap(beerDto -> beerService
                .updateBeer(request.pathVariable("beerId"), beerDto))
            .flatMap(savedDto -> ServerResponse.noContent().build());
    }
}
