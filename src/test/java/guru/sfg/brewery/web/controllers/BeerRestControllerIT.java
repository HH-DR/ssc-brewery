package guru.sfg.brewery.web.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest
public class BeerRestControllerIT extends BaseIT{

    @Test
    void testDeleteBeerBadCreds() throws Exception {
        mockMvc.perform(delete("/api/v1/beer/493410b3-dd0b-4b78-97bf-289f50f6e74f")
                .header("Api-Key", "user")
                .header("Api-Secret", "somethingWrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteBeerById() throws Exception {
        mockMvc.perform(delete("/api/v1/beer/493410b3-dd0b-4b78-97bf-289f50f6e74f")
                        .header("Api-Key", "user")
                        .header("Api-Secret", "userpw")
                        )
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteBeerHttpBasic() throws Exception {
        mockMvc.perform(delete("/api/v1/beer/493410b3-dd0b-4b78-97bf-289f50f6e74f")
                        .with(httpBasic("user", "userpw"))
                )
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void testDeleteBeerNoAuth() throws Exception {
        mockMvc.perform(delete("/api/v1/beer/493410b3-dd0b-4b78-97bf-289f50f6e74f"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void initCreationForm() throws Exception {
        mockMvc.perform(get("/beers/new").with(httpBasic("user","userpw"))) // user und userpw kommen aus der SecurityConfig - erstellt mit Overwrite von protected UserDetailsService userDetailsService()
                .andExpect(status().isOk())
                .andExpect(view().name("beers/createBeer"))
                .andExpect(model().attributeExists("beer"));
    }

    @Test
    void findBeers() throws Exception {
        mockMvc.perform(get("/api/v1/beer"))
                .andExpect(status().isOk());
    }

    @Test
    void findBeerById() throws Exception {
        mockMvc.perform(get("/api/v1/beer/493410b3-dd0b-4b78-97bf-289f50f6e74f"))
                .andExpect(status().isOk());
    }

    @Test
    void findBeerByUpc() throws Exception {
        mockMvc.perform(get("/api/v1/beerUpc/0631234200036"))
                .andExpect(status().isOk());
    }
}
