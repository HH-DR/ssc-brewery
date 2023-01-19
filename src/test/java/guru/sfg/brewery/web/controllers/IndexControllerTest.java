package guru.sfg.brewery.web.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
public class IndexControllerTest extends BaseIT{

    // TDD - to get this test running, one needs configuration
    @Test
    void getIndexSlash() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }
}