package com.sport_pro_be.modules.notification.service;

import com.sport_pro_be.modules.notification.config.DiscordNotificationProperties;
import com.sport_pro_be.modules.notification.domain.OrderLineNotificationSnapshot;
import com.sport_pro_be.modules.notification.domain.OrderNotificationSnapshot;
import com.sport_pro_be.modules.notification.dto.AllowedMentions;
import com.sport_pro_be.modules.notification.dto.DiscordMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.sport_pro_be.modules.notification.constant.NotificationMessageConstant.DISCORD_MESSAGE_TOO_LONG;

@Component
@RequiredArgsConstructor
public class DiscordOrderMessageFormatter {

    private static final int CONTENT_LIMIT = 2000;
    private static final int CHUNK_SAFETY_OVERHEAD = 160;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DiscordNotificationProperties properties;

    public List<DiscordMessageRequest> format(OrderNotificationSnapshot snapshot) {
        int maxBodyLength = CONTENT_LIMIT - metadata(snapshot).length() - CHUNK_SAFETY_OVERHEAD;
        List<String> lineBlocks = snapshot.getLines().stream()
                .map(this::formatLine)
                .flatMap(line -> splitOversizedLine(line, maxBodyLength).stream())
                .toList();
        List<List<String>> partitions = partition(lineBlocks, maxBodyLength);

        List<DiscordMessageRequest> messages = new ArrayList<>();
        int totalParts = partitions.size();
        for (int index = 0; index < totalParts; index++) {
            String content = header(snapshot, index + 1, totalParts)
                    + String.join("\n", partitions.get(index))
                    + metadata(snapshot);
            if (content.length() > CONTENT_LIMIT) {
                throw new IllegalArgumentException(DISCORD_MESSAGE_TOO_LONG);
            }
            messages.add(new DiscordMessageRequest(content, AllowedMentions.none()));
        }
        return List.copyOf(messages);
    }

    private String header(OrderNotificationSnapshot snapshot, int part, int totalParts) {
        String partLabel = totalParts > 1 ? " — Part " + part + "/" + totalParts : "";
        return "**New order #" + escape(snapshot.getOrderCode()) + "**" + partLabel + "\n";
    }

    private String metadata(OrderNotificationSnapshot snapshot) {
        String url = properties.getAdminOrderUrlTemplate().replace("{orderId}", snapshot.getOrderId().toString());
        return "\n**Total:** " + formatMoney(snapshot.getTotalAmount(), snapshot.getCurrency())
                + "\n**Created:** " + DATE_FORMAT.withZone(properties.getTimeZone()).format(snapshot.getCreatedAt())
                + "\n**Admin:** " + url;
    }

    private String formatLine(OrderLineNotificationSnapshot line) {
        List<String> attributes = new ArrayList<>();
        addAttribute(attributes, line.getVariant());
        addAttribute(attributes, line.getSize());
        addAttribute(attributes, line.getColor());
        String suffix = attributes.isEmpty() ? "" : " (" + String.join(" / ", attributes) + ")";
        return "• " + escape(line.getProductName()) + suffix + " × " + line.getQuantity();
    }

    private void addAttribute(List<String> attributes, String value) {
        if (value != null && !value.isBlank()) {
            attributes.add(escape(value));
        }
    }

    private List<List<String>> partition(List<String> lines, int maxBodyLength) {
        List<List<String>> result = new ArrayList<>();
        List<String> current = new ArrayList<>();
        int currentLength = 0;
        for (String line : lines) {
            int additionalLength = line.length() + (current.isEmpty() ? 0 : 1);
            if (!current.isEmpty() && currentLength + additionalLength > maxBodyLength) {
                result.add(List.copyOf(current));
                current.clear();
                currentLength = 0;
            }
            current.add(line);
            currentLength += line.length() + (current.size() == 1 ? 0 : 1);
        }
        if (!current.isEmpty()) {
            result.add(List.copyOf(current));
        }
        return result.isEmpty() ? List.of(List.of()) : List.copyOf(result);
    }

    private List<String> splitOversizedLine(String line, int maxLength) {
        if (maxLength < 1) {
            throw new IllegalArgumentException(DISCORD_MESSAGE_TOO_LONG);
        }
        List<String> segments = new ArrayList<>();
        int offset = 0;
        while (offset < line.length()) {
            String prefix = segments.isEmpty() ? "" : "↳ ";
            int available = maxLength - prefix.length();
            if (available < 1) {
                throw new IllegalArgumentException(DISCORD_MESSAGE_TOO_LONG);
            }
            int end = safeSplitBoundary(line, offset, Math.min(line.length(), offset + available));
            if (end <= offset) {
                throw new IllegalArgumentException(DISCORD_MESSAGE_TOO_LONG);
            }
            segments.add(prefix + line.substring(offset, end));
            offset = end;
        }
        return segments.isEmpty() ? List.of("") : List.copyOf(segments);
    }

    private int safeSplitBoundary(String value, int start, int proposedEnd) {
        int end = proposedEnd;
        if (end < value.length() && end > start
                && Character.isHighSurrogate(value.charAt(end - 1))
                && Character.isLowSurrogate(value.charAt(end))) {
            end--;
        }
        while (end > start && hasOddTrailingBackslashCount(value, start, end)) {
            end--;
        }
        return end;
    }

    private boolean hasOddTrailingBackslashCount(String value, int start, int end) {
        int count = 0;
        for (int index = end - 1; index >= start && value.charAt(index) == '\\'; index--) {
            count++;
        }
        return count % 2 != 0;
    }

    private String formatMoney(BigDecimal amount, String currency) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        formatter.setMaximumFractionDigits("VND".equalsIgnoreCase(currency) ? 0 : 2);
        formatter.setMinimumFractionDigits(0);
        return formatter.format(amount) + " " + currency;
    }

    private String escape(String value) {
        String normalized = normalizeUnsafeCharacters(value);
        if (normalized.startsWith(">")) {
            normalized = "\\" + normalized;
        }
        return normalized
                .replace("\\", "\\\\")
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace("|", "\\|")
                .replace("#", "\\#")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("@everyone", "@\u200Beveryone")
                .replace("@here", "@\u200Bhere")
                .replace("<@", "<@\u200B");
    }

    private String normalizeUnsafeCharacters(String value) {
        StringBuilder normalized = new StringBuilder(value.length());
        value.codePoints().forEach(codePoint -> {
            int type = Character.getType(codePoint);
            if (type == Character.CONTROL
                    || type == Character.FORMAT
                    || type == Character.LINE_SEPARATOR
                    || type == Character.PARAGRAPH_SEPARATOR) {
                normalized.append(' ');
            } else {
                normalized.appendCodePoint(codePoint);
            }
        });
        return normalized.toString();
    }
}
