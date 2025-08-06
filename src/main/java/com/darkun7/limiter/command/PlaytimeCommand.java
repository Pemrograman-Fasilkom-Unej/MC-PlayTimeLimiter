package com.darkun7.limiter.command;

import com.darkun7.limiter.PlayTimeLimiter;
import com.darkun7.limiter.data.PlayerData;
import com.darkun7.limiter.data.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;

import java.util.*;

public class PlaytimeCommand implements CommandExecutor {

    private final PlayerDataManager dataManager = PlayTimeLimiter.getDataManager();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Show self playtime
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players can use this.");
                return true;
            }

            PlayerData data = dataManager.getData(player.getUniqueId());
            if (data == null) return true;

            int limit = PlayTimeLimiter.getInstance().getLimit(player);
            player.sendMessage("§6Daily used: §f" + data.dailyUsed + "§7/" + limit + " min");
            player.sendMessage("§6Daily extra: §f" + data.dailyExtra + " min");
            player.sendMessage("§6Total used: §f" + data.totalUsed + " min");
            player.sendMessage("§6Today death count: §f" + data.dailyDeath);
            return true;
        }

        // /playtime hud
        if (args.length == 1 && args[0].equalsIgnoreCase("hud")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players can toggle HUD.");
                return true;
            }

            PlayerData data = dataManager.getData(player.getUniqueId());
            if (data == null) return true;

            data.hudEnabled = !data.hudEnabled;
            player.sendMessage("§6HUD " + (data.hudEnabled ? "§aenabled" : "§cdisabled"));
            return true;
        }

        // /playtime hud <actionbar/tablist/scoreboard>
        if (args.length == 2 && args[0].equalsIgnoreCase("hud")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
            }

            Player player = (Player) sender;
            String type = args[1].toLowerCase();

            if (!type.equals("actionbar") && !type.equals("tablist") && !type.equals("scoreboard")) {
                player.sendMessage("§cInvalid HUD type. Use: actionbar, tablist, scoreboard.");
                return true;
            }

            PlayTimeLimiter plugin = PlayTimeLimiter.getInstance();
            PlayerData data = plugin.getDataManager().getData(player.getUniqueId());

            if (data == null) {
                player.sendMessage("§cUnable to update HUD type — data not loaded.");
                return true;
            }

            data.hudType = type;
            plugin.getDataManager().savePlayer(player.getUniqueId());

            player.sendMessage("§aHUD display type updated to §f" + type + "§a.");
            return true;
        }

        // /playtime grant <player> <minutes>
        if (args.length == 3 && args[0].equalsIgnoreCase("grant")) {
            Player player = (Player) sender;
            PlayerData selfData = dataManager.getData(player.getUniqueId());
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            int mins;
            try {
                mins = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid minutes.");
                return true;
            }
            if (mins > selfData.dailyExtra) {
                sender.sendMessage("§cMax limit based on extra time (" + selfData.dailyExtra + ").");
                return true;
            }

            UUID uuid = target.getUniqueId();
            PlayerData data = dataManager.getData(uuid);
            if (data == null) {
                data = new PlayerData(uuid);
                dataManager.getDataMap().put(uuid, data);
            }

            data.dailyExtra += mins;
            selfData.dailyExtra -= mins;
            dataManager.savePlayer(uuid);
            sender.sendMessage("§aGrant §f " + mins + "§a minutes extra time today to §f" + target.getName() + "§a.");
            if (target.isOnline()) {
                ((Player) target).sendMessage("§b" + player.getName() + "§a sent you §f" + mins + "§a minutes extra time today.");
            }
            return true;
        }

        // /playtime top
        if (args.length == 1 && args[0].equalsIgnoreCase("top")) {
            Map<UUID, Integer> totals = new HashMap<>();

            for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
                UUID uuid = offline.getUniqueId();
                if (!offline.isOnline()) {
                    dataManager.loadPlayerByUUID(uuid);
                }
                PlayerData data = dataManager.getData(uuid);
                if (data != null) {
                    totals.put(uuid, data.totalUsed);
                }
                if (!offline.isOnline()) {
                    dataManager.unloadPlayer(offline.getUniqueId());
                }
            }

            List<Map.Entry<UUID, Integer>> sorted = new ArrayList<>(totals.entrySet());
            sorted.sort((a, b) -> b.getValue() - a.getValue());

            sender.sendMessage("§eTop Playtime Players:");
            int i = 1;
            for (Map.Entry<UUID, Integer> entry : sorted.subList(0, Math.min(10, sorted.size()))) {
                String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                sender.sendMessage("§7" + i++ + ". §f" + name + " §8- §b" + entry.getValue() + " min");
            }

            return true;
        }

        // /playtime reset <player>
        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            if (!sender.hasPermission("playtimelimiter.admin")) {
                sender.sendMessage("§cNo permission.");
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            UUID uuid = target.getUniqueId();

            PlayerData data = dataManager.getData(uuid);
            if (data == null) {
                data = new PlayerData(uuid);
                dataManager.getDataMap().put(uuid, data);
            }

            data.dailyUsed = 0;
            data.dailyExtra = 0;
            // data.totalUsed = 0;
            data.dailyClaimed.clear();
            // data.totalClaimed.clear();
            data.dailyDeath = 0;
            dataManager.savePlayer(uuid);

            sender.sendMessage("§aPlaytime reset for §f" + target.getName());
            return true;
        }

        // /playtime set <player> <minutes>
        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            if (!sender.hasPermission("playtimelimiter.admin")) {
                sender.sendMessage("§cNo permission.");
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            int mins;
            try {
                mins = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid minutes.");
                return true;
            }

            UUID uuid = target.getUniqueId();
            PlayerData data = dataManager.getData(uuid);
            if (data == null) {
                data = new PlayerData(uuid);
                dataManager.getDataMap().put(uuid, data);
            }

            data.dailyUsed = mins;
            dataManager.savePlayer(uuid);
            sender.sendMessage("§aSet " + target.getName() + " to " + mins + " minutes used today.");
            return true;
        }

        // /playtime reload
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("playtimelimiter.admin")) {
                sender.sendMessage("§cYou don't have permission to do that.");
                return true;
            }

            PlayTimeLimiter plugin = PlayTimeLimiter.getInstance();

            plugin.reloadConfig(); // reload config.yml
            sender.sendMessage("§aPlayTimeLimiter config reloaded.");

            // Optional: Reload sender data files if you support that
            for (Player online  : Bukkit.getOnlinePlayers()) {
                plugin.getDataManager().savePlayer(online.getUniqueId()); // save current
                plugin.getDataManager().loadPlayer(online);               // reload fresh
            }
            sender.sendMessage("§7All online player data reloaded.");

            return true;
        }

        sender.sendMessage("§cUsage:");
        sender.sendMessage("§e/playtime §7- Check your time");
        sender.sendMessage("§e/playtime hud §7- Toggle HUD");
        sender.sendMessage("§e/playtime grant <player> <minutes> §7- Transfer extra time");
        sender.sendMessage("§e/playtime top §7- Top playtime");
        if (sender.hasPermission("playtimelimiter.admin")) {
            sender.sendMessage("§e/playtime reset <player>");
            sender.sendMessage("§e/playtime reload §7- Reload config");
            sender.sendMessage("§e/playtime set <player> <minutes>");
        }
        return true;
    }
}
