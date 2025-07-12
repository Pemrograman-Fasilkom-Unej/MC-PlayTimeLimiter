package com.darkun7.limiter.data;

import com.darkun7.limiter.PlayTimeLimiter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class PlayerDataManager {

    private final Map<UUID, PlayerData> dataMap = new HashMap<>();
    private final File dataFolder;

    public PlayerDataManager() {
        dataFolder = new File(PlayTimeLimiter.getInstance().getDataFolder(), "data");
        if (!dataFolder.exists()) dataFolder.mkdirs();
    }

    public void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        File file = new File(dataFolder, uuid + ".yml");

        PlayerData data = new PlayerData(uuid);
        if (file.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            data.lastLoginDate = config.getString("lastLoginDate", "");
            data.dailyUsed = config.getInt("dailyUsed", 0);
            data.totalUsed = config.getInt("totalUsed", 0);
            data.hudEnabled = config.getBoolean("hudEnabled", false);
            data.hudType = config.getString("hudType", "tablist");
            data.dailyClaimed = new HashSet<>(config.getIntegerList("dailyClaimed"));
            data.totalClaimed = new HashSet<>(config.getIntegerList("totalClaimed"));
        }

        // Daily reset if date changed
        String today = LocalDate.now().toString();
        if (!today.equals(data.lastLoginDate)) {
            data.dailyUsed = 0;
            data.dailyClaimed.clear();
            data.lastLoginDate = today;
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
        config.set("totalUsed", data.totalUsed);
        config.set("hudEnabled", data.hudEnabled);
        config.set("hudType", data.hudType);
        config.set("dailyClaimed", new ArrayList<>(data.dailyClaimed));
        config.set("totalClaimed", new ArrayList<>(data.totalClaimed));

        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().warning("Failed to save data for " + uuid);
        }
    }

    public PlayerData getData(UUID uuid) {
        return dataMap.get(uuid);
    }

    public void unloadPlayer(UUID uuid) {
        savePlayer(uuid);
        dataMap.remove(uuid);
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
