package com.darkun7.limiter.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlaytimeTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(
        CommandSender sender,
        Command command,
        String alias,
        String[] args
    ) {
        if (args.length == 1) {
            return Arrays.asList("top", "hud", "grant" ,"set", "reset", "reload");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("hud")) {
            return Arrays.asList("actionbar", "scoreboard", "tablist");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return null; // Bukkit will suggest player names
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("grant")) {
            return null; // Bukkit will suggest player names
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            return Collections.singletonList("<minutes>");
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("grant")) {
            return Collections.singletonList("<minutes>");
        }

        return Collections.emptyList();
    }
}
