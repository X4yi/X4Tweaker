package com.x4yi.x4tweaker.setting;

import com.google.gson.JsonObject;

public class StringSetting extends Setting<String> {
    public StringSetting(String name, String description, String defaultValue) {
        super(name, description, defaultValue);
    }

    @Override
    public void loadFromJson(JsonObject json) {
        if (json.has(getName())) {
            setValue(json.get(getName()).getAsString());
        }
    }

    @Override
    public void saveToJson(JsonObject json) {
        json.addProperty(getName(), getValue());
    }
}
