package guru.springframework.spring6reactivemongo.bootstrap;

import guru.springframework.spring6reactivemongo.model.Beer;
import guru.springframework.spring6reactivemongo.model.Customer;
import guru.springframework.spring6reactivemongo.repository.BeerRepository;
import guru.springframework.spring6reactivemongo.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Log
public class BootstrapData implements CommandLineRunner {

    private final BeerRepository beerRepository;
    
    private final CustomerRepository customerRepository;

    @Override
    public void run(String @NonNull ... args) {
        beerRepository.deleteAll()
            .doOnSuccess(success -> loadBeerData())
            .subscribe();

        customerRepository.deleteAll()
            .doOnSuccess(success -> loadCustomerData())
            .subscribe();
    }

    private void loadCustomerData() {
        log.info("Adding customer data...");
        if (customerRepository.count().block() == 0) {
            Customer customer1 = Customer.builder()
                .customerName("John Doe")
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

            Customer customer2 = Customer.builder()
                .customerName("Fridolin Mann")
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

            Customer customer3 = Customer.builder()
                .customerName("Hansjörg Riesen")
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

            customerRepository.save(customer1).block();
            customerRepository.save(customer2).block();
            customerRepository.save(customer3).block();

            Objects.requireNonNull(customerRepository.findAll()
                    .collectList()
                    .block())
                .forEach(customer -> log.info("Customer added: " + customer));

            log.info("3 Customers added successfully");
        }
    }

    private void loadBeerData() {
        log.info("Adding beer data...");
        if (beerRepository.count().block() == 0) {
            Beer beer1 = Beer.builder()
                .beerName("Galaxy Cat")
                .beerStyle("Pale Ale")
                .upc("12356")
                .price(new BigDecimal("12.99"))
                .quantityOnHand(122)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

            Beer beer2 = Beer.builder()
                .beerName("Crank")
                .beerStyle("Pale Ale")
                .upc("12356222")
                .price(new BigDecimal("11.99"))
                .quantityOnHand(392)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

            Beer beer3 = Beer.builder()
                .beerName("Sunshine City")
                .beerStyle("IPA")
                .upc("12356")
                .price(new BigDecimal("13.99"))
                .quantityOnHand(144)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

            beerRepository.save(beer1).block();
            beerRepository.save(beer2).block();
            beerRepository.save(beer3).block();

            beerRepository.findAll()
                .collectList()
                .block()
                .forEach(beer -> log.info("Beer added: " + beer));

            log.info("3 Beers added successfully");
        }
    }
}
