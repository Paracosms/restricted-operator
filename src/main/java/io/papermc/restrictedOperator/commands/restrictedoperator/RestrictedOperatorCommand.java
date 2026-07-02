// Adds the following command for console and bypass-usernames:
// /restrictedoperator reload
// This command reloads the config file

package io.papermc.restrictedOperator.commands.restrictedoperator;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.restrictedOperator.PermissionNodes;
import io.papermc.restrictedOperator.config.ConfigManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;

public final class RestrictedOperatorCommand {
    private RestrictedOperatorCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> create(ConfigManager configManager) {
        return Commands.literal("restrictedoperator")
                .requires(source -> canUse(source.getSender(), configManager))
                .then(Commands.literal("reload")
                        .executes(ctx -> {
                            configManager.reload();
                            ctx.getSource().getSender().sendMessage("Reloaded RestrictedOperator config.");
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    static boolean canUse(CommandSender sender, ConfigManager configManager) {
        return canUse(
                sender instanceof ConsoleCommandSender,
                sender instanceof RemoteConsoleCommandSender,
                sender.hasPermission(PermissionNodes.BYPASS_USERS),
                sender instanceof Player player && configManager.isBypassUsername(player.getName())
        );
    }

    static boolean canUse(
            boolean isConsole,
            boolean isRemoteConsole,
            boolean hasBypassPermission,
            boolean hasBypassUsername
    ) {
        return isConsole
                || isRemoteConsole
                || hasBypassPermission
                || hasBypassUsername;
    }
}
