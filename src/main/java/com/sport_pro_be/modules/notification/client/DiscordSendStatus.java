package com.sport_pro_be.modules.notification.client;

public enum DiscordSendStatus {
    SUCCESS,
    RATE_LIMITED,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    CLIENT_ERROR,
    SERVER_ERROR,
    NETWORK_ERROR,
    INVALID_RESPONSE
}
