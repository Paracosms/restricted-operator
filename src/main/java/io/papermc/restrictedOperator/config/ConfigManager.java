// Bridge between config.yml and java classes

package io.papermc.restrictedOperator.config;

import io.papermc.restrictedOperator.filter.CommandFilter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ConfigManager {
    private final JavaPlugin plugin;

    private boolean playerChatCommandsFilterEnabled;
    private boolean commandBlocksFilterEnabled;
    private boolean commandBlockMinecartsFilterEnabled;
    private Set<String> bypassUsernames;
    private String blockedPlayerCommandMessage;
    private String blockedCommandBlockNearbyMessage;
    private String blockedInstructorNotifyMessage;
    private boolean logBlockedCommands;
    private boolean notifyInstructors;
    private boolean notifyNearbyPlayers;
    private int nearbyRadiusBlocks;
    private long notificationCooldownSecondsPerSource;
    private CommandFilter commandFilter;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        playerChatCommandsFilterEnabled = config.getBoolean("filter.player-chat-commands", true);
        commandBlocksFilterEnabled = config.getBoolean("filter.command-blocks", true);
        commandBlockMinecartsFilterEnabled = config.getBoolean("filter.command-block-minecarts", true);
        bypassUsernames = normalizeList(config.getStringList("bypass-usernames"));
        blockedPlayerCommandMessage = config.getString(
                "messages.blocked-player-command",
                "That command is disabled." // hardcoded fallback string
        );
        blockedCommandBlockNearbyMessage = config.getString(
                "messages.blocked-command-block-nearby",
                config.getString("messages.blocked-command-block", "A command block command was blocked.")
        );
        blockedInstructorNotifyMessage = config.getString(
                "messages.blocked-instructor-notify",
                "&e[RestrictedOperator]&f Blocked {source} at {location}. reason=[{reason}] command=&7{command}"
        );
        logBlockedCommands = config.getBoolean("logging.log-blocked-commands", true);
        notifyInstructors = config.contains("notifications.notify-instructors")
                ? config.getBoolean("notifications.notify-instructors", true)
                : config.getBoolean("logging.notify-instructors", true);
        notifyNearbyPlayers = config.getBoolean("notifications.notify-nearby-players", true);
        nearbyRadiusBlocks = config.getInt("notifications.nearby-radius-blocks", 16);
        notificationCooldownSecondsPerSource = config.getLong("notifications.cooldown-seconds-per-source", 10L);

        boolean normalizeRootsLowercase = config.getBoolean("filter.normalize-roots-lowercase", true);
        Set<String> blockedRoots = normalizeList(config.getStringList("blocked-roots"));
        Set<String> blockedSelectors = normalizeList(config.getStringList("blocked-selectors"));
        commandFilter = new CommandFilter(blockedRoots, blockedSelectors, normalizeRootsLowercase);
    }

    public boolean isPlayerChatCommandsFilterEnabled() {
        return playerChatCommandsFilterEnabled;
    }

    public boolean isCommandBlocksFilterEnabled() {
        return commandBlocksFilterEnabled;
    }

    public boolean isCommandBlockMinecartsFilterEnabled() {
        return commandBlockMinecartsFilterEnabled;
    }

    public String getBlockedPlayerCommandMessage() {
        return blockedPlayerCommandMessage;
    }

    public String getBlockedCommandBlockNearbyMessage() {
        return blockedCommandBlockNearbyMessage;
    }

    public String getBlockedInstructorNotifyMessage() {
        return blockedInstructorNotifyMessage;
    }

    public boolean isBypassUsername(String username) {
        return bypassUsernames.contains(username.toLowerCase(Locale.ROOT));
    }

    public boolean shouldLogBlockedCommands() {
        return logBlockedCommands;
    }

    public boolean shouldNotifyInstructors() {
        return notifyInstructors;
    }

    public boolean shouldNotifyNearbyPlayers() {
        return notifyNearbyPlayers;
    }

    public int getNearbyRadiusBlocks() {
        return nearbyRadiusBlocks;
    }

    public long getNotificationCooldownSecondsPerSource() {
        return notificationCooldownSecondsPerSource;
    }

    public CommandFilter getCommandFilter() {
        return commandFilter;
    }

    private Set<String> normalizeList(List<String> values) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                normalized.add(trimmed.toLowerCase(Locale.ROOT));
            }
        }
        return normalized;
    }
}
