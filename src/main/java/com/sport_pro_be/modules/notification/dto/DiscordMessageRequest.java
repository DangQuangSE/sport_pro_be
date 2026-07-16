package com.sport_pro_be.modules.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiscordMessageRequest(
        String content,
        @JsonProperty("allowed_mentions") AllowedMentions allowedMentions) {
}
