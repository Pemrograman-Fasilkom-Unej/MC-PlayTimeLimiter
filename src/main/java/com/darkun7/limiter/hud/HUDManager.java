package com.darkun7.limiter.hud;

import com.darkun7.limiter.PlayTimeLimiter;
import com.darkun7.limiter.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class HUDManager {

    private final PlayTimeLimiter plugin = PlayTimeLimiter.getInstance();

    public void start() {
        int interval = plugin.getConfig().getInt("hud.interval-seconds", 10);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerData data = plugin.getDataManager().getData(player.getUniqueId());
                    if (data == null || !data.hudEnabled) continue;

                    String hudType = data.hudType.toLowerCase();

                    switch (hudType) {
                        case "scoreboard":
                            updateScoreboard(player, data);
                            break;
                        case "tablist":
                            updateTablist(player, data);
                            break;
                        case "actionbar":
                        default:
                            updateActionbar(player, data);
                            break;
                    }
                }
            }
        }.runTaskTimer(plugin, 20L * interval, 20L * interval);
    }

    private void updateActionbar(Player player, PlayerData data) {
        int limit = plugin.getLimit(player);
        int used = data.dailyUsed;
        int remaining = Math.max(0, limit - used);
        double percent = Math.min(100.0, (double) used / limit * 100);

        player.sendActionBar(
            com.darkun7.limiter.util.MessageUtil.format(
                "messages.hud",
                "&ePlaytime: &f{used}/{limit} min ({percent}%%) - &a{remaining} min left",
                "used", String.valueOf(used),
                "limit", String.valueOf(limit),
                "percent", String.format("%.0f", percent),
                "remaining", String.valueOf(remaining)
            )
        );
    }

    private void updateTablist(Player player, PlayerData data) {
        int limit = plugin.getLimit(player);
        int used = data.dailyUsed;
        int remaining = Math.max(0, limit - used);
        double percent = Math.min(100.0, (double) used / limit * 100);

        String header = "§a§lPlayTimeLimiter";
        String footer = String.format("§eUsed: §f%d/%d min §7(§f%.0f%%§7)\n§aRemaining: §f%d min",
                used, limit, percent, remaining);

        player.setPlayerListHeader(header);
        player.setPlayerListFooter(footer);
    }

    private void updateScoreboard(Player player, PlayerData data) {
        int limit = plugin.getLimit(player);
        int used = data.dailyUsed;
        int remaining = Math.max(0, limit - used);
        double percent = Math.min(100.0, (double) used / limit * 100);

        var scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        var objective = scoreboard.registerNewObjective("ptl_" + player.getName(), "dummy", "§aPlayTimeLimiter");
        objective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);

        objective.getScore("§fUsed: §e" + used + "/" + limit + " min").setScore(3);
        objective.getScore("§fPercent: §e" + String.format("%.0f", percent) + "%").setScore(2);
        objective.getScore("§fLeft: §a" + remaining + " min").setScore(1);

        player.setScoreboard(scoreboard);
    }

}
