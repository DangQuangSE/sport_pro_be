package com.sport_pro_be.modules.notification.dto;

import java.util.List;

public record AllowedMentions(List<String> parse) {
    public AllowedMentions {
        parse = parse == null ? List.of() : List.copyOf(parse);
    }

    public static AllowedMentions none() {
        return new AllowedMentions(List.of());
    }
}
