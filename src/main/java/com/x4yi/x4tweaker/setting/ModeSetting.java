package com.x4yi.x4tweaker.setting;

import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting<String> {
    private final List<String> modes;

    public ModeSetting(String name, String description, String defaultValue, String... modes) {
        super(name, description, defaultValue);
        this.modes = Arrays.asList(modes);
    }

    public List<String> getModes() {
        return modes;
    }

    public void cycle() {
        int index = modes.indexOf(getValue());
        index++;
        if (index >= modes.size()) {
            index = 0;
        }
        setValue(modes.get(index));
    }

    public void cycleBack() {
        int index = modes.indexOf(getValue());
        index--;
        if (index < 0) {
            index = modes.size() - 1;
        }
        setValue(modes.get(index));
    }

    @Override
    public void setValue(String value) {
        if (modes.contains(value)) {
            super.setValue(value);
        }
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
