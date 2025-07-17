package com.darkun7.limiter.listener;

import com.darkun7.limiter.PlayTimeLimiter;
import com.darkun7.limiter.data.PlayerData;
import com.darkun7.limiter.data.PlayerDataManager;
import com.darkun7.limiter.util.MessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.Bukkit;
import java.util.UUID;
import java.time.LocalDate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import java.time.LocalDate;

public class PlayerEventListener implements Listener {

    private final PlayerDataManager dataManager = PlayTimeLimiter.getDataManager();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        dataManager.loadPlayer(event.getPlayer());

        Player player = event.getPlayer();
        PlayerData data = dataManager.getData(player.getUniqueId());
        if (data == null) return;

        int limit = PlayTimeLimiter.getInstance().getLimit(player);
        if (data.dailyUsed >= limit) {
            player.kick(MessageUtil.format(
                "messages.kick",
                "&cYou have reached your daily playtime limit of &e{minutes}&c minutes.",
                "minutes", String.valueOf(limit)
            ));
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        dataManager.loadPlayer(event.getPlayer());

        UUID uuid = event.getPlayer().getUniqueId();
        PlayerData data = PlayTimeLimiter.getDataManager().getData(uuid);


        if (data == null) return;

        // Daily reset if date changed
        String today = LocalDate.now().toString();
        if (!today.equals(data.lastLoginDate)) {
            data.dailyUsed = 0;
            data.dailyClaimed.clear();
            data.lastLoginDate = today;
            data.dailyDeath = 0;
        }

        int limit = PlayTimeLimiter.getInstance().getLimitByUUID(uuid);
        if (data.dailyUsed >= limit) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, MessageUtil.format(
                            "messages.kick",
                            "&cYou have reached your daily playtime limit of &e{minutes}&c minutes.",
                            "minutes", String.valueOf(limit)
                        ));
        }
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        dataManager.unloadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        PlayerData victimData = dataManager.getData(victim.getUniqueId());
        PlayerDataManager dataManager = PlayTimeLimiter.getDataManager();
        
        if (victimData == null || victimData.dailyUsed <= 0) return;
        if (victimData == null) return;

        int limit = PlayTimeLimiter.getInstance().getLimit(victim);
        if (victimData.dailyUsed >= limit) {
            victim.kick(MessageUtil.format(
                "messages.kick",
                "&cYou have reached your daily playtime limit of &e{minutes}&c minutes.",
                "minutes", String.valueOf(limit)
            ));
        }

        int penalty = PlayTimeLimiter.getInstance().getConfig().getInt("death.penalty-minutes", 1);
        int steal = PlayTimeLimiter.getInstance().getConfig().getInt("death.steal-minutes", 5);
        boolean PDState = PlayTimeLimiter.getInstance().getConfig().getBoolean("death.progressive", true);
        int PDIncrement = PlayTimeLimiter.getInstance().getConfig().getInt("death.progressive-increment", 1);
        int PDLimit = PlayTimeLimiter.getInstance().getConfig().getInt("death.progressive-limit", 5);

        Player killer = victim.getKiller();

        // PvP death: transfer time
        if (killer != null && killer != victim) {
            PlayerData killerData = dataManager.getData(killer.getUniqueId());
            if (killerData != null) {
                int drain = Math.min(steal, victimData.dailyUsed);
                if (PDState == false) {
                    victimData.dailyUsed += drain;
                    killerData.dailyUsed -= drain;

                    victim.sendMessage(MessageUtil.format(
                        "messages.kill-loss",
                        "&cYou lost &e{drain} &cminutes to {killer}.",
                        "drain", String.valueOf(drain),
                        "killer", killer.getName()
                    ));
                } else {
                    int DPPenalty = (Math.min(PDLimit, victimData.dailyDeath) * penalty) + drain;
                    victimData.dailyDeath += 1;
                    victimData.dailyUsed += DPPenalty;

                    victim.sendMessage(MessageUtil.format(
                        "messages.kill-loss",
                        "&cYou lost &e{drain} &cminutes to {killer}.",
                        "drain", String.valueOf(DPPenalty),
                        "killer", killer.getName()
                    ));
                }

                killer.sendMessage(MessageUtil.format(
                    "messages.kill-reward",
                    "&aYou gained &e{drain} &aminutes from killing {victim}.",
                    "drain", String.valueOf(drain),
                    "victim", victim.getName()
                ));
            }
        } else {
            // Mob/environment death: apply penalty
            int reduce = Math.min(penalty, victimData.dailyUsed);
            if (PDState == false) {
                victimData.dailyUsed += reduce;
            } else {
                int DPPenalty = Math.min(PDLimit, victimData.dailyDeath) * penalty;
                reduce = DPPenalty;
                victimData.dailyDeath += 1;
                victimData.dailyUsed += DPPenalty;
            }

            victim.sendMessage(MessageUtil.format(
                    "messages.killed",
                    "&cYou lost &e{reduce} &cminutes as a death penalty.",
                    "reduce", String.valueOf(reduce)
                ));
        }
    }

}
