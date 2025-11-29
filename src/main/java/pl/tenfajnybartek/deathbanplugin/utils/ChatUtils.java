package pl.tenfajnybartek.deathbanplugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatUtils {
    private static BukkitAudiences adventure;

    public static void init(JavaPlugin plugin) {
        if (adventure == null) adventure = BukkitAudiences.create(plugin);
    }

    public static Component color(String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (adventure == null) return;
        adventure.sender(sender).sendMessage(color(message));
    }

    public static void close() {
        if (adventure != null) adventure.close();
    }
}