package com.x4yi.x4tweaker.gui.v2.theme;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class ThemePresets {
    public static final String DEFAULT = "Default";
    public static final String DARK = "Dark";
    public static final String LIGHT = "Light";
    public static final String NEON = "Neon";
    public static final String OCEAN = "Ocean";

    private static final Map<String, Map<Integer, Color>> presets = new HashMap<String, Map<Integer, Color>>();

    static {
        Map<Integer, Color> d = new HashMap<Integer, Color>();
        d.put(0, new Color(75, 0, 130, 200));
        d.put(1, new Color(48, 0, 84, 200));
        d.put(2, new Color(0, 180, 80, 255));
        d.put(3, new Color(0, 120, 50, 255));
        d.put(4, new Color(150, 150, 150, 255));
        d.put(5, new Color(100, 100, 100, 255));
        d.put(6, new Color(15, 15, 15, 230));
        d.put(7, new Color(30, 30, 30, 255));
        d.put(8, new Color(30, 30, 30, 180));
        d.put(9, new Color(75, 0, 130, 100));
        d.put(10, new Color(20, 20, 20, 200));
        d.put(11, new Color(40, 40, 40, 200));
        d.put(12, new Color(12, 12, 12, 240));
        d.put(13, new Color(18, 18, 18, 220));
        d.put(14, new Color(60, 60, 60, 200));
        d.put(15, new Color(35, 35, 35, 180));
        d.put(16, new Color(60, 60, 60, 150));
        presets.put(DEFAULT, d);

        Map<Integer, Color> dk = new HashMap<Integer, Color>();
        dk.put(0, new Color(40, 40, 40, 200));
        dk.put(1, new Color(25, 25, 25, 200));
        dk.put(2, new Color(0, 200, 100, 255));
        dk.put(3, new Color(0, 140, 70, 255));
        dk.put(4, new Color(100, 100, 100, 255));
        dk.put(5, new Color(70, 70, 70, 255));
        dk.put(6, new Color(8, 8, 8, 240));
        dk.put(7, new Color(20, 20, 20, 255));
        dk.put(8, new Color(18, 18, 18, 180));
        dk.put(9, new Color(40, 40, 40, 100));
        dk.put(10, new Color(12, 12, 12, 200));
        dk.put(11, new Color(25, 25, 25, 200));
        dk.put(12, new Color(6, 6, 6, 240));
        dk.put(13, new Color(10, 10, 10, 220));
        dk.put(14, new Color(40, 40, 40, 200));
        dk.put(15, new Color(20, 20, 20, 180));
        dk.put(16, new Color(40, 40, 40, 150));
        presets.put(DARK, dk);

        Map<Integer, Color> l = new HashMap<Integer, Color>();
        l.put(0, new Color(100, 100, 200, 200));
        l.put(1, new Color(70, 70, 160, 200));
        l.put(2, new Color(0, 160, 60, 255));
        l.put(3, new Color(0, 120, 40, 255));
        l.put(4, new Color(120, 120, 120, 255));
        l.put(5, new Color(80, 80, 80, 255));
        l.put(6, new Color(240, 240, 240, 230));
        l.put(7, new Color(200, 200, 200, 255));
        l.put(8, new Color(220, 220, 220, 180));
        l.put(9, new Color(100, 100, 200, 100));
        l.put(10, new Color(230, 230, 230, 200));
        l.put(11, new Color(200, 200, 200, 200));
        l.put(12, new Color(235, 235, 235, 240));
        l.put(13, new Color(245, 245, 245, 220));
        l.put(14, new Color(180, 180, 180, 200));
        l.put(15, new Color(210, 210, 210, 180));
        l.put(16, new Color(180, 180, 180, 150));
        presets.put(LIGHT, l);

        Map<Integer, Color> n = new HashMap<Integer, Color>();
        n.put(0, new Color(255, 0, 128, 200));
        n.put(1, new Color(180, 0, 90, 200));
        n.put(2, new Color(0, 255, 200, 255));
        n.put(3, new Color(0, 180, 140, 255));
        n.put(4, new Color(150, 150, 150, 255));
        n.put(5, new Color(100, 100, 100, 255));
        n.put(6, new Color(10, 5, 15, 230));
        n.put(7, new Color(30, 10, 40, 255));
        n.put(8, new Color(20, 10, 30, 180));
        n.put(9, new Color(255, 0, 128, 100));
        n.put(10, new Color(15, 8, 20, 200));
        n.put(11, new Color(30, 15, 40, 200));
        n.put(12, new Color(8, 3, 12, 240));
        n.put(13, new Color(12, 6, 18, 220));
        n.put(14, new Color(50, 20, 60, 200));
        n.put(15, new Color(25, 12, 35, 180));
        n.put(16, new Color(50, 20, 60, 150));
        presets.put(NEON, n);

        Map<Integer, Color> o = new HashMap<Integer, Color>();
        o.put(0, new Color(0, 100, 180, 200));
        o.put(1, new Color(0, 60, 120, 200));
        o.put(2, new Color(0, 200, 150, 255));
        o.put(3, new Color(0, 140, 100, 255));
        o.put(4, new Color(130, 160, 180, 255));
        o.put(5, new Color(80, 110, 130, 255));
        o.put(6, new Color(5, 15, 25, 230));
        o.put(7, new Color(15, 30, 50, 255));
        o.put(8, new Color(10, 25, 40, 180));
        o.put(9, new Color(0, 100, 180, 100));
        o.put(10, new Color(8, 20, 35, 200));
        o.put(11, new Color(15, 30, 50, 200));
        o.put(12, new Color(3, 10, 20, 240));
        o.put(13, new Color(8, 18, 30, 220));
        o.put(14, new Color(30, 50, 70, 200));
        o.put(15, new Color(12, 25, 40, 180));
        o.put(16, new Color(30, 50, 70, 150));
        presets.put(OCEAN, o);
    }

    public static String[] getNames() {
        return new String[]{DEFAULT, DARK, LIGHT, NEON, OCEAN};
    }

    public static Map<Integer, Color> getPreset(String name) {
        return presets.get(name);
    }

    public static void applyPreset(String name, com.x4yi.x4tweaker.gui.v2.framework.ThemeBridge bridge) {
        Map<Integer, Color> preset = presets.get(name);
        if (preset == null) return;
        for (Map.Entry<Integer, Color> e : preset.entrySet()) {
            bridge.setColorByIndex(e.getKey(), e.getValue());
        }
    }
}
