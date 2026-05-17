package com.x4yi.x4tweaker.setting;

import com.google.gson.JsonObject;
import java.awt.Color;

public class ColorSetting extends Setting<Color> {
    public ColorSetting(String name, String description, Color defaultValue) {
        super(name, description, defaultValue);
    }

    @Override
    public void loadFromJson(JsonObject json) {
        if (json.has(getName())) {
            int rgb = json.get(getName()).getAsInt();
            setValue(new Color(rgb, true));
        }
    }

    @Override
    public void saveToJson(JsonObject json) {
        json.addProperty(getName(), getValue().getRGB());
    }
}
