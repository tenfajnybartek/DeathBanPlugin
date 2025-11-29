package pl.tenfajnybartek.deathbanplugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

public class ChatUtils {

    public static Component color(String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(color(message)); // Paper 1.19+ obsługuje to natywnie
    }
}