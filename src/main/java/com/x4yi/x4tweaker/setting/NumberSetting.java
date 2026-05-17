package com.x4yi.x4tweaker.setting;

import com.google.gson.JsonObject;

public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double increment;

    public NumberSetting(String name, String description, double defaultValue, double min, double max, double increment) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getIncrement() {
        return increment;
    }

    @Override
    public void setValue(Double value) {
        double val = value;
        val = Math.max(min, Math.min(max, val));
        val = (double) Math.round(val * (1.0 / increment)) / (1.0 / increment);
        super.setValue(val);
    }

    @Override
    public void loadFromJson(JsonObject json) {
        if (json.has(getName())) {
            setValue(json.get(getName()).getAsDouble());
        }
    }

    @Override
    public void saveToJson(JsonObject json) {
        json.addProperty(getName(), getValue());
    }
}
