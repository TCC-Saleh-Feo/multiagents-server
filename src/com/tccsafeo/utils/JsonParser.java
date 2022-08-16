package com.tccsafeo.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.util.ArrayList;

public class JsonParser {
    private static ObjectMapper mapper;

    private static ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        return mapper;
    }

    public static <T> T entity(String json, Class<T> genericClass) throws IOException {
        return getMapper().readValue(json, genericClass);
    }

    public static <T> ArrayList<T> arrayList(String json, Class<T> genericClass) throws IOException {
        TypeFactory typeFactory = getMapper().getTypeFactory();
        JavaType type = typeFactory.constructCollectionType(ArrayList.class, genericClass);
        return getMapper().readValue(json, type);
    }

    public static String toJson(Object object) throws IOException {
        return getMapper().writeValueAsString(object);
    }
}
