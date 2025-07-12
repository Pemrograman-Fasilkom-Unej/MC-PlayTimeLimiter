package com.darkun7.limiter;

import com.darkun7.limiter.command.PlaytimeCommand;
import com.darkun7.limiter.command.PlaytimeTabCompleter;
import com.darkun7.limiter.data.PlayerDataManager;
import com.darkun7.limiter.hud.HUDManager;
import com.darkun7.limiter.listener.PlayerEventListener;
import com.darkun7.limiter.reward.RewardManager;
import com.darkun7.limiter.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.util.UUID;

public final class PlayTimeLimiter extends JavaPlugin {

    private static PlayTimeLimiter instance;
    private static PlayerDataManager dataManager;
    private static RewardManager rewardManager;
    private static HUDManager hudManager;

    public static PlayTimeLimiter getInstance() {
        return instance;
    }

    public static PlayerDataManager getDataManager() {
        return dataManager;
    }

    public static RewardManager getRewardManager() {
        return rewardManager;
    }

    public static HUDManager getHUDManager() {
        return hudManager;
    }


    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        dataManager = new PlayerDataManager();
        rewardManager = new RewardManager();
        hudManager = new HUDManager();

        getServer().getPluginManager().registerEvents(new PlayerEventListener(), this);
        getCommand("playtime").setExecutor(new PlaytimeCommand());
        getCommand("playtime").setTabCompleter(new PlaytimeTabCompleter());

        hudManager.start();

        startPlaytimeTracker();
    }

    @Override
    public void onDisable() {
        dataManager.saveAll();
    }

    private void startPlaytimeTracker() {
        int interval = 60; // seconds

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getServer().getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    var data = dataManager.getData(uuid);
                    if (data == null) continue;

                    int limit = getLimit(player);
                    data.dailyUsed++;
                    data.totalUsed++;

                    rewardManager.checkRewards(player);

                    double usedPercent = (double) data.dailyUsed / limit;
                    if (data.dailyUsed == (int)(limit * 0.75)) {
                        player.sendMessage(MessageUtil.get("messages.warn75", "&6You're at 75%!"));
                    } else if (data.dailyUsed == (int)(limit * 0.90)) {
                        player.sendMessage(MessageUtil.get("messages.warn90", "&6You're at 90%!"));
                    }

                    if (data.dailyUsed >= limit) {
                        player.kick(MessageUtil.get("messages.kick", "&cDaily playtime limit reached!"));
                    }
                }
            }
        }.runTaskTimer(this, 20L * interval, 20L * interval);
    }

    public int getLimit(Player player) {
        for (int i = 10000; i >= 1; i--) {
            if (player.hasPermission("playtime.limit." + i)) {
                return i;
            }
        }
        return getConfig().getInt("limits.default", 120);
    }

    public int getLimitByUUID(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) return getLimit(player);
        return getConfig().getInt("default-limit", 120); // fallback limit
    }


}
