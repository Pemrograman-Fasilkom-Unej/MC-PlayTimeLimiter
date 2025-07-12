package com.darkun7.limiter.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import com.darkun7.limiter.PlayTimeLimiter;

public class MessageUtil {

    /**
     * Get a colored message from config.
     * Supports '&' color codes.
     * @param path the config path, e.g., "messages.warn90"
     * @param fallback the fallback message if not found
     * @return a Component with color formatting
     */
    public static Component get(String path, String fallback) {
        FileConfiguration config = PlayTimeLimiter.getInstance().getConfig();
        String raw = config.getString(path, fallback);
        return LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
    }

    /**
     * Get a formatted message with placeholders (like {minutes}) replaced.
     * @param path the config path
     * @param fallback fallback message if config missing
     * @param placeholders array of [key, value, key2, value2...]
     * @return formatted Component
     */
    public static Component format(String path, String fallback, String... placeholders) {
        FileConfiguration config = PlayTimeLimiter.getInstance().getConfig();
        String raw = config.getString(path, fallback);
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            String key = placeholders[i];
            String value = placeholders[i + 1];
            raw = raw.replace("{" + key + "}", value);
        }
        return LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
    }
} 
