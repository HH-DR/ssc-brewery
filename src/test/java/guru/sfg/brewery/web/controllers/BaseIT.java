package guru.sfg.brewery.web.controllers;

import guru.sfg.brewery.domain.BeerInventory;
import guru.sfg.brewery.repositories.BeerInventoryRepository;
import guru.sfg.brewery.repositories.BeerRepository;
import guru.sfg.brewery.repositories.CustomerRepository;
import guru.sfg.brewery.services.BeerService;
import guru.sfg.brewery.services.BreweryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@Slf4j
public abstract class BaseIT {

    @Autowired
    WebApplicationContext wac;

    protected MockMvc mockMvc;

//    Diese MockBeans werden nur benötigt, wenn man ohne H2 Datenbank arbeitet.
//    Wenn Sie eingesetzt werden, wenn die Tests auf die richtigen Daten zugreifen, dann werden Nullpointer Produziert,
//    Denn die Mocks können nur das zurückgeben, was man definiert.


//    @MockBean
//    BeerRepository beerRepository;
//    @MockBean
//    BeerInventory beerInventory;
//    @MockBean
//    BeerInventoryRepository beerInventoryRepository;
//    @MockBean
//    BreweryService breweryService;
//    @MockBean
//    CustomerRepository customerRepository;
//    @MockBean
//    BeerService beerService;

    @BeforeEach
    void setup(){
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
        log.debug("=========== BaseIT setup() successfully created mockMvc");
    }




    public static Stream<Arguments> getStreamAdminCustomer(){
        return Stream.of(
                Arguments.of("admin", "adminpw"),
                Arguments.of("customer", "customerpw")
        );
    }


    // static method to deliver Parameters for @ParameterizedTest
    public static Stream<Arguments> getStreamAllUsers(){
        return Stream.of(
                Arguments.of("user", "userpw"),
                Arguments.of("customer", "customerpw"),
                Arguments.of("admin", "adminpw")
        );
    }

    // static method to deliver Parameters for @ParameterizedTest
    public static Stream<Arguments> getStreamNotAdmin(){
        return Stream.of(
                Arguments.of("user", "userpw"),
                Arguments.of("customer", "customerpw")
        );
    }



}
