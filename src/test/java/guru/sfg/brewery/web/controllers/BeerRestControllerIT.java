package guru.sfg.brewery.web.controllers;

import guru.sfg.brewery.domain.Beer;
import guru.sfg.brewery.domain.BeerOrder;
import guru.sfg.brewery.repositories.BeerOrderRepository;
import guru.sfg.brewery.repositories.BeerRepository;
import guru.sfg.brewery.web.model.BeerStyleEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Random;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


//@WebMvcTest
@SpringBootTest
public class BeerRestControllerIT extends BaseIT{
   
    @Autowired
    BeerRepository beerRepository;
    @Autowired
    BeerOrderRepository beerOrderRepository;

    @DisplayName("List Beers")
    @Nested
    class ListBeers {

        @Test
        void findBeers() throws Exception {
            mockMvc.perform(get("/api/v1/beer"))
                    .andExpect(status().isUnauthorized());
        }

        @ParameterizedTest(name = "{index} with [{arguments}]")
        @MethodSource("guru.sfg.brewery.web.controllers.BeerControllerIT#getStreamAllUsers")
        void findBeersWithAuth(String user, String pwd) throws Exception {
            mockMvc.perform(get("/api/v1/beer/")
                    .with(httpBasic(user, pwd)))
                    .andExpect(status().isOk());
        }
    }

    @DisplayName("Find Beer By Id")
    @Nested
    class FindById{

        @Test
        void findBeerById() throws Exception {
//        mockMvc.perform(get("/api/v1/beer/493410b3-dd0b-4b78-97bf-289f50f6e74f"))
            mockMvc.perform(get("/api/v1/beer/" + beerToDelete().getId()))   // die Methode beerToDelete
                    // erzeugt ein Bier zum Löschen und speichert in die DB. Nach dem Ausstellen der Mocks in BaseIT ist die Methode nötig geworden
                    .andExpect(status().isUnauthorized());
        }

