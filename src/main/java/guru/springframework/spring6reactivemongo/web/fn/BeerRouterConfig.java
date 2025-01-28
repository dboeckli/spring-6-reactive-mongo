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
public class BeerRouterConfig {

    public static final String BEER_PATH = "/api/v3/beer";
    public static final String BEER_PATH_ID = BEER_PATH + "/{beerId}";

    private final BeerHandler beerHandler;

    @Bean
    @RouterOperations({
        @RouterOperation(path = BEER_PATH, beanClass = BeerHandler.class, beanMethod = "listBeers"),
        @RouterOperation(path = BEER_PATH_ID, beanClass = BeerHandler.class, beanMethod = "getBeerById"),
        @RouterOperation(path = BEER_PATH, beanClass = BeerHandler.class, beanMethod = "createNewBeer"),
        @RouterOperation(path = BEER_PATH_ID, beanClass = BeerHandler.class, beanMethod = "updateBeerById"),
        @RouterOperation(path = BEER_PATH_ID, beanClass = BeerHandler.class, beanMethod = "patchBeerById"),
        @RouterOperation(path = BEER_PATH_ID, beanClass = BeerHandler.class, beanMethod = "deleteBeerById")
    })
    public RouterFunction<ServerResponse> beerRoutes() {
        return route()
            .GET(BEER_PATH, accept(APPLICATION_JSON), beerHandler::listBeers)
            .GET(BEER_PATH_ID, accept(APPLICATION_JSON), beerHandler::getBeerById)
            .POST(BEER_PATH, accept(APPLICATION_JSON), beerHandler::createNewBeer)
            .PUT(BEER_PATH_ID, accept(APPLICATION_JSON), beerHandler::updateBeerById)
            .PATCH(BEER_PATH_ID, accept(APPLICATION_JSON), beerHandler::patchBeerById)
            .DELETE(BEER_PATH_ID, accept(APPLICATION_JSON), beerHandler::deleteBeerById)
            .build();
    }
    
}
