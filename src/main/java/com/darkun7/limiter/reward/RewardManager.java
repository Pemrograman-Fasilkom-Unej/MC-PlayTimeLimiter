package com.darkun7.limiter.reward;

import com.darkun7.limiter.PlayTimeLimiter;
import com.darkun7.limiter.data.PlayerData;
import com.darkun7.limiter.data.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Set;

public class RewardManager {

    private final PlayTimeLimiter plugin = PlayTimeLimiter.getInstance();
    private final PlayerDataManager dataManager = plugin.getDataManager();

    public void checkRewards(Player player) {
        PlayerData data = dataManager.getData(player.getUniqueId());
        if (data == null) return;

        checkType(player, data.totalUsed, "total", data.totalClaimed);
        checkType(player, data.dailyUsed, "daily", data.dailyClaimed);
    }

    private void checkType(Player player, int minutes, String path, Set<Integer> claimedSet) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("rewards." + path);
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            int threshold;
            try {
                threshold = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                continue;
            }

            if (minutes >= threshold && !claimedSet.contains(threshold)) {
                ConfigurationSection reward = section.getConfigurationSection(key);
                if (reward == null) continue;

                String cmd = reward.getString("command", "").replace("%player%", player.getName());
                String msg = reward.getString("message", "");
                String broadcast = reward.getString("broadcast", "");

                // Execute reward
                if (!cmd.isEmpty()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }

                // Show message
                if (!msg.isEmpty()) {
                    String formatted = msg.replace("%player%", player.getName());
                    Component messageComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(formatted);
                    player.sendMessage(messageComponent);
                }

                if (!broadcast.isEmpty()) {
                    String formatted = broadcast.replace("%player%", player.getName());
                    Component messageComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(formatted);
                    Bukkit.broadcast(messageComponent);
                }

                claimedSet.add(threshold);
            }
        }
    }
}
