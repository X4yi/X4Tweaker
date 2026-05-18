package com.x4yi.x4tweaker.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.translation.LanguageMap;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Shared i18n utility methods used by ClickGUI and HUD modules.
 */
public final class I18nUtils {

    private static final Map<String, String> FALLBACK_MAP = new HashMap<String, String>();

    static {
        FALLBACK_MAP.put("es_mx", "es_es");
        FALLBACK_MAP.put("es_ar", "es_es");
        FALLBACK_MAP.put("es_co", "es_es");
        FALLBACK_MAP.put("es_cl", "es_es");
        FALLBACK_MAP.put("es_pe", "es_es");
        FALLBACK_MAP.put("es_ve", "es_es");
        FALLBACK_MAP.put("es_uy", "es_es");
        FALLBACK_MAP.put("es_ec", "es_es");
        FALLBACK_MAP.put("es_gt", "es_es");
        FALLBACK_MAP.put("es_cu", "es_es");
        FALLBACK_MAP.put("es_bo", "es_es");
        FALLBACK_MAP.put("es_do", "es_es");
        FALLBACK_MAP.put("es_hn", "es_es");
        FALLBACK_MAP.put("es_py", "es_es");
        FALLBACK_MAP.put("es_sv", "es_es");
        FALLBACK_MAP.put("es_ni", "es_es");
        FALLBACK_MAP.put("es_cr", "es_es");
        FALLBACK_MAP.put("es_pa", "es_es");
        FALLBACK_MAP.put("es_pr", "es_es");
    }

    private I18nUtils() {}

    /**
     * Normalizes a display name into a lang key segment.
     * Example: "KillAuraLegit" → "killauralegit", "Better AFK" → "better_afk"
     */
    public static String normalizeKey(String input) {
        String normalized = input.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
        int start = 0;
        int end = normalized.length();
        while (start < end && normalized.charAt(start) == '_') start++;
        while (end > start && normalized.charAt(end - 1) == '_') end--;
        return normalized.substring(start, end);
    }

    /**
     * Returns the fallback language for a given locale.
     * Example: "es_MX" → "es_ES", "es_AR" → "es_ES"
     * Returns null if no fallback is needed or defined.
     */
    public static String getFallbackLanguage(String langCode) {
        if (langCode == null) return null;
        String normalized = langCode.toLowerCase(Locale.ROOT);
        return FALLBACK_MAP.get(normalized);
    }

    /**
     * Detects the current game language and applies the appropriate fallback.
     * If the language is a Spanish regional variant, injects es_es.lang translations.
     * Call this once during mod initialization.
     */
    public static void applyLanguageFallback() {
        try {
            String currentLang = Minecraft.getMinecraft().gameSettings.language;
            if (currentLang == null) return;

            String normalized = currentLang.toLowerCase(Locale.ROOT);
            String fallback = FALLBACK_MAP.get(normalized);
            if (fallback == null) return;

            String resourcePath = "/assets/x4tweaker/lang/" + fallback + ".lang";
            InputStream stream = I18nUtils.class.getResourceAsStream(resourcePath);
            if (stream == null) return;

            Map<String, String> entries = new HashMap<String, String>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq < 0) continue;
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                entries.put(key, value);
            }
            reader.close();
            stream.close();

            injectTranslations(entries);
        } catch (Exception e) {
            // Silently fail - fallback is optional
        }
    }

    @SuppressWarnings("unchecked")
    private static void injectTranslations(Map<String, String> entries) throws Exception {
        LanguageMap langMap;
        try {
            java.lang.reflect.Method getInstanceMethod = LanguageMap.class.getDeclaredMethod("getInstance");
            getInstanceMethod.setAccessible(true);
            langMap = (LanguageMap) getInstanceMethod.invoke(null);
        } catch (NoSuchMethodException e) {
            java.lang.reflect.Field instanceField = LanguageMap.class.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            langMap = (LanguageMap) instanceField.get(null);
        }

        Field privateMapField = LanguageMap.class.getDeclaredField("privateMap");
        privateMapField.setAccessible(true);
        Map<String, String> privateMap = (Map<String, String>) privateMapField.get(langMap);
        privateMap.putAll(entries);

        Field languageMapField = LanguageMap.class.getDeclaredField("languageMap");
        languageMapField.setAccessible(true);
        Map<String, String> languageMap = (Map<String, String>) languageMapField.get(langMap);
        languageMap.putAll(entries);
    }
}
