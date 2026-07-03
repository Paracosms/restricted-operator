// Adds the following command for console and bypass-usernames:
// /restrictedoperator reload
// This command reloads the config file

package io.papermc.restrictedOperator.commands.restrictedoperator;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.restrictedOperator.PermissionNodes;
import io.papermc.restrictedOperator.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

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
                        }))
                .then(Commands.literal("config")
                        .requires(source -> canUseConfig(source.getSender(), configManager))
                        .then(createAddCommand(configManager))
                        .then(createRemoveCommand(configManager))
                        .then(createListCommand(configManager)));
    }

    // the following command can be executed by bypass-usernames only.
    // /restrictedoperator config add/remove/list root/namespace/selector "String"
    // add: add a config entry "String" to the category and reload. if "String" already exists, send a message in the player's chat: ""String" already exists in [category]."
    // remove: remove a config entry "String" to the category and reload. if "String" does not exist, send a message in the player's chat: "There is no "String" in [category]."
    // root: the token after this will be added/removed from "blocked-roots:" in the config.yml.
    // namespace: the token after this will be added/removed from "blocked-namespaces:" in the config.yml
    // selector: the token after this will be added/removed from "blocked-selectors:" in the config.yml. The only options are [@p, @r, @a, @e, @s, @n]

    private static LiteralArgumentBuilder<CommandSourceStack> createAddCommand(ConfigManager configManager) {
        return Commands.literal("add")
                .then(createNameCategoryCommand("root", true, configManager::addBlockedRoot, configManager))
                .then(createNameCategoryCommand("namespace", true, configManager::addBlockedNamespace, configManager))
                .then(Commands.literal("selector")
                        .then(createSelectorLiteral("@p", sender -> updateSelector(sender, configManager, true, "@p")))
                        .then(createSelectorLiteral("@r", sender -> updateSelector(sender, configManager, true, "@r")))
                        .then(createSelectorLiteral("@a", sender -> updateSelector(sender, configManager, true, "@a")))
                        .then(createSelectorLiteral("@e", sender -> updateSelector(sender, configManager, true, "@e")))
                        .then(createSelectorLiteral("@s", sender -> updateSelector(sender, configManager, true, "@s")))
                        .then(createSelectorLiteral("@n", sender -> updateSelector(sender, configManager, true, "@n"))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createRemoveCommand(ConfigManager configManager) {
        return Commands.literal("remove")
                .then(createNameCategoryCommand("root", false, configManager::removeBlockedRoot, configManager))
                .then(createNameCategoryCommand("namespace", false, configManager::removeBlockedNamespace, configManager))
                .then(Commands.literal("selector")
                        .then(createSelectorLiteral("@p", sender -> updateSelector(sender, configManager, false, "@p")))
                        .then(createSelectorLiteral("@r", sender -> updateSelector(sender, configManager, false, "@r")))
                        .then(createSelectorLiteral("@a", sender -> updateSelector(sender, configManager, false, "@a")))
                        .then(createSelectorLiteral("@e", sender -> updateSelector(sender, configManager, false, "@e")))
                        .then(createSelectorLiteral("@s", sender -> updateSelector(sender, configManager, false, "@s")))
                        .then(createSelectorLiteral("@n", sender -> updateSelector(sender, configManager, false, "@n"))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createListCommand(ConfigManager configManager) {
        return Commands.literal("list")
                .then(Commands.literal("root")
                        .executes(ctx -> listEntries(ctx.getSource().getSender(), "blocked-roots", configManager.getBlockedRoots(), false)))
                .then(Commands.literal("namespace")
                        .executes(ctx -> listEntries(ctx.getSource().getSender(), "blocked-namespaces", configManager.getBlockedNamespaces(), true)))
                .then(Commands.literal("selector")
                        .executes(ctx -> listEntries(ctx.getSource().getSender(), "blocked-selectors", configManager.getBlockedSelectors(), false)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createNameCategoryCommand(
            String categoryName,
            boolean add,
            ConfigUpdate update,
            ConfigManager configManager
    ) {
        return Commands.literal(categoryName)
                .then(Commands.argument("value", StringArgumentType.word())
                        .executes(ctx -> updateNameCategory(
                                ctx.getSource().getSender(),
                                categoryName,
                                add,
                                StringArgumentType.getString(ctx, "value"),
                                update,
                                configManager
                        )));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createSelectorLiteral(String selector, Consumer<CommandSender> action) {
        return Commands.literal(selector)
                .executes(ctx -> {
                    action.accept(ctx.getSource().getSender());
                    return Command.SINGLE_SUCCESS;
                });
    }

    private static int updateNameCategory(
            CommandSender sender,
            String categoryName,
            boolean add,
            String input,
            ConfigUpdate update,
            ConfigManager configManager
    ) {
        String normalized = normalizeValue(input);
        boolean changed = update.update(normalized);
        return handleUpdateResult(sender, configManager, input, categoryName, changed, add);
    }

    private static int updateSelector(CommandSender sender, ConfigManager configManager, boolean add, String selector) {
        boolean changed = add ? configManager.addBlockedSelector(selector) : configManager.removeBlockedSelector(selector);
        return handleUpdateResult(sender, configManager, selector, "selector", changed, add);
    }

    private static int handleUpdateResult(
            CommandSender sender,
            ConfigManager configManager,
            String input,
            String categoryName,
            boolean changed,
            boolean add
    ) {
        if (!changed) {
            sender.sendMessage(Component.text(
                    add
                            ? "\"" + input + "\" already exists in " + categoryName
                            : "There is no \"" + input + "\" in " + categoryName,
                    NamedTextColor.RED
            ));
            return Command.SINGLE_SUCCESS;
        }

        configManager.reload();
        sender.sendMessage(add
                ? "Added \"" + input + "\" to " + categoryName
                : "Removed \"" + input + "\" from " + categoryName);
        return Command.SINGLE_SUCCESS;
    }

    private static int listEntries(CommandSender sender, String header, List<String> values, boolean namespaceDisplay) {
        StringBuilder message = new StringBuilder(header).append(':');
        for (String value : values) {
            message.append('\n').append("- ").append(formatListedValue(value, namespaceDisplay));
        }
        sender.sendMessage(message.toString());
        return Command.SINGLE_SUCCESS;
    }

    static boolean canUseConfig(CommandSender sender, ConfigManager configManager) {
        return canUseConfig(
                sender instanceof ConsoleCommandSender,
                sender instanceof RemoteConsoleCommandSender,
                sender instanceof Player player && configManager.isBypassUsername(player.getName())
        );
    }

    static boolean canUseConfig(boolean isConsole, boolean isRemoteConsole, boolean hasBypassUsername) {
        return isConsole || isRemoteConsole || hasBypassUsername;
    }

    static String normalizeValue(String input) {
        return input.toLowerCase(Locale.ROOT);
    }

    static String formatListedValue(String value, boolean namespaceDisplay) {
        if (namespaceDisplay && !value.endsWith(":")) {
            return "/" + value + ":";
        }
        if (!namespaceDisplay && !value.startsWith("@")) {
            return "/" + value;
        }
        return value;
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

    @FunctionalInterface
    private interface ConfigUpdate {
        boolean update(String value);
    }
}
