// Catches commands at runtime and restricts accordingly

package io.papermc.restrictedOperator.listener;

import io.papermc.restrictedOperator.CommandCheckResult;
import io.papermc.restrictedOperator.CommandSourceType;
import io.papermc.restrictedOperator.PermissionNodes;
import io.papermc.restrictedOperator.RestrictedOperatorPlugin;
import io.papermc.restrictedOperator.commands.unrestrict.CommandBlockTrustService;
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

    public CommandListener(
            RestrictedOperatorPlugin plugin,
            ConfigManager configManager,
            CommandBlockTrustService trustService,
            CommandBlockAttributionService attributionService
    ) {
        this.support = new CommandListenerSupport(plugin, configManager, trustService, attributionService);
    }

    // Restricts chat commands
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!support.isPlayerChatCommandsFilterEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        if (player.hasPermission(PermissionNodes.BYPASS_USERS) || support.isBypassUsername(player.getName())) {
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

        // Restricts console commands
        if (sender instanceof ConsoleCommandSender  || sender instanceof RemoteConsoleCommandSender) {
            if (!support.isConsoleCommandsFilterEnabled()) {
                return;
            }
            CommandCheckResult result = support.getCommandFilter().check(event.getCommand(), CommandSourceType.CONSOLE);
            if (result.allowed()) {
                return;
            }

            event.setCancelled(true);
            support.notifyBlockedCommandSource(
                    "console",
                    "console",
                    null,
                    event.getCommand(),
                    result,
                    null
            );
            return;
        }

        // Restricts command block commands
        if (sender instanceof BlockCommandSender) {
            if (!support.isCommandBlocksFilterEnabled()) {
                return;
            }

            if (support.isTrustedCommandBlock((BlockCommandSender) sender)) {
                return;
            }

            CommandCheckResult result = support.getCommandFilter().check(event.getCommand(), CommandSourceType.COMMAND_BLOCK);
            if (result.allowed()) {
                return;
            }

            event.setCancelled(true);
            BlockCommandSender blockSender = (BlockCommandSender) sender;
            support.notifyBlockedCommandSource(
                    support.createBlockSourceKey(blockSender.getBlock().getLocation()),
                    "command block",
                    blockSender.getBlock().getLocation(),
                    event.getCommand(),
                    result,
                    support.findLastKnownEditor(blockSender.getBlock().getLocation())
            );
            return;
        }

        // Restricts command block minecart commands
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
                    result,
                    null
            );
        }
    }
}
