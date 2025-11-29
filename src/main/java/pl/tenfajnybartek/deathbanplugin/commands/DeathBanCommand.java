package pl.tenfajnybartek.deathbanplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import pl.tenfajnybartek.deathbanplugin.base.DeathBanPlugin;
import pl.tenfajnybartek.deathbanplugin.database.Storage;
import pl.tenfajnybartek.deathbanplugin.utils.ChatUtils;

import java.util.List;

public class DeathBanCommand implements CommandExecutor {
    private final DeathBanPlugin plugin;

    public DeathBanCommand(DeathBanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        var configManager = plugin.getConfigManager();

        // Permission check
        if (!sender.hasPermission("deathban.command.use")) {
            ChatUtils.sendMessage(sender,
                    configManager.getMessage("no_permission", "&cNie masz uprawnień."));
            return true;
        }
        // Argument check
        if (args.length < 1 || args.length > 2) {
            ChatUtils.sendMessage(sender,
                    configManager.getMessage("usage", "&7Użycie: &f/deathban <unban|unbanall|checkban> [gracz]"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "unbanall" -> {
                int count = 0;
                for (Storage s : List.copyOf(plugin.getStorageManager().getPlayerList())) {
                    s.delete();
                    count++;
                }
                String msg = configManager.getMessage("unbanall", "&aOdbanowano wszystkich (&e%0&a)");
                ChatUtils.sendMessage(sender, msg.replace("%0", String.valueOf(count)));
            }
            case "unban" -> {
                if (args.length != 2) {
                    ChatUtils.sendMessage(sender,
                            configManager.getMessage("no_player", "&cPodaj nick gracza."));
                    return true;
                }
                Storage s = plugin.getStorageManager().getPlayerByNick(args[1]);
                if (s == null) {
                    String msg = configManager.getMessage("not_banned", "&cGracz &e%0 &cnie jest zbanowany.");
                    ChatUtils.sendMessage(sender, msg.replace("%0", args[1]));
                } else {
                    s.delete();
                    String msg = configManager.getMessage("unban", "&aGracz &e%0 &azostał odbanowany.");
                    ChatUtils.sendMessage(sender, msg.replace("%0", s.getNick()));
                }
            }
            case "checkban" -> {
                if (args.length != 2) {
                    ChatUtils.sendMessage(sender,
                            configManager.getMessage("no_player", "&cPodaj nick gracza."));
                    return true;
                }
                Storage s = plugin.getStorageManager().getPlayerByNick(args[1]);
                if (s == null) {
                    String msg = configManager.getMessage("not_banned", "&cGracz &e%0 &cnie jest zbanowany.");
                    ChatUtils.sendMessage(sender, msg.replace("%0", args[1]));
                } else {
                    String formattedDate = pl.tenfajnybartek.deathbanplugin.utils.DateUtils.formatDate(s.getTime());
                    String msg = configManager.getMessage("ban_check", "&e%0 &ajest zbanowany do &b%1");
                    ChatUtils.sendMessage(sender, msg.replace("%0", s.getNick()).replace("%1", formattedDate));
                }
            }
            case "reload" -> {
                configManager.reload();
                ChatUtils.sendMessage(sender,
                        configManager.getMessage("reload", "&aKonfiguracja została przeładowana."));
            }
            default -> ChatUtils.sendMessage(sender,
                    configManager.getMessage("usage", "&7Użycie: &f/deathban <unban|unbanall|checkban> [gracz]"));
        }
        return true;
    }
}