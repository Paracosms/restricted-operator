package io.papermc.restrictedOperator.listener;

import io.papermc.restrictedOperator.CommandCheckResult;
import io.papermc.restrictedOperator.CommandSourceType;
import io.papermc.restrictedOperator.PermissionNodes;
import io.papermc.restrictedOperator.RestrictedOperatorPlugin;
import io.papermc.restrictedOperator.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class CommandListener implements Listener {
    private final RestrictedOperatorPlugin plugin;
    private final ConfigManager configManager;

    public CommandListener(RestrictedOperatorPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!configManager.isPlayerCommandsEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        if (configManager.isBypassUsername(player.getName())) {
            return;
        }

        CommandCheckResult result = configManager.getCommandFilter().check(event.getMessage(), CommandSourceType.PLAYER);
        if (result.allowed()) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(configManager.getBlockedPlayerCommandMessage());

        if (configManager.shouldLogBlockedCommands()) {
            plugin.getLogger().info(String.format(
                    "Blocked player command from %s: reason=%s, matched=%s, command=%s",
                    player.getName(),
                    result.reason(),
                    result.matchedPattern(),
                    event.getMessage()
            ));
        }

        if (configManager.shouldNotifyInstructors()) {
            String notifyMessage = ChatColor.YELLOW + "[RestrictedOperator] " + ChatColor.WHITE
                    + "Blocked " + player.getName() + ": " + result.reason() + "; command="
                    + ChatColor.GRAY + event.getMessage();
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                if (onlinePlayer.hasPermission(PermissionNodes.NOTIFY)) {
                    onlinePlayer.sendMessage(notifyMessage);
                }
            }
            sendConsoleNotification(plugin.getServer().getConsoleSender(), notifyMessage);
        }
    }

    private void sendConsoleNotification(CommandSender console, String message) {
        console.sendMessage(message);
    }
}
