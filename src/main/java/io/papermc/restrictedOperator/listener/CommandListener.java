// Catches commands at runtime and restricts accordingly

package io.papermc.restrictedOperator.listener;

import io.papermc.restrictedOperator.CommandCheckResult;
import io.papermc.restrictedOperator.CommandSourceType;
import io.papermc.restrictedOperator.PermissionNodes;
import io.papermc.restrictedOperator.RestrictedOperatorPlugin;
import io.papermc.restrictedOperator.config.ConfigManager;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public final class CommandListener implements Listener {
    private final CommandListenerSupport support;

    public CommandListener(RestrictedOperatorPlugin plugin, ConfigManager configManager) {
        this.support = new CommandListenerSupport(plugin, configManager);
    }

    // Restricts chat commands
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!support.isPlayerChatCommandsFilterEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        if (support.isBypassUsername(player.getName())) {
            return;
        }

        CommandCheckResult result = support.getCommandFilter().check(event.getMessage(), CommandSourceType.PLAYER);
        if (result.allowed()) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(support.getBlockedPlayerCommandMessage());

        support.notifyBlockedPlayerCommand(player, event.getMessage(), result);
    }

    // Restricts commands sent by command block/command block minecart/console
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommandPreprocess(ServerCommandEvent event) {
        CommandSender sender = event.getSender();
        if (sender instanceof ConsoleCommandSender  || sender instanceof RemoteConsoleCommandSender) {
            return;
        }

        if (sender instanceof BlockCommandSender) {
            if (!support.isCommandBlocksFilterEnabled()) {
                return;
            }

            CommandCheckResult result = support.getCommandFilter().check(event.getCommand(), CommandSourceType.COMMAND_BLOCK);
            if (result.allowed()) {
                return;
            }

            // Check if /unrestrict command has been used [IMPLEMENT LATER, DO NOT IMPLEMENT IF NOT EXPLICITLY TOLD TO DO SO]

            event.setCancelled(true);
            support.notifyBlockedCommandSource(
                    support.createBlockSourceKey(((BlockCommandSender) sender).getBlock().getLocation()),
                    "command block",
                    ((BlockCommandSender) sender).getBlock().getLocation(),
                    event.getCommand(),
                    result
            );
            return;
        }

        if (sender instanceof CommandMinecart) {
            if (!support.isCommandBlockMinecartsFilterEnabled()) {
                return;
            }

            CommandCheckResult result = support.getCommandFilter().check(event.getCommand(), CommandSourceType.COMMAND_BLOCK_MINECART);
            if (result.allowed()) {
                return;
            }

            event.setCancelled(true);
            support.notifyBlockedCommandSource(
                    support.createMinecartSourceKey((CommandMinecart) sender),
                    "command block minecart",
                    ((CommandMinecart) sender).getLocation(),
                    event.getCommand(),
                    result
            );
        }
    }
}