        @ParameterizedTest(name ="{index} with [{arguments}]")
        @MethodSource("guru.sfg.brewery.web.controllers.BeerControllerIT#getStreamAllUsers")
        void findBeerByIdWithAuth(String user, String pwd) throws Exception {
            Beer beer = beerRepository.findAll().get(0);

            mockMvc.perform(get("/api/v1/beer/" + beer.getId())
                            .with(httpBasic(user, pwd)))
                    .andExpect(status().isOk());
        }
    }




@DisplayName("Find Beer by UPC")
@Nested
class FindByUpc{
    @Test
    void findBeerByUpc() throws Exception {
        mockMvc.perform(get("/api/v1/beerUpc/0631234200036"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "{index} with [{arguments}]")
    @MethodSource("guru.sfg.brewery.web.controllers.BeerControllerIT#getStreamAllUsers")
    void findBeerByUpcWithAuth(String user, String pwd) throws Exception {
        mockMvc.perform(get("/api/v1/beerUpc/0631234200036")
                .with(httpBasic(user,pwd)))
                .andExpect(status().isOk());
    }

    }






    //    helper method for going against the DB, to get the ID of the requested Beer
    public Beer beerToDelete(){
        Random random = new Random();
        return beerRepository.saveAndFlush(Beer.builder()
                .beerName("Delete me Beer")
                .beerStyle(BeerStyleEnum.IPA)
                .minOnHand(12)
                .quantityToBrew(200)
                .upc(String.valueOf(random.nextInt(999999999)))
                .build()
        );
    }

///////////////////////////////////////// Tests - mit ROLES                         /////////////////////////////////////////
///////////////////////////////////////// Tests - pos. und neg. je nach Rolle       /////////////////////////////////////////


    @Test
    void name() {
    }

    @Test
    void findBeerFormAdmin() throws Exception {
        mockMvc.perform(get("/beers")
                .param("beerName","")   // siehe Kommentar in web.controllers.BaseIT wegen Mockito Mocks
                .with(httpBasic("admin", "adminpw")))
                .andExpect(status().isOk());
    }

    @Test
    void listBreweriesCustomer() throws Exception {
        mockMvc.perform(get("/brewery/breweries")
                        .with(httpBasic("customer","customerpw")))
                .andExpect(status().is2xxSuccessful());
    }
    @Test
    void listBreweriesUser() throws Exception {
        mockMvc.perform(get("/brewery/breweries")
                        .with(httpBasic("user","userpw")))
                .andExpect(status().isForbidden());
    }
    @Test
    void listBreweriesAdmin() throws Exception {
        mockMvc.perform(get("/brewery/breweries")
                        .with(httpBasic("admin","adminpw")))
                .andExpect(status().is2xxSuccessful());
    }



    @Test
    void testDeleteBeerHttpBasicAdminRole() throws Exception {
//        mockMvc.perform(delete("/api/v1/beer/493410b3-dd0b-4b78-97bf-289f50f6e74f")
        mockMvc.perform(delete("/api/v1/beer/"+ beerToDelete().getId()) // die Methode beerToDelete erzeugt ein Bier zum löschen und speichert in die DB
                        .with(httpBasic("admin", "adminpw"))
                )
                .andExpect(status().is2xxSuccessful()); // nicht 041 Unauthorized, sondern 403 Forbidden, weil mvcMatcher die Role Admin erfordert
    }

    @Test
    void testDeleteBeerHttpBasicCustomerRole() throws Exception {
        mockMvc.perform(delete("/api/v1/beer/493410b3-dd0b-4b78-97bf-289f50f6e74f")
                        .with(httpBasic("customer", "customerpw"))
                )
                .andExpect(status().isForbidden()); // nicht 041 Unauthorized, sondern 403 Forbidden, weil mvcMatcher die Role Admin erfordert
    }

    @Test
    void testDeleteBeerHttpBasicUserRole() throws Exception {
        mockMvc.perform(delete("/api/v1/beer/493410b3-dd0b-4b78-97bf-289f50f6e74f")
                        .with(httpBasic("user", "userpw"))
                )
                .andExpect(status().isForbidden());
    }







///////////////////////////////////////// Tests - noch ohne ROLES               /////////////////////////////////////////
///////////////////////////////////////// dafür aber mit RestHeaderAuthFilter   /////////////////////////////////////////
//    weil security.RestHeaderAuthFilter auskommentiert ist, musste die Config umgebaut werden, damit die Tests weiter laufen können
    @Test
    void testDeleteBeerBadCreds() throws Exception {
        mockMvc.perform(delete("/api/v1/beer/493410b3-dd0b-4b78-97bf-289f50f6e74f")
                .header("Api-Key", "user")
                .header("Api-Secret", "somethingWrong"))
                .andExpect(status().isUnauthorized());
    }

//    weil security.RestHeaderAuthFilter auskommentiert ist, musste die Config umgebaut werden, damit die Tests weiter laufen können
//    dieser Test musste deswegen aber ganz auskommentiert werden
//    @Test
//    void testDeleteBeerById() throws Exception {
//        mockMvc.perform(delete("/api/v1/beer/493410b3-dd0b-4b78-97bf-289f50f6e74f")
//                        .header("Api-Key", "user")
//                        .header("Api-Secret", "userpw")
//                        )
//                .andExpect(status().isOk());
//    }

    //    weil security.RestHeaderAuthFilter auskommentiert ist, musste die Config umgebaut werden, damit die Tests weiter laufen können
//    dieser Test musste deswegen aber ganz auskommentiert werden
//    @Test
//    void testDeleteBeerHttpBasic() throws Exception {
//        mockMvc.perform(delete("/api/v1/beer/493410b3-dd0b-4b78-97bf-289f50f6e74f")
//                        .with(httpBasic("user", "userpw"))
//                )
//                .andExpect(status().is2xxSuccessful());
//    }

    //    weil security.RestHeaderAuthFilter auskommentiert ist, musste die Config umgebaut werden, damit die Tests weiter laufen können
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






}
