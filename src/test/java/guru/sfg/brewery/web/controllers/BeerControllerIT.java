package guru.sfg.brewery.web.controllers;

import guru.sfg.brewery.repositories.BeerRepository;
import guru.sfg.brewery.security.permissions.BeerReadPermission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


//@WebMvcTest
@SpringBootTest
public class BeerControllerIT extends BaseIT{

    ///////////////////////////////////////// Refactor for @Nested und @Parameterized  /////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Autowired
    BeerRepository beerRepository;

    @DisplayName("Init New Form - Nested Class")
    @Nested
    class InitNewForm{

        @ParameterizedTest(name = "#{index} with [{arguments}]")                             // wiederholt den Test für jeden Parameter, diese sind in der Klasse
        @MethodSource("guru.sfg.brewery.web.controllers.BeerControllerIT#getStreamAllUsers") // BaseIT in static methods als Arguments angelegt
        void initCreationFormAuth(String user, String pwd) throws Exception {
            mockMvc.perform(get("/beers/new")
                            .with(httpBasic(user, pwd)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("beers/createBeer"))
                    .andExpect(model().attributeExists("beer"));
        }

        @Test
        void initCreationFormNotAuth() throws Exception {
            mockMvc.perform(get("/beers/new"))
                    .andExpect(status().isUnauthorized());
        }
    }


//    test with
    @WithMockUser("user")
    @Test
    void findBeers()throws Exception{
        mockMvc.perform(get("/beers/find"))
                .andExpect(status().isOk())
                .andExpect(view().name("beers/findBeers"))
                .andExpect(model().attributeExists("beer"));
    }

    // test with user from application.properties using httpBasic - method
    @Test
    void findBeersWithHttpBasic() throws Exception {

        mockMvc.perform(get("/beers/find").with(httpBasic("user", "userpw")))
                .andExpect(status().isOk())
                .andExpect(view().name("beers/findBeers"))
                .andExpect(model().attributeExists("beer"));
    }


}
