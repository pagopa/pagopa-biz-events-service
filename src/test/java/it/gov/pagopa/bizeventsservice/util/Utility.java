package it.gov.pagopa.bizeventsservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class Utility {

    private static ObjectMapper objectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        return om;
    }

    public static <T> T readModelFromFile(String relativePath, Class<T> clazz) throws IOException {
        ClassLoader classLoader = Utility.class.getClassLoader();

        try (InputStream is = classLoader.getResourceAsStream(relativePath)) {
            if (is == null) {
                throw new IOException("Test resource not found on classpath: " + relativePath);
            }
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return objectMapper().readValue(content, clazz);
        }
    }

    public static <T> T readModelFromFile(String relativePath, TypeReference<T> typeReference) throws IOException {
        ClassLoader classLoader = Utility.class.getClassLoader();

        try (InputStream is = classLoader.getResourceAsStream(relativePath)) {
            if (is == null) {
                throw new IOException("Test resource not found on classpath: " + relativePath);
            }
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return objectMapper().readValue(content, typeReference);
        }
    }

    /**
     * @param object to map into the Json string
     * @return object as Json string
     * @throws JsonProcessingException if there is an error during the parsing of the object
     */
    public String toJson(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }
}