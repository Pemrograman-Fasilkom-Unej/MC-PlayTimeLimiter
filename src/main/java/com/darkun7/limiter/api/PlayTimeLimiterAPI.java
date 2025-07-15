package com.darkun7.limiter.api;

import com.darkun7.limiter.PlayTimeLimiter;
import com.darkun7.limiter.data.PlayerData;
import com.darkun7.limiter.data.PlayerDataManager;
import org.bukkit.entity.Player;

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
        PlayerData data = dataManager.getData(uuid);
        if (data == null) {
            data = new PlayerData(uuid);
            dataManager.getDataMap().put(uuid, data);
        }
        data.dailyUsed = Math.max(0, data.dailyUsed - minutes);
        dataManager.savePlayer(uuid);
    }

    public void setDailyDeath(UUID uuid, int count) {
        PlayerData data = dataManager.getData(uuid);
        if (data == null) {
            data = new PlayerData(uuid);
            dataManager.getDataMap().put(uuid, data);
        }
        data.dailyDeath = count;
        dataManager.savePlayer(uuid);
    }
}
