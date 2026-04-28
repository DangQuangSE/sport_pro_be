package com.sport_pro_be.common;

import java.text.Normalizer;
import java.util.Locale;

public class SlugUtils {

    private SlugUtils() {
    }

    public static String toSlugBase(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return normalized;
    }
}
