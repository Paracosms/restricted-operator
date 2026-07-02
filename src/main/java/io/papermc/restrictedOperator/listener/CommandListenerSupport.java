package io.papermc.restrictedOperator.listener;

import io.papermc.restrictedOperator.CommandCheckResult;
import io.papermc.restrictedOperator.PermissionNodes;
import io.papermc.restrictedOperator.RestrictedOperatorPlugin;
import io.papermc.restrictedOperator.commands.unrestrict.CommandBlockTrustService;
import io.papermc.restrictedOperator.config.ConfigManager;
import io.papermc.restrictedOperator.filter.CommandFilter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class CommandListenerSupport {
    private static final LegacyComponentSerializer LEGACY_AMPERSAND = LegacyComponentSerializer.legacyAmpersand();

    private final RestrictedOperatorPlugin plugin;
    private final ConfigManager configManager;
    private final CommandBlockTrustService trustService;
    private final CommandBlockAttributionService attributionService;
    private final Map<String, Long> lastNotificationMillisBySource = new HashMap<>();

    CommandListenerSupport(
            RestrictedOperatorPlugin plugin,
            ConfigManager configManager,
            CommandBlockTrustService trustService,
            CommandBlockAttributionService attributionService
    ) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.trustService = trustService;
        this.attributionService = attributionService;
    }

    CommandFilter getCommandFilter() {
        return configManager.getCommandFilter();
    }

    boolean isPlayerChatCommandsFilterEnabled() {
        return configManager.isPlayerChatCommandsFilterEnabled();
    }

    boolean isBypassUsername(String username) {
        return configManager.isBypassUsername(username);
    }

    String getBlockedPlayerCommandMessage() {
        return configManager.getBlockedPlayerCommandMessage();
    }

    boolean shouldLogBlockedCommands() {
        return configManager.shouldLogBlockedCommands();
    }

    boolean shouldNotifyInstructors() {
        return configManager.shouldNotifyInstructors();
    }

    boolean isNotifyUsername(String username) {
        return configManager.isNotifyUsername(username);
    }

    boolean isCommandBlocksFilterEnabled() {
        return configManager.isCommandBlocksFilterEnabled();
    }

    boolean isCommandBlockMinecartsFilterEnabled() {
        return configManager.isCommandBlockMinecartsFilterEnabled();
    }

    boolean isConsoleCommandsFilterEnabled() {
        return configManager.isConsoleCommandsFilterEnabled();
    }

    boolean isTrustedCommandBlock(BlockCommandSender sender) {
        return trustService.isTrusted(sender);
    }

    Player findLastKnownEditor(Location location) {
        if (!configManager.shouldNotifyLastKnownEditor()) {
            return null;
        }

        long maxAgeMillis = configManager.getEditorHintExpireMinutes() * 60_000L;
        UUID trackedEditorId = attributionService.findTrackedEditorId(location, maxAgeMillis);
        if (trackedEditorId == null) {
            return null;
        }

        return plugin.getServer().getPlayer(trackedEditorId);
    }

    void notifyBlockedPlayerCommand(Player player, String command, CommandCheckResult result) {
        if (shouldLogBlockedCommands()) {
            plugin.getLogger().info(String.format(
                    "Blocked player command from %s: reason=%s, matched=%s, command=%s",
                    player.getName(),
                    result.reason(),
                    result.matchedPattern(),
                    command
            ));
        }

        if (shouldNotifyInstructors()) {
            Component notifyMessage = Component.text()
                    .append(Component.text("[RestrictedOperator] ", NamedTextColor.YELLOW))
                    .append(Component.text("Blocked ", NamedTextColor.WHITE))
                    .append(Component.text(player.getName(), NamedTextColor.WHITE))
                    .append(Component.text(": " + result.reason() + "; command=", NamedTextColor.WHITE))
                    .append(Component.text(command, NamedTextColor.GRAY))
                    .build();
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                if (onlinePlayer.hasPermission(PermissionNodes.NOTIFY) || isNotifyUsername(onlinePlayer.getName())) {
                    onlinePlayer.sendMessage(notifyMessage);
                }
            }
            sendConsoleNotification(plugin.getServer().getConsoleSender(), notifyMessage);
        }
    }

    void notifyBlockedCommandSource(
            String sourceKey,
            String sourceType,
            Location location,
            String command,
            CommandCheckResult result,
            Player lastKnownEditor
    ) {
        if (isOnCooldown(sourceKey)) {
            return;
        }

        String locationText = formatLocation(location);
        String rawCommand = command == null ? "" : command;

        if (shouldLogBlockedCommands()) {
            plugin.getLogger().info(String.format(
                    "Blocked %s at %s: reason=%s, matched=%s, command=%s",
                    sourceType,
                    locationText,
                    result.reason(),
                    result.matchedPattern(),
                    rawCommand
            ));
        }

        if (shouldNotifyInstructors()) {
            Component instructorMessageComponent = formatBlockedInstructorNotifyComponent(sourceType, location, rawCommand, result);
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                if (onlinePlayer.hasPermission(PermissionNodes.NOTIFY) || isNotifyUsername(onlinePlayer.getName())) {
                    onlinePlayer.sendMessage(instructorMessageComponent);
                }
            }
            sendConsoleNotification(plugin.getServer().getConsoleSender(), instructorMessageComponent);
        }

        if (configManager.shouldNotifyNearbyPlayers() && location != null && location.getWorld() != null) {
            Component nearbyMessage = deserializeLegacy(configManager.getBlockedCommandBlockNearbyMessage());
            for (Player nearbyPlayer : location.getWorld().getNearbyPlayers(
                    location,
                    configManager.getNearbyRadiusBlocks(),
                    configManager.getNearbyRadiusBlocks(),
                    configManager.getNearbyRadiusBlocks()
            )) {
                if (lastKnownEditor != null && nearbyPlayer.getUniqueId().equals(lastKnownEditor.getUniqueId())) {
                    continue;
                }
                nearbyPlayer.sendMessage(nearbyMessage);
            }
        }

        if (lastKnownEditor != null) {
            lastKnownEditor.sendMessage(deserializeLegacy(configManager.getBlockedCommandBlockEditorMessage()));
        }
    }

    String createBlockSourceKey(Location location) {
        if (location == null || location.getWorld() == null) {
            return "unknown";
        }

        return location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }

    String createMinecartSourceKey(CommandMinecart minecart) {
        Location location = minecart.getLocation();
        if (location.getWorld() == null) {
            return minecart.getUniqueId().toString();
        }

        return location.getWorld().getName() + ":" + minecart.getUniqueId();
    }

    private boolean isOnCooldown(String sourceKey) {
        long cooldownMillis = configManager.getNotificationCooldownSecondsPerSource() * 1000L;
        if (cooldownMillis <= 0L) {
            return false;
        }

        long now = System.currentTimeMillis();
        Long lastNotification = lastNotificationMillisBySource.get(sourceKey);
        if (lastNotification != null && now - lastNotification < cooldownMillis) {
            return true;
        }

        lastNotificationMillisBySource.put(sourceKey, now);
        return false;
    }

    private String formatLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return "unknown location";
        }

        return "x: " + location.getBlockX() + " y: " + location.getBlockY() + " z: " + location.getBlockZ();
    }

    private Component formatBlockedInstructorNotifyComponent(String sourceType, Location location, String command, CommandCheckResult result) {
        String locationText = formatLocation(location);
        String message = configManager.getBlockedInstructorNotifyMessage()
                .replace("{source}", sourceType)
                .replace("{reason}", String.valueOf(result.reason()))
                .replace("{command}", command);

        int locationPlaceholderIndex = message.indexOf("{location}");
        if (locationPlaceholderIndex < 0) {
            return deserializeLegacy(message.replace("{location}", locationText));
        }

        String beforeLocation = message.substring(0, locationPlaceholderIndex);
        String afterLocation = message.substring(locationPlaceholderIndex + "{location}".length());
        Component clickableLocation = createClickableLocationComponent(location, locationText);

        return deserializeLegacy(beforeLocation)
                .append(clickableLocation)
                .append(deserializeLegacy(afterLocation));
    }

    private Component createClickableLocationComponent(Location location, String locationText) {
        if (location == null || location.getWorld() == null) {
            return Component.text(locationText);
        }

        String worldKey = location.getWorld().getKey().toString();
        String tpCommand = "/execute in " + worldKey + " run tp @s " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
        return Component.text(locationText)
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.suggestCommand(tpCommand));
    }

    private void sendConsoleNotification(CommandSender console, Component message) {
        console.sendMessage(message);
    }

    private Component deserializeLegacy(String message) {
        return LEGACY_AMPERSAND.deserialize(message);
    }
}
