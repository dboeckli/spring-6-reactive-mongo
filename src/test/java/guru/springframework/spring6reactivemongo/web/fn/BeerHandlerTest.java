package guru.springframework.spring6reactivemongo.web.fn;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import lombok.extern.java.Log;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log
class BeerHandlerTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0.3").withExposedPorts(27017);

    private static final String DATABASE_NAME = BeerHandlerTest.class.getSimpleName();
    
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.database", () -> DATABASE_NAME); // Replace with your desired database name
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl(DATABASE_NAME));
        //registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeAll
    static void setup() {
        log.info("### Starting container on port: " + mongoDBContainer.getMappedPort(mongoDBContainer.getExposedPorts().getFirst()));
        mongoDBContainer.start();
        log.info("### ConnectionString: " + mongoDBContainer.getConnectionString());
    }

    @AfterAll
    static void tearDown() {
        log.info("################## stopping container ####################");
        mongoDBContainer.close();
    }

    @Autowired
    WebTestClient webTestClient;

    @Test
    @Order(1)
    void testListBeers() {
        webTestClient.get().uri(BeerRouterConfig.BEER_PATH)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            //.expectBody(BeerDto.class).isEqualTo(BeerDto.builder().beerName("Galaxy Cat").build())
            .expectBody().jsonPath("$.size()", hasSize(greaterThan(1)));
    }

    @Test
    @Order(1)
    void testListBeers2() {
        webTestClient.get().uri(BeerRouterConfig.BEER_PATH)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            //.expectBody().jsonPath("$.length()").isEqualTo(3)
            .expectBodyList(BeerDto.class).hasSize(3);
    }


    @Test
    @Order(2)
    void testGetBeerById() {
        BeerDto givenBeer = getAnyExistingBeer();
        
        BeerDto gotBeer = webTestClient.get().uri(BeerRouterConfig.BEER_PATH_ID, givenBeer.getId())
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

        webTestClient.get().uri(BeerRouterConfig.BEER_PATH_ID, givenBeer.getId())
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    @Order(3)
    void testCreateBeer() {
        BeerDto beerToCreate = BeerDto.builder().beerName("New Beer").build();

        String location = webTestClient.post().uri(BeerRouterConfig.BEER_PATH)
            .body(Mono.just(beerToCreate), BeerDto.class)
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().valueMatches("location", "/api/v3/beer/[a-f0-9]{24}$")
            .returnResult(BeerDto.class)
            .getResponseHeaders()
            .getLocation()
            .toString();

        System.out.println("Location: " + location);
        assertNotNull(location);
        
        BeerDto createdBeer = getBeerByLocation(location);
        assertNotNull(createdBeer);
    }

    @Test
    @Order(4)
    void testUpdateBeer() {
        BeerDto beerToUpdate = this.getAnyExistingBeer();
        beerToUpdate.setBeerName("UpdatedBeer");

        webTestClient.put()
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
    void testUpdateBeerNotFound() {
        BeerDto beerToUpdate = this.getAnyExistingBeer();
        beerToUpdate.setBeerName("UpdatedBeer");
        beerToUpdate.setId("9999");

        webTestClient.put()
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

        webTestClient.patch()
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
    void testPatchBeerNotFound() {
        BeerDto beerToUpdate = this.getAnyExistingBeer();
        beerToUpdate.setBeerName("PatchedBeer");
        beerToUpdate.setId("8888");

        webTestClient.patch()
            .uri(BeerRouterConfig.BEER_PATH_ID, beerToUpdate.getId())
            .body(Mono.just(beerToUpdate), BeerDto.class)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    @Order(99)
    void testDeleteBeer() {
        BeerDto beerToDelete = this.getAnyExistingBeer();

        webTestClient.delete()
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

        webTestClient.delete()
            .uri(BeerRouterConfig.BEER_PATH_ID, beerToDelete.getId())
            .exchange()
            .expectStatus().isNotFound();
    }
    
    private BeerDto getAnyExistingBeer() {
        return webTestClient.get().uri(BeerRouterConfig.BEER_PATH)
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
            return webTestClient.get().uri(BeerRouterConfig.BEER_PATH_ID, id)
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
        return webTestClient.get().uri(location)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-type", "application/json")
            .expectBody(BeerDto.class).returnResult().getResponseBody();
    }
    
 
}
