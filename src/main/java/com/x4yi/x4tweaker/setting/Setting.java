package com.x4yi.x4tweaker.setting;

import com.google.gson.JsonObject;
import com.x4yi.x4tweaker.module.Module;
import java.util.function.Supplier;

public abstract class Setting<T> {
    private final String name;
    private final String description;
    private T value;
    private final T defaultValue;
    private Module owner;
    private Supplier<Boolean> visibilityCondition = new Supplier<Boolean>() {
        @Override
        public Boolean get() {
            return Boolean.TRUE;
        }
    };

    public Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
        if (owner != null) {
            owner.onSettingChanged();
        }
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public boolean isVisible() {
        try {
            return visibilityCondition == null || visibilityCondition.get();
        } catch (Exception ignored) {
            return true;
        }
    }

    public Setting<T> withVisibilityCondition(Supplier<Boolean> condition) {
        if (condition != null) {
            this.visibilityCondition = condition;
        }
        return this;
    }

    public void setOwner(Module owner) {
        this.owner = owner;
    }

    public void reset() {
        setValue(defaultValue);
    }

    public abstract void loadFromJson(JsonObject json);
    public abstract void saveToJson(JsonObject json);
}
