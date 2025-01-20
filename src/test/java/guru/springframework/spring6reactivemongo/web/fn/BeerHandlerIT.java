package guru.springframework.spring6reactivemongo.web.fn;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import guru.springframework.spring6reactivemongo.test.config.MongoExtension;
import guru.springframework.spring6reactivemongo.test.config.TestMongoDockerContainer;
import lombok.extern.java.Log;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOAuth2Login;

@Testcontainers
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log
@Import(TestMongoDockerContainer.class)
@ExtendWith(MongoExtension.class)
class BeerHandlerIT {

    @Autowired
    WebTestClient webTestClient;

    @Test
    @Order(1)
    void testListBeers() {
        webTestClient
            .mutateWith(mockOAuth2Login())
            .get().uri(BeerRouterConfig.BEER_PATH)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            //.expectBody(BeerDto.class).isEqualTo(BeerDto.builder().beerName("Galaxy Cat").build())
            .expectBody().jsonPath("$.size()").value(equalTo(3));
    }

    @Test
    @Order(1)
    void testListBeers2() {
        webTestClient
            .mutateWith(mockOAuth2Login())
            .get().uri(BeerRouterConfig.BEER_PATH)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            //.expectBody().jsonPath("$.length()").isEqualTo(3)
            .expectBodyList(BeerDto.class).hasSize(3);
    }

    @Test
    @Order(1)
    void testListBeersByBeerStyle() {
        BeerDto existingBeer = getAnyExistingBeer();
        int expectedBeerCount;
        if (existingBeer.getBeerStyle().equals("IPA")) {
            expectedBeerCount = 1;
        } else {
            expectedBeerCount = 2;
        }

        webTestClient
            .mutateWith(mockOAuth2Login())
            .get().uri(UriComponentsBuilder
                .fromPath(BeerRouterConfig.BEER_PATH)
                .queryParam("beerStyle", existingBeer.getBeerStyle()).build().toUri())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            .expectBody().jsonPath("$.size()").value(equalTo(expectedBeerCount));
    }

    @Test
    @Order(1)
    void testFindFirstBeerByBeerName() {
        BeerDto existingBeer = getAnyExistingBeer();

        webTestClient
            .mutateWith(mockOAuth2Login())
            .get().uri(UriComponentsBuilder
                .fromPath(BeerRouterConfig.BEER_PATH)
                .queryParam("beerName", existingBeer.getBeerName()).build().toUri())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            .expectBody().jsonPath("$.size()").value(equalTo(1));
    }


    @Test
    @Order(2)
    void testGetBeerById() {
        BeerDto givenBeer = getAnyExistingBeer();
        
        BeerDto gotBeer = webTestClient
            .mutateWith(mockOAuth2Login())
            .get().uri(BeerRouterConfig.BEER_PATH_ID, givenBeer.getId())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            .expectBody(BeerDto.class).returnResult().getResponseBody();
        
        assertEquals(givenBeer.getId(), gotBeer.getId());
    }

