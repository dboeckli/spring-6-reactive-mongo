package guru.springframework.spring6reactivemongo.service;

import guru.springframework.spring6reactivemongo.dto.CustomerDto;
import guru.springframework.spring6reactivemongo.mapper.CustomerMapper;
import guru.springframework.spring6reactivemongo.test.helper.AbstractBaseMongoTestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomerServiceImplTest extends AbstractBaseMongoTestUtil {

    @Autowired
    CustomerService customerService;

    @Autowired
    CustomerMapper customerMapper;

    private static final String DATABASE_NAME = "sjdlfsjlfjsldjflsjfdldskjf";

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.database", () -> DATABASE_NAME); // Replace with your desired database name
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl(DATABASE_NAME));
    }

    @Test
    void listCustomers() {
        List<CustomerDto> customers = customerService.listCustomers().collectList().block();

        assertNotNull(customers);
        assertEquals(3, customers.size());
        assertThat(customers).extracting(CustomerDto::getCustomerName).contains("John Doe", "Fridolin Mann", "Hansjörg Riesen");
    }

    @Test
    void findFirstCustomerByName() {
        Mono<CustomerDto> firstCustomer =  customerService.findFirstByCustomerName("John Doe");
        
        assertNotNull(firstCustomer);
        assertEquals("John Doe", firstCustomer.block().getCustomerName());
    }
    
}
