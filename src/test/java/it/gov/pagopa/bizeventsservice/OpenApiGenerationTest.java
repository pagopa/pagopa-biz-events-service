package it.gov.pagopa.bizeventsservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiGenerationTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @Test
    void swaggerSpringPlugin() throws Exception {
        saveOpenAPI("/v3/api-docs", "openapi.json");
        saveOpenAPI("/v3/api-docs/helpdesk", "openapi_helpdesk.json");
        saveOpenAPI("/v3/api-docs/ec", "openapi_ec.json");
        saveOpenAPI("/v3/api-docs/lap", "openapi_lap.json");
        saveOpenAPI("/v3/api-docs/lap_jwt", "openapi_lap_jwt.json");
        saveOpenAPI("/v3/api-docs/transaction", "openapi_transaction.json");
        saveOpenAPI("/v3/api-docs/transaction_jwt", "openapi_transaction_jwt.json");
    }

    private void saveOpenAPI(String fromUri, String toFile) throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(fromUri).accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andDo(
                        result -> {
                            assertNotNull(result);
                            assertNotNull(result.getResponse());
                            final String content = result.getResponse().getContentAsString();
                            assertFalse(content.isBlank());
                            assertFalse(content.contains("${"), "Generated swagger contains placeholders");
                            Object swagger =
                                    objectMapper.readValue(result.getResponse().getContentAsString(), Object.class);
                            String formatted =
                                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(swagger);
                            Path basePath = Paths.get("openapi/");
                            Files.createDirectories(basePath);
                            Files.write(basePath.resolve(toFile), formatted.getBytes());
                        });
    }
}
