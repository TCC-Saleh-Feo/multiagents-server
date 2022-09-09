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

    public static <T> T entity(String json, Class<T> genericClass) {
        try {
            return getMapper().readValue(json, genericClass);
        } catch (IOException exception) {
            System.out.println("Could not convert string to entity!");
        }
        return null;
    }

    public static <T> ArrayList<T> arrayList(String json, Class<T> genericClass) {
        try {
            TypeFactory typeFactory = getMapper().getTypeFactory();
            JavaType type = typeFactory.constructCollectionType(ArrayList.class, genericClass);
            return getMapper().readValue(json, type);
        } catch (IOException exception) {
            System.out.println("Could not convert string to entity!");
        }
        return null;
    }

    public static String toJson(Object object) {
        try {
            return getMapper().writeValueAsString(object);
        } catch (IOException exception) {
            System.out.println("String could not be converted to JSON!");
        }
        return null;
    }
}
