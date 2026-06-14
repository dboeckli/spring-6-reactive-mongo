package guru.springframework.spring6reactivemongo.web.rest;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import guru.springframework.spring6reactivemongo.service.BeerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class BeerControllerTest {

    BeerService beerServiceMock;

    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        beerServiceMock = mock(BeerService.class);
        BeerController beerController = new BeerController(beerServiceMock);
        webTestClient = WebTestClient.bindToController(beerController).build();
    }

    @Test
    void testListBeersNoParams() {
        given(beerServiceMock.listBeers()).willReturn(Flux.just(BeerDto.builder().build()));

        webTestClient.get()
            .uri(BeerController.BEER_PATH)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .jsonPath("$.length()")
            .isEqualTo(1);
    }

    @Test
    void testListBeersByStyle() {
        given(beerServiceMock.findByBeerStyle(any())).willReturn(Flux.just(BeerDto.builder().build()));

        webTestClient.get()
            .uri(BeerController.BEER_PATH + "?beerStyle=IPA")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .jsonPath("$.length()")
            .isEqualTo(1);
    }

    @Test
    void testListBeersByStyleEmpty() {
        webTestClient.get().uri(BeerController.BEER_PATH + "?beerStyle=").exchange().expectStatus().isBadRequest();
    }

    @Test
    void testListBeersByStyleBlank() {
        webTestClient.get().uri(BeerController.BEER_PATH + "?beerStyle=   ").exchange().expectStatus().isBadRequest();
    }

    @Test
    void testListBeersByName() {
        given(beerServiceMock.findFirstByBeerName(any())).willReturn(Mono.just(BeerDto.builder().build()));

        webTestClient.get()
            .uri(BeerController.BEER_PATH + "?beerName=Test Beer")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .jsonPath("$.length()")
            .isEqualTo(1);
    }

    @Test
    void testListBeersByNameEmpty() {
        webTestClient.get().uri(BeerController.BEER_PATH + "?beerName=").exchange().expectStatus().isBadRequest();
    }

    @Test
    void testListBeersByNameBlank() {
        webTestClient.get().uri(BeerController.BEER_PATH + "?beerName=   ").exchange().expectStatus().isBadRequest();
    }

}
