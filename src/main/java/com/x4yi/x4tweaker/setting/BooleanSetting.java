package com.x4yi.x4tweaker.setting;

import com.google.gson.JsonObject;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }

    public void toggle() {
        setValue(!getValue());
    }

    @Override
    public void loadFromJson(JsonObject json) {
        if (json.has(getName())) {
            setValue(json.get(getName()).getAsBoolean());
        }
    }

    @Override
    public void saveToJson(JsonObject json) {
        json.addProperty(getName(), getValue());
    }
}