    @Test
    @Order(2)
    void testGetBeerByIdNotFound() {
        BeerDto givenBeer = getAnyExistingBeer();
        givenBeer.setId("abcd_DOES_NOT_EXIST");

        webTestClient
            .mutateWith(mockOAuth2Login())
            .get().uri(BeerRouterConfig.BEER_PATH_ID, givenBeer.getId())
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    @Order(3)
    void testCreateBeer() {
        BeerDto beerToCreate = BeerDto.builder().beerName("New Beer").build();

        String location = webTestClient
            .mutateWith(mockOAuth2Login())
            .post().uri(BeerRouterConfig.BEER_PATH)
            .body(Mono.just(beerToCreate), BeerDto.class)
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().valueMatches("location", "/api/v3/beer/[a-f0-9]{24}$")
            .returnResult(BeerDto.class)
            .getResponseHeaders()
            .getLocation()
            .toString();

        log.info("Location: " + location);
        assertNotNull(location);
        
        BeerDto createdBeer = getBeerByLocation(location);
        assertNotNull(createdBeer);
    }

    @Test
    @Order(3)
    void testCreateBeerBeerNameExactly3Characters() {
        BeerDto beerToCreate = BeerDto.builder().beerName("123").build();

        String location = webTestClient
            .mutateWith(mockOAuth2Login())
            .post().uri(BeerRouterConfig.BEER_PATH)
            .body(Mono.just(beerToCreate), BeerDto.class)
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().valueMatches("location", "/api/v3/beer/[a-f0-9]{24}$")
            .returnResult(BeerDto.class)
            .getResponseHeaders()
            .getLocation()
            .toString();

        log.info("Location: " + location);
        assertNotNull(location);

        BeerDto createdBeer = getBeerByLocation(location);
        assertNotNull(createdBeer);
    }

    @Test
    @Order(3)
    void testCreateBeerEmptyBeerName() {
        BeerDto beerToCreate = BeerDto.builder().beerName("").build();

        webTestClient
            .mutateWith(mockOAuth2Login())
            .post().uri(BeerRouterConfig.BEER_PATH)
            .body(Mono.just(beerToCreate), BeerDto.class)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @Order(3)
    void testCreateBeerNullBeerName() {
        BeerDto beerToCreate = BeerDto.builder().beerName(null).build();

        webTestClient
            .mutateWith(mockOAuth2Login())
            .post().uri(BeerRouterConfig.BEER_PATH)
            .body(Mono.just(beerToCreate), BeerDto.class)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @Order(3)
    void testCreateBeerToShortBeerName() {
        BeerDto beerToCreate = BeerDto.builder().beerName("12").build();

        webTestClient
            .mutateWith(mockOAuth2Login())
            .post().uri(BeerRouterConfig.BEER_PATH)
            .body(Mono.just(beerToCreate), BeerDto.class)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @Order(3)
    void testCreateBeerToShortBeerStyle() {
        BeerDto beerToCreate = BeerDto.builder()
            .beerName("123")
            .beerStyle("")
            .build();

        webTestClient
            .mutateWith(mockOAuth2Login())
            .post().uri(BeerRouterConfig.BEER_PATH)
            .body(Mono.just(beerToCreate), BeerDto.class)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @Order(4)
    void testUpdateBeer() {
        BeerDto beerToUpdate = this.getAnyExistingBeer();
        beerToUpdate.setBeerName("UpdatedBeer");

        webTestClient
            .mutateWith(mockOAuth2Login())
            .put()
            .uri(BeerRouterConfig.BEER_PATH_ID, beerToUpdate.getId())
            .body(Mono.just(beerToUpdate), BeerDto.class)
            .exchange()
            .expectStatus().isNoContent();

        BeerDto updatedBeer = getBeerById(beerToUpdate.getId());
        assertNotNull(updatedBeer);
        assertEquals(beerToUpdate.getId(), updatedBeer.getId());
        assertEquals(beerToUpdate.getBeerName(), updatedBeer.getBeerName());
    }

    @Test
    @Order(4)
    void testUpdateBeerToShortBeerName() {
        BeerDto beerToUpdate = this.getAnyExistingBeer();
        beerToUpdate.setBeerName("12");

        webTestClient
            .mutateWith(mockOAuth2Login())
            .put()
            .uri(BeerRouterConfig.BEER_PATH_ID, beerToUpdate.getId())
            .body(Mono.just(beerToUpdate), BeerDto.class)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @Order(4)
    void testUpdateBeerToShortBeerStyle() {
        BeerDto beerToUpdate = this.getAnyExistingBeer();
        beerToUpdate.setBeerName("123");
        beerToUpdate.setBeerName("");

        webTestClient
            .mutateWith(mockOAuth2Login())
            .put()
            .uri(BeerRouterConfig.BEER_PATH_ID, beerToUpdate.getId())
            .body(Mono.just(beerToUpdate), BeerDto.class)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @Order(4)
    void testUpdateBeerNotFound() {
        BeerDto beerToUpdate = this.getAnyExistingBeer();
        beerToUpdate.setBeerName("UpdatedBeer");
        beerToUpdate.setId("9999");

        webTestClient
            .mutateWith(mockOAuth2Login())
            .put()
            .uri(BeerRouterConfig.BEER_PATH_ID, beerToUpdate.getId())
            .body(Mono.just(beerToUpdate), BeerDto.class)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    @Order(5)
    void testPatchBeer() {
        BeerDto beerToUpdate = this.getAnyExistingBeer();
        beerToUpdate.setBeerName("PatchedBeer");

        webTestClient
            .mutateWith(mockOAuth2Login())
            .patch()
            .uri(BeerRouterConfig.BEER_PATH_ID, beerToUpdate.getId())
            .body(Mono.just(beerToUpdate), BeerDto.class)
            .exchange()
            .expectStatus().isNoContent();


        BeerDto updatedBeer = getBeerById(beerToUpdate.getId());
        assertNotNull(updatedBeer);
        assertEquals(beerToUpdate.getId(), updatedBeer.getId());
        assertEquals(beerToUpdate.getBeerName(), updatedBeer.getBeerName());
    }

    @Test
    @Order(5)
    void testPatchBeerToShortBeerName() {
        BeerDto beerToUpdate = this.getAnyExistingBeer();
        beerToUpdate.setBeerName("12");

        webTestClient
            .mutateWith(mockOAuth2Login())
            .patch()
            .uri(BeerRouterConfig.BEER_PATH_ID, beerToUpdate.getId())
            .body(Mono.just(beerToUpdate), BeerDto.class)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @Order(5)
    void testPatchBeerToShortBeerStyle() {
        BeerDto beerToUpdate = this.getAnyExistingBeer();
        beerToUpdate.setBeerName("123");
        beerToUpdate.setBeerStyle("");

        webTestClient
            .mutateWith(mockOAuth2Login())
            .patch()
            .uri(BeerRouterConfig.BEER_PATH_ID, beerToUpdate.getId())
            .body(Mono.just(beerToUpdate), BeerDto.class)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @Order(5)
    void testPatchBeerNotFound() {
        BeerDto beerToUpdate = this.getAnyExistingBeer();
        beerToUpdate.setBeerName("PatchedBeer");
        beerToUpdate.setId("8888");

        webTestClient
            .mutateWith(mockOAuth2Login())
            .patch()
            .uri(BeerRouterConfig.BEER_PATH_ID, beerToUpdate.getId())
            .body(Mono.just(beerToUpdate), BeerDto.class)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    @Order(99)
    void testDeleteBeer() {
        BeerDto beerToDelete = this.getAnyExistingBeer();

        webTestClient
            .mutateWith(mockOAuth2Login())
            .delete()
            .uri(BeerRouterConfig.BEER_PATH_ID, beerToDelete.getId())
            .exchange()
            .expectStatus().isNoContent();

        BeerDto deletedBeer = getBeerById(beerToDelete.getId());
        assertNull(deletedBeer);
    }

    @Test
    @Order(99)
    void testDeleteBeerNotFound() {
        BeerDto beerToDelete = this.getAnyExistingBeer();
        beerToDelete.setId("7777");

        webTestClient
            .mutateWith(mockOAuth2Login())
            .delete()
            .uri(BeerRouterConfig.BEER_PATH_ID, beerToDelete.getId())
            .exchange()
            .expectStatus().isNotFound();
    }
    
    private BeerDto getAnyExistingBeer() {
        return webTestClient
            .mutateWith(mockOAuth2Login())
            .get().uri(BeerRouterConfig.BEER_PATH)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            .expectBodyList(BeerDto.class)
            .returnResult()
            .getResponseBody()
            .stream()
            .findFirst()
            .orElse(null);
    }


    private BeerDto getBeerById(String id) {
        try {
            return webTestClient
                .mutateWith(mockOAuth2Login())
                .get().uri(BeerRouterConfig.BEER_PATH_ID, id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody(BeerDto.class).returnResult().getResponseBody();
        } catch (AssertionError ex) {
            if (ex.getMessage().contains("Status expected:<200 OK> but was:<404 NOT_FOUND>")) {
                return null;
            }
            throw ex; 
        }
    }

    private BeerDto getBeerByLocation(String location) {
        return webTestClient
            .mutateWith(mockOAuth2Login())
            .get().uri(location)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            .expectBody(BeerDto.class).returnResult().getResponseBody();
    }
    
 
}
