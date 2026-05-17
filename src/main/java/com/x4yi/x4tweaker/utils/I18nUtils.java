package com.x4yi.x4tweaker.utils;

import java.util.Locale;


public final class I18nUtils {

    private I18nUtils() {}


    public static String normalizeKey(String input) {
        String normalized = input.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
        int start = 0;
        int end = normalized.length();
        while (start < end && normalized.charAt(start) == '_') start++;
        while (end > start && normalized.charAt(end - 1) == '_') end--;
        return normalized.substring(start, end);
    }
}
