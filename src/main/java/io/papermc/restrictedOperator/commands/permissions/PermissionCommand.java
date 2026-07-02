// Adds the following console-only commands to give permissions to player:
// /bypass add [player]
// /bypass remove [player]
// /notify add [player]
// /notify remove [player]

package io.papermc.restrictedOperator.commands.permissions;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.restrictedOperator.config.ConfigManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;

public final class PermissionCommand {
    private PermissionCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> createBypass(ConfigManager configManager) {
        return create(
                configManager,
                "bypass",
                "restrictedoperator.bypass-usernames",
                configManager::addBypassUsername,
                configManager::removeBypassUsername
        );
    }

    public static LiteralArgumentBuilder<CommandSourceStack> createNotify(ConfigManager configManager) {
        return create(
                configManager,
                "notify",
                "restrictedoperator.notify",
                configManager::addNotifyUsername,
                configManager::removeNotifyUsername
        );
    }

    public static boolean isConsole(CommandSender sender) {
        return sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> create(
            ConfigManager configManager,
            String commandName,
            String permission,
            PermissionUpdate addPermission,
            PermissionUpdate removePermission
    ) {
        return Commands.literal(commandName)
                .requires(source -> isConsole(source.getSender()))
                .then(Commands.literal("add")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .executes(ctx -> {
                                    String playerName = StringArgumentType.getString(ctx, "player");
                                    boolean changed = addPermission.update(playerName);
                                    if (changed) {
                                        configManager.reload();
                                    }
                                    sendMessage(ctx.getSource().getSender(), changed
                                            ? "Added " + permission + " to " + playerName + " in config.yml and reloaded RestrictedOperator config."
                                            : playerName + " already has " + permission + " in config.yml.");
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .executes(ctx -> {
                                    String playerName = StringArgumentType.getString(ctx, "player");
                                    boolean changed = removePermission.update(playerName);
                                    if (changed) {
                                        configManager.reload();
                                    }
                                    sendMessage(ctx.getSource().getSender(), changed
                                            ? "Removed " + permission + " from " + playerName + " in config.yml and reloaded RestrictedOperator config."
                                            : playerName + " does not have " + permission + " in config.yml.");
                                    return Command.SINGLE_SUCCESS;
                                })));
    }

    private static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(message);
    }

    @FunctionalInterface
    private interface PermissionUpdate {
        boolean update(String username);
    }
}
