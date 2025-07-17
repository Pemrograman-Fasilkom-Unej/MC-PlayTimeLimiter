package com.darkun7.limiter.api;

import com.darkun7.limiter.PlayTimeLimiter;
import com.darkun7.limiter.data.PlayerData;
import com.darkun7.limiter.data.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.util.UUID;

public class PlayTimeLimiterAPI {

    private final PlayerDataManager dataManager;

    public PlayTimeLimiterAPI(PlayTimeLimiter plugin) {
        this.dataManager = plugin.getDataManager();
    }

    public int getDailyUsed(UUID uuid) {
        PlayerData data = dataManager.getData(uuid);
        return data != null ? data.dailyUsed : -1;
    }

    public void setDailyUsed(UUID uuid, int minutes) {
        Bukkit.getLogger().info("[PlayTimeLimiter] Set daily used on " + uuid + " to "+ minutes + "minutes");
        PlayerData data = dataManager.getData(uuid);
        if (data == null) {
            data = new PlayerData(uuid);
            dataManager.getDataMap().put(uuid, data);
        }
        data.dailyUsed = minutes;
        dataManager.savePlayer(uuid);
    }

    public int getTotalUsed(UUID uuid) {
        PlayerData data = dataManager.getData(uuid);
        return data != null ? data.totalUsed : -1;
    }

    public void reduceDailyUsed(UUID uuid, int minutes) {
        Bukkit.getLogger().info("[PlayTimeLimiter] reduce daily used on " + uuid + " by "+ minutes + "minutes");
        PlayerData data = dataManager.getData(uuid);
        if (data == null) {
            data = new PlayerData(uuid);
            dataManager.getDataMap().put(uuid, data);
        }
        data.dailyUsed = Math.max(0, data.dailyUsed - minutes);
        dataManager.savePlayer(uuid);
    }

    public void setDailyDeath(UUID uuid, int count) {
        Bukkit.getLogger().info("[PlayTimeLimiter] Set death used on " + uuid + " to "+ count + "minutes");
        PlayerData data = dataManager.getData(uuid);
        if (data == null) {
            data = new PlayerData(uuid);
            dataManager.getDataMap().put(uuid, data);
        }
        data.dailyDeath = count;
        dataManager.savePlayer(uuid);
    }
}
