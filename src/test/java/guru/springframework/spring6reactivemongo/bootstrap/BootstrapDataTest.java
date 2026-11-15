package guru.springframework.spring6reactivemongo.bootstrap;

import guru.springframework.spring6reactivemongo.model.Beer;
import guru.springframework.spring6reactivemongo.model.Customer;
import guru.springframework.spring6reactivemongo.repository.BeerRepository;
import guru.springframework.spring6reactivemongo.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class BootstrapDataTest {

    private final BeerRepository beerRepository = mock(BeerRepository.class);

    private final CustomerRepository customerRepository = mock(CustomerRepository.class);

    private final BootstrapData bootstrapData = new BootstrapData(beerRepository, customerRepository);

    @Test
    void doesNotLoadDataWhenRepositoriesAlreadyContainData() {
        given(beerRepository.deleteAll()).willReturn(Mono.empty());
        given(customerRepository.deleteAll()).willReturn(Mono.empty());
        given(beerRepository.count()).willReturn(Mono.just(1L));
        given(customerRepository.count()).willReturn(Mono.just(1L));

        bootstrapData.run();

        verify(beerRepository, never()).save(any());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void loadsDataWhenRepositoriesAreEmpty() {
        given(beerRepository.deleteAll()).willReturn(Mono.empty());
        given(customerRepository.deleteAll()).willReturn(Mono.empty());
        given(beerRepository.count()).willReturn(Mono.just(0L));
        given(customerRepository.count()).willReturn(Mono.just(0L));
        given(beerRepository.save(any(Beer.class))).willAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        given(customerRepository.save(any(Customer.class)))
            .willAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        given(beerRepository.findAll()).willReturn(Flux.empty());
        given(customerRepository.findAll()).willReturn(Flux.empty());

        bootstrapData.run();

        verify(beerRepository, times(3)).save(any());
        verify(customerRepository, times(3)).save(any());
    }

}
