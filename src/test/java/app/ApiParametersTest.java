package app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiParametersTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        var service = new RentalService(new InMemoryRentalRepository());
        service.createBike("City, blue");
        mvc = MockMvcBuilders.standaloneSetup(new BikeController(service), new CustomerController(service))
                .setControllerAdvice(new ApiErrors())
                .addInterceptors(new WebConfig.QueryParameters())
                .build();
    }

    @Test
    void knownFiltersAreBoundWithoutChangingTheText() throws Exception {
        mvc.perform(get("/bikes").param("model", "City, blue").param("available", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].model").value("City, blue"));
    }

    @Test
    void invalidIdsAndAvailabilityAreBadRequests() throws Exception {
        mvc.perform(get("/bikes/0")).andExpect(status().isBadRequest());
        mvc.perform(get("/customers/abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/bikes").param("available", "yes")).andExpect(status().isBadRequest());
    }

    @Test
    void rejectsUnknownRepeatedAndBlankParameters() throws Exception {
        mvc.perform(get("/bikes").param("color", "red")).andExpect(status().isBadRequest());
        mvc.perform(get("/customers").param("name", "Anna", "Ivan")).andExpect(status().isBadRequest());
        mvc.perform(get("/bikes/with-customers").param("model", " ")).andExpect(status().isBadRequest());
        mvc.perform(get("/bikes/1").param("model", "City")).andExpect(status().isBadRequest());
    }
}
