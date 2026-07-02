package io.papermc.restrictedOperator.listener;

import io.papermc.restrictedOperator.CommandCheckResult;
import io.papermc.restrictedOperator.CommandSourceType;
import io.papermc.restrictedOperator.PermissionNodes;
import io.papermc.restrictedOperator.RestrictedOperatorPlugin;
import io.papermc.restrictedOperator.config.ConfigManager;
import io.papermc.restrictedOperator.filter.CommandFilter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;

import java.util.HashMap;
import java.util.Map;

final class CommandListenerSupport {
    private final RestrictedOperatorPlugin plugin;
    private final ConfigManager configManager;
    private final Map<String, Long> lastNotificationMillisBySource = new HashMap<>();

    CommandListenerSupport(RestrictedOperatorPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
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

    boolean isCommandBlocksFilterEnabled() {
        return configManager.isCommandBlocksFilterEnabled();
    }

    boolean isCommandBlockMinecartsFilterEnabled() {
        return configManager.isCommandBlockMinecartsFilterEnabled();
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
            String notifyMessage = ChatColor.YELLOW + "[RestrictedOperator] " + ChatColor.WHITE
                    + "Blocked " + player.getName() + ": " + result.reason() + "; command="
                    + ChatColor.GRAY + command;
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                if (onlinePlayer.hasPermission(PermissionNodes.NOTIFY)) {
                    onlinePlayer.sendMessage(notifyMessage);
                }
            }
            sendConsoleNotification(plugin.getServer().getConsoleSender(), notifyMessage);
        }
    }

    void notifyBlockedCommandSource(String sourceKey, String sourceType, Location location, String command, CommandCheckResult result) {
        if (isOnCooldown(sourceKey)) {
            return;
        }

        String locationText = formatLocation(location);
        String rawCommand = command == null ? "" : command;
        String instructorMessage = formatBlockedInstructorNotifyMessage(sourceType, locationText, rawCommand, result);

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
                if (onlinePlayer.hasPermission(PermissionNodes.NOTIFY)) {
                    onlinePlayer.sendMessage(instructorMessageComponent);
                }
            }
            sendConsoleNotification(plugin.getServer().getConsoleSender(), instructorMessage);
        }

        if (configManager.shouldNotifyNearbyPlayers() && location != null && location.getWorld() != null) {
            String nearbyMessage = ChatColor.translateAlternateColorCodes('&', configManager.getBlockedCommandBlockNearbyMessage());
            for (Player nearbyPlayer : location.getWorld().getNearbyPlayers(
                    location,
                    configManager.getNearbyRadiusBlocks(),
                    configManager.getNearbyRadiusBlocks(),
                    configManager.getNearbyRadiusBlocks()
            )) {
                nearbyPlayer.sendMessage(nearbyMessage);
            }
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

    private String formatBlockedInstructorNotifyMessage(String sourceType, String locationText, String command, CommandCheckResult result) {
        String message = configManager.getBlockedInstructorNotifyMessage()
                .replace("{source}", sourceType)
                .replace("{location}", locationText)
                .replace("{reason}", String.valueOf(result.reason()))
                .replace("{command}", command);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private Component formatBlockedInstructorNotifyComponent(String sourceType, Location location, String command, CommandCheckResult result) {
        String locationText = formatLocation(location);
        String message = configManager.getBlockedInstructorNotifyMessage()
                .replace("{source}", sourceType)
                .replace("{reason}", String.valueOf(result.reason()))
                .replace("{command}", command);

        int locationPlaceholderIndex = message.indexOf("{location}");
        if (locationPlaceholderIndex < 0) {
            return LegacyComponentSerializer.legacyAmpersand().deserialize(
                    message.replace("{location}", locationText)
            );
        }

        String beforeLocation = message.substring(0, locationPlaceholderIndex);
        String afterLocation = message.substring(locationPlaceholderIndex + "{location}".length());
        Component clickableLocation = createClickableLocationComponent(location, locationText);

        return LegacyComponentSerializer.legacyAmpersand().deserialize(beforeLocation)
                .append(clickableLocation)
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(afterLocation));
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

    private void sendConsoleNotification(CommandSender console, String message) {
        console.sendMessage(message);
    }
}
