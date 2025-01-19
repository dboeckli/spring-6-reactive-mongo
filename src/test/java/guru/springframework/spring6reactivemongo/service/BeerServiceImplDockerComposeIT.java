package guru.springframework.spring6reactivemongo.service;

import guru.springframework.spring6reactivemongo.dto.BeerDto;
import guru.springframework.spring6reactivemongo.mapper.BeerMapper;
import guru.springframework.spring6reactivemongo.mapper.BeerMapperImpl;
import guru.springframework.spring6reactivemongo.model.Beer;
import lombok.extern.java.Log;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("docker-with-compose")
@Log
class BeerServiceImplDockerComposeIT {

    @Autowired
    BeerService beerService;

    @Autowired
    BeerMapper beerMapper;

    @Test
    @Order(1)
    void testGetById() {
        BeerDto savedBeer = beerService.saveBeer(Mono.just(beerMapper.beerToBeerDto(getTestBeer()))).block();
        BeerDto beer = beerService.getById(savedBeer.getId()).block();
        
        log.info("Found Beer ID: " + beer.getId());
        assertNotNull(beer);
        assertEquals(savedBeer.getBeerName(), beer.getBeerName());
    }

    
    @Test
    @Order(1)
    void testFindFirstByBeerNameWithBlock() {
        BeerDto beer = beerMapper.beerToBeerDto(getTestBeer());
        beer.setBeerName("beer to find");
        beerService.saveBeer(Mono.just(beer)).block();
        
        BeerDto foundBeer = beerService.findFirstByBeerName(beer.getBeerName()).block();

        assertNotNull(foundBeer);
        assertEquals(beer.getBeerName(), foundBeer.getBeerName());
    }

    
    @Test
    @Order(1)
    void testFindFirstByBeerNameWithSubscribe() {
        BeerDto beer = beerMapper.beerToBeerDto(getTestBeer());
        beer.setBeerName("beer to find");
        beerService.saveBeer(Mono.just(beer)).block();

        Mono<BeerDto> foundBeer = beerService.findFirstByBeerName(beer.getBeerName());

        AtomicBoolean waitingForSearch = new AtomicBoolean(false);
        AtomicReference<BeerDto> waitingForSearchedBeer = new AtomicReference<>();
        foundBeer.subscribe(foundDto -> {
            System.out.println("Found Beer ID: " + foundDto.getId());
            waitingForSearch.set(true);
            waitingForSearchedBeer.set(foundDto);
        });

        await().untilTrue(waitingForSearch);
        assertNotNull(waitingForSearchedBeer.get());
        assertEquals(beer.getBeerName(), waitingForSearchedBeer.get().getBeerName());
    }
    
    @Test
    @Order(1)
    void testFindByBeerStyleWithSubscribe() {
        BeerDto beer1 = beerMapper.beerToBeerDto(getTestBeer());
        beer1.setBeerName("beer1 to find");
        beer1.setBeerStyle("gugustyle");
        BeerDto beer2 = beerMapper.beerToBeerDto(getTestBeer());
        beer2.setBeerName("beer2 to find");
        beer2.setBeerStyle("gugustyle");
        beerService.saveBeer(Mono.just(beer1)).block();
        beerService.saveBeer(Mono.just(beer2)).block();

        AtomicBoolean waitingForSearch = new AtomicBoolean(false);
        AtomicReference<List<BeerDto>> waitingForSearchedBeers = new AtomicReference<>();
        beerService.findByBeerStyle("gugustyle")
            .collectList()
            .subscribe(dtos -> {
                System.out.println(dtos.toString());
                waitingForSearch.set(true);
                waitingForSearchedBeers.set(dtos);
            });

        await().untilTrue(waitingForSearch);

        List<BeerDto> foundBeers = waitingForSearchedBeers.get();
        assertNotNull(foundBeers);
        assertEquals(2, foundBeers.size());
        assertThat(foundBeers).extracting(BeerDto::getBeerName).contains("beer1 to find", "beer2 to find");
    }

