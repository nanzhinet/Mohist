package com.mohistmc.mohist.plugins;

import com.mohistmc.mohist.api.WorldAPI;
import com.mohistmc.mohist.plugins.world.WorldDate;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MohistPapiHook extends PlaceholderExpansion {

    private static final String hook_name = "mohist";

    public static void init() {
        new MohistPapiHook().register();
    }

    public static String replace(OfflinePlayer player, String x) {
        return PlaceholderAPI.setPlaceholders(player, x.replace("&", "§"));
    }

    @Override
    public String onPlaceholderRequest(Player p, @NotNull String i) {
        if (p == null) {
            return null;
        }
        if (i.equalsIgnoreCase("world_name")) {
            return WorldAPI.getDate(p.getWorld(), WorldDate.NAME).replace("&", "§");
        }

        return null;
    }

    @Override
    public @NotNull String getIdentifier() {
        return hook_name;
    }

    @Override
    public @NotNull String getAuthor() {
        return "MohistMC";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.20.1";
    }
}
