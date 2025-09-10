package com.carlos.challenge.testutil;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private JsonUtil(){}
    public static String toJson(Object o) {
        try { return MAPPER.writeValueAsString(o); }
        catch (Exception e){ throw new RuntimeException(e); }
    }
}
