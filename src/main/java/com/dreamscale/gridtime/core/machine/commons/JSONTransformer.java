package com.dreamscale.gridtime.core.machine.commons;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.util.TimeZone;

@Slf4j
public class JSONTransformer {

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            mapper.registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .setTimeZone(TimeZone.getTimeZone("UTC"));

            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            return mapper.readValue(json, clazz);
        } catch (Exception ex) {
            log.error("Failed to convert "+clazz+" from json", ex);
            throw new RuntimeException(ex);
        }
    }

    public static String toJson(Object model) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            mapper.registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .setTimeZone(TimeZone.getTimeZone("UTC"));

            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return mapper.writer().writeValueAsString(model);
        } catch (JsonProcessingException ex) {
            log.error("Failed to convert "+model.getClass()+" into json", ex);
            return "";
        }
    }

}
