package com.danpeter.postson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class SystemUserSerializer implements JsonSerializer<SystemUser> {
    public JsonElement serialize(SystemUser src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("displayName", src.firstName() + " " + src.lastName());
        return jsonObject;
    }
}

