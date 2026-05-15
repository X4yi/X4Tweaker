package com.x4yi.x4tweaker.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class StringListSetting extends Setting<List<String>> {
    public StringListSetting(String name, String description) {
        super(name, description, new ArrayList<>());
    }

    public void addString(String str) {
        if (!getValue().contains(str)) {
            getValue().add(str);
        }
    }

    public void removeString(String str) {
        getValue().remove(str);
    }

    public boolean contains(String str) {
        return getValue().contains(str);
    }

    @Override
    public void reset() {
        setValue(new ArrayList<>());
    }

    @Override
    public void loadFromJson(JsonObject json) {
        if (json.has(getName())) {
            JsonArray array = json.getAsJsonArray(getName());
            List<String> list = new ArrayList<>();
            for (JsonElement el : array) {
                list.add(el.getAsString());
            }
            setValue(list);
        }
    }

    @Override
    public void saveToJson(JsonObject json) {
        JsonArray array = new JsonArray();
        for (String str : getValue()) {
            array.add(str);
        }
        json.add(getName(), array);
    }
}