    @Test
    @Order(1)
    void testListBeers() {
        BeerDto beer1 = beerMapper.beerToBeerDto(getTestBeer());
        beer1.setBeerName("listBeer 1");
        BeerDto beer2 = beerMapper.beerToBeerDto(getTestBeer());
        beer1.setBeerName("listBeer 2");

        beerService.saveBeer(Mono.just(beer1)).block();
        beerService.saveBeer(Mono.just(beer2)).block();
        
        List<BeerDto> beers = beerService.listBeers().collectList().block();

        assertNotNull(beers);
        assertTrue(beers.size() >= 2);
        assertThat(beers).extracting(BeerDto::getBeerName).contains(beer1.getBeerName(), beer2.getBeerName());
    }
    
    

    @Test
    @Order(10)
    void testSaveBeerWithSubscribe() {
        AtomicBoolean waitingForSave = new AtomicBoolean(false);
        AtomicReference<BeerDto> waitingForSavedBeer = new AtomicReference<>();
        
        Mono<BeerDto> savedMono = beerService.saveBeer(Mono.just(beerMapper.beerToBeerDto(getTestBeer())));

        savedMono.subscribe(savedDto -> {
            System.out.println("Save Beer ID: " + savedDto.getId());
            waitingForSave.set(true);
            waitingForSavedBeer.set(savedDto);
        });
        await().untilTrue(waitingForSave);
        assertNotNull(waitingForSavedBeer.get());
        assertNotNull(waitingForSavedBeer.get().getId());
    }

    @Test
    @Order(11)
    void testSaveBeerWitBlock() {
        BeerDto savedBeer = beerService.saveBeer(Mono.just(beerMapper.beerToBeerDto(getTestBeer()))).block();

        assertNotNull(savedBeer);
        assertNotNull(savedBeer.getId());
    }

    @Test
    @DisplayName("Test Update Beer Using Block")
    @Order(12)
    void testUpdateBlocking() {
        // first we add a new beer
        BeerDto savedBeer = beerService.saveBeer(Mono.just(beerMapper.beerToBeerDto(getTestBeer()))).block();
        
        // then we update the beer with the same id
        final String newName = "New Beer Name";  // use final so cannot mutate
        BeerDto beerToChange = getTestBeerDto();
        beerToChange.setBeerName(newName);

        BeerDto updatedDto = beerService.updateBeer(savedBeer.getId(), beerToChange).block();

        //verify exists in db
        BeerDto changedBeer = beerService.getById(updatedDto.getId()).block();
        assertThat(changedBeer.getBeerName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("Test Update Using Reactive Streams")
    @Order(13)
    void testUpdateStreaming() {
        // first we add a new beer
        BeerDto savedBeer = beerService.saveBeer(Mono.just(beerMapper.beerToBeerDto(getTestBeer()))).block();
        
        // then we update the beer with the same id
        final String newName = "New Beer Name";  // use final so cannot mutate
        BeerDto beerToChange = getTestBeerDto();
        beerToChange.setBeerName(newName);

        AtomicReference<BeerDto> atomicDto = new AtomicReference<>();
        beerService.updateBeer(savedBeer.getId(), beerToChange)
           .subscribe(atomicDto::set);

        await().until(() -> atomicDto.get() != null);

        BeerDto changedBeer = beerService.getById(atomicDto.get().getId()).block();
        assertThat(changedBeer.getBeerName()).isEqualTo(newName);
    }

    @Test
    @Order(99)
    void testDeleteBeer() {
        // first we add a new beer
        BeerDto savedBeer = beerService.saveBeer(Mono.just(beerMapper.beerToBeerDto(getTestBeer()))).block();
        
        // then we delete the beer with the same id 
        beerService.deleteBeerById(savedBeer.getId()).block();

        Mono<BeerDto> expectedEmptyBeerMono = beerService.getById(savedBeer.getId());
        BeerDto emptyBeer  = expectedEmptyBeerMono.block();

        assertThat(emptyBeer).isNull();
    }
    

    private static BeerDto getTestBeerDto(){
        return new BeerMapperImpl().beerToBeerDto(getTestBeer());
    }

    private static Beer getTestBeer() {
        return Beer.builder()
            .beerName("Space Dust")
            .beerStyle("IPA")
            .price(BigDecimal.TEN)
            .quantityOnHand(12)
            .upc("123213")
            .build();
    }
}
