package guru.sfg.brewery.web.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.Rollback;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@SpringBootTest
public class CustomerControllerIT extends BaseIT {

    @DisplayName("--> Add Customer Tests Group")
    @Nested
    class AddCustomerTestsGroup{

        @Rollback
        @Test
        void processCreationForm() throws Exception {
            mockMvc.perform(post("/customers/new")
                    .param("customerName", "Bill Board")
                    .with(httpBasic("admin", "adminpw")))
                    .andExpect(status().is3xxRedirection());
        }

        @Rollback
        @ParameterizedTest(name = "{index} -> [{arguments}]")
        @MethodSource("guru.sfg.brewery.web.controllers.BeerControllerIT#getStreamNotAdmin")
        void processCreationFormWrongAuth(String user, String pwd) throws Exception {
            mockMvc.perform(post("/customers/new")
                            .with(csrf())
                            .param("customerName", "Bill Board")
                            .with(httpBasic(user, pwd)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void processCreationFormNoAuth() throws Exception {
            mockMvc.perform(post("/customers/new")
                            .with(csrf())
                            .param("customerName", "Bill Board"))
                    .andExpect(status().isUnauthorized());
        }
    }


    @DisplayName("-->Customer Tests Group")
    @Nested
    class CustomerTestsGroup{

    @ParameterizedTest(name = "-{index} with arg: [{arguments}]")
    @MethodSource("guru.sfg.brewery.web.controllers.BeerControllerIT#getStreamAdminCustomer")
    void testCustomerListWithAuth(String user, String pwd) throws Exception {
        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/customers")

                  .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic(user,pwd))
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk()
                );
    }

    @Test
    void testListCustomerWrongAuth() throws Exception {

        mockMvc.perform(get("/customers")
                        .with(httpBasic("user", "userpw")))
                .andExpect(status().isForbidden());
            }

    @Test
    void testListCustomerNoAuth() throws Exception {

        mockMvc.perform(get("/customers"))
                .andExpect(status().isUnauthorized());
            }
    }



}
