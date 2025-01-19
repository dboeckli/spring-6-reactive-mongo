package guru.springframework.spring6reactivemongo.web.fn;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import guru.springframework.spring6reactivemongo.service.BeerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.Validator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class BeerHandlerTest {

    BeerService beerServiceMock;
    Validator validatorMock;

    BeerRouterConfig beerRouterConfig;
    
    BeerHandler beerHandler;
    
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        beerServiceMock = mock(BeerService.class);
        validatorMock = mock(Validator.class);
        
        beerHandler = new BeerHandler(beerServiceMock, validatorMock);
        beerRouterConfig = new BeerRouterConfig(beerHandler);

        webTestClient = WebTestClient.bindToRouterFunction(beerRouterConfig.beerRoutes()).build();
    }

    @Test
    void testListBeersNoParams() {
        given(beerServiceMock.listBeers()).willReturn(Flux.just(BeerDto.builder().build()));

        webTestClient.get().uri(BeerRouterConfig.BEER_PATH)
            .exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$.length()").isEqualTo(1);
    }

    @Test
    void testListBeersByStyle() {
        given(beerServiceMock.findByBeerStyle(any())).willReturn(Flux.just(BeerDto.builder().build()));

        webTestClient.get().uri(BeerRouterConfig.BEER_PATH + "?beerStyle=IPA")
            .exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$.length()").isEqualTo(1);
    }

    @Test
    void testListBeersByStyleEmpty() {
        webTestClient.get().uri(BeerRouterConfig.BEER_PATH + "?beerStyle=")
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void testListBeersByStyleBlank() {
        webTestClient.get().uri(BeerRouterConfig.BEER_PATH + "?beerStyle=   ")
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void testListBeersByName() {
        given(beerServiceMock.findFirstByBeerName(any())).willReturn(Mono.just(BeerDto.builder().build()));

        webTestClient.get().uri(BeerRouterConfig.BEER_PATH + "?beerName=Test Beer")
            .exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$.length()").isEqualTo(1);
    }

    @Test
    void testListBeersByNameEmpty() {
        webTestClient.get().uri(BeerRouterConfig.BEER_PATH + "?beerName=")
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void testListBeersByNameBlank() {
        webTestClient.get().uri(BeerRouterConfig.BEER_PATH + "?beerName=   ")
            .exchange()
            .expectStatus().isBadRequest();
    }
}
