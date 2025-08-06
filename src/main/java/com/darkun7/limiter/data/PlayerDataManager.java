package com.darkun7.limiter.data;

import com.darkun7.limiter.PlayTimeLimiter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class PlayerDataManager {

    private final Map<UUID, PlayerData> dataMap = new HashMap<>();
    private final Map<UUID, UUID> aliasMap = new HashMap<>(); // Bedrock UUID -> Java UUID
    private final File dataFolder;

    public PlayerDataManager() {
        dataFolder = new File(PlayTimeLimiter.getInstance().getDataFolder(), "data");
        if (!dataFolder.exists()) dataFolder.mkdirs();
    }

    public void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();

        // Check for an existing UUID with the same name (case-insensitive)
        for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            if (offline.getName() != null && offline.getName().equalsIgnoreCase(name)) {
                UUID existingUUID = offline.getUniqueId();
                if (!existingUUID.equals(uuid)) {
                    aliasMap.put(uuid, existingUUID); // map Bedrock -> Java
                    loadPlayerByUUID(existingUUID); // Load Java data instead
                    dataMap.put(uuid, dataMap.get(existingUUID)); // share the same data reference
                    return;
                }
            }
        }
        loadPlayerByUUID(player.getUniqueId());
    }

    public void loadPlayerByUUID(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");

        PlayerData data = new PlayerData(uuid);
        if (file.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            data.lastLoginDate = config.getString("lastLoginDate", "");
            data.dailyUsed = config.getInt("dailyUsed", 0);
            data.dailyExtra = config.getInt("dailyExtra", 0);
            data.totalUsed = config.getInt("totalUsed", 0);
            data.hudEnabled = config.getBoolean("hudEnabled", false);
            data.hudType = config.getString("hudType", "tablist");
            data.dailyDeath = config.getInt("dailyDeath", 0);
            data.dailyClaimed = new HashSet<>(config.getIntegerList("dailyClaimed"));
            data.totalClaimed = new HashSet<>(config.getIntegerList("totalClaimed"));
        }

        // Daily reset if date changed
        String today = LocalDate.now().toString();
        if (!today.equals(data.lastLoginDate)) {
            data.dailyUsed = 0;
            data.dailyExtra = 0;
            data.dailyClaimed.clear();
            data.lastLoginDate = today;
            data.dailyDeath = 0;
        }

        dataMap.put(uuid, data);
    }


    public void savePlayer(UUID uuid) {
        PlayerData data = dataMap.get(uuid);
        if (data == null) return;

        File file = new File(dataFolder, uuid + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        config.set("lastLoginDate", data.lastLoginDate);
        config.set("dailyUsed", data.dailyUsed);
        config.set("dailyExtra", data.dailyExtra);
        config.set("totalUsed", data.totalUsed);
        config.set("hudEnabled", data.hudEnabled);
        config.set("hudType", data.hudType);
        config.set("dailyDeath", data.dailyDeath);
        config.set("dailyClaimed", new ArrayList<>(data.dailyClaimed));
        config.set("totalClaimed", new ArrayList<>(data.totalClaimed));

        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().warning("Failed to save data for " + uuid);
        }
    }

    public UUID resolveUUID(UUID uuid) {
        return aliasMap.getOrDefault(uuid, uuid);
    }


    public PlayerData getData(UUID uuid) {
        UUID resolved = aliasMap.getOrDefault(uuid, uuid);
        return dataMap.get(resolved);
    }

    public void unloadPlayer(UUID uuid) {
        UUID resolved = aliasMap.getOrDefault(uuid, uuid);
        savePlayer(uuid);
        dataMap.remove(resolved);
    }

    public void saveAll() {
        for (UUID uuid : dataMap.keySet()) {
            savePlayer(uuid);
        }
    }

    public Map<UUID, PlayerData> getDataMap() {
        return dataMap;
    }

}
