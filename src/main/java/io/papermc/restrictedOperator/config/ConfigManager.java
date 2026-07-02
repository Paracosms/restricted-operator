// Bridge between config.yml and java classes

package io.papermc.restrictedOperator.config;

import io.papermc.restrictedOperator.filter.CommandFilter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ConfigManager {
    private final JavaPlugin plugin;

    private boolean playerChatCommandsFilterEnabled;
    private boolean commandBlocksFilterEnabled;
    private boolean commandBlockMinecartsFilterEnabled;
    private boolean consoleCommandsFilterEnabled;
    private Set<String> bypassUsernames;
    private Set<String> notifyUsernames;
    private String blockedPlayerCommandMessage;
    private String blockedCommandBlockNearbyMessage;
    private String blockedCommandBlockEditorMessage;
    private String blockedInstructorNotifyMessage;
    private boolean logBlockedCommands;
    private boolean notifyInstructors;
    private boolean notifyNearbyPlayers;
    private boolean notifyLastKnownEditor;
    private int nearbyRadiusBlocks;
    private long notificationCooldownSecondsPerSource;
    private long trackingPruneIntervalDays;
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
        consoleCommandsFilterEnabled = config.getBoolean("filter.console", false);
        bypassUsernames = normalizeList(config.getStringList("permissions.bypass-usernames"));
        notifyUsernames = normalizeList(config.getStringList("permissions.notify"));

        // hardcoded fallback strings if undefined
        blockedPlayerCommandMessage = config.getString(
                "messages.blocked-player-command",
                "That command is disabled."
        );
        blockedCommandBlockNearbyMessage = config.getString(
                "messages.blocked-command-block-nearby",
                config.getString("messages.blocked-command-block-nearby", "A command block command was blocked.")
        );
        blockedCommandBlockEditorMessage = config.getString(
                "messages.blocked-command-block-editor",
                "A command block you placed or edited tried to run a disabled command."
        );
        blockedInstructorNotifyMessage = config.getString(
                "messages.blocked-instructor-notify",
                "&e[RestrictedOperator]&f Blocked {source} at {location}: Last modified by {player} reason=[{reason}]; command=&7{command}"
        );

        logBlockedCommands = config.getBoolean("logging.log-blocked-commands", true);
        notifyInstructors = config.getBoolean("notifications.notify-instructors", true);
        notifyNearbyPlayers = config.getBoolean("notifications.notify-nearby-players", true);
        notifyLastKnownEditor = config.getBoolean("notifications.notify-last-known-editor", true);
        nearbyRadiusBlocks = config.getInt("notifications.nearby-radius-blocks", 16);
        notificationCooldownSecondsPerSource = config.getLong("notifications.cooldown-seconds-per-source", 10L);
        trackingPruneIntervalDays = config.getLong("attribution.tracking-prune-interval-days", 7L);

        boolean normalizeRootsLowercase = config.getBoolean("filter.normalize-roots-lowercase", true);
        Set<String> blockedRoots = normalizeList(config.getStringList("blocked-roots"));
        Set<String> blockedSelectors = normalizeList(config.getStringList("blocked-selectors"));
        commandFilter = new CommandFilter(blockedRoots, blockedSelectors, normalizeRootsLowercase);

        if (!blockedSelectors.contains("@e")) {
            plugin.getServer().getConsoleSender().sendMessage("WARNING: The current config will allow @e to be used by operators. Permitting the @e selector is considered highly risky.");
        }
        if (consoleCommandsFilterEnabled) {
            plugin.getServer().getConsoleSender().sendMessage("WARNING: The current config restricts console commands. Restricted commands sent through the console will not run.");
        }
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

    public boolean isConsoleCommandsFilterEnabled() {
        return consoleCommandsFilterEnabled;
    }

    public String getBlockedPlayerCommandMessage() {
        return blockedPlayerCommandMessage;
    }

    public String getBlockedCommandBlockNearbyMessage() {
        return blockedCommandBlockNearbyMessage;
    }

    public String getBlockedCommandBlockEditorMessage() {
        return blockedCommandBlockEditorMessage;
    }

    public String getBlockedInstructorNotifyMessage() {
        return blockedInstructorNotifyMessage;
    }

    public boolean isBypassUsername(String username) {
        return bypassUsernames.contains(username.toLowerCase(Locale.ROOT));
    }

    public boolean isNotifyUsername(String username) {
        return notifyUsernames.contains(username.toLowerCase(Locale.ROOT));
    }

    public boolean addBypassUsername(String username) {
        return updateUsernameList("permissions.bypass-users", username, true);
    }

    public boolean removeBypassUsername(String username) {
        return updateUsernameList("permissions.bypass-users", username, false);
    }

    public boolean addNotifyUsername(String username) {
        return updateUsernameList("permissions.notify", username, true);
    }

    public boolean removeNotifyUsername(String username) {
        return updateUsernameList("permissions.notify", username, false);
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

    public boolean shouldNotifyLastKnownEditor() {
        return notifyLastKnownEditor;
    }

    public long getNotificationCooldownSecondsPerSource() {
        return notificationCooldownSecondsPerSource;
    }

    public long getTrackingPruneIntervalDays() {
        return trackingPruneIntervalDays;
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

    private boolean updateUsernameList(String path, String username, boolean add) {
        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        if (normalizedUsername.isEmpty()) {
            return false;
        }

        Set<String> usernames = normalizeList(plugin.getConfig().getStringList(path));
        boolean changed = add ? usernames.add(normalizedUsername) : usernames.remove(normalizedUsername);
        if (!changed) {
            return false;
        }

        plugin.getConfig().set(path, new ArrayList<>(usernames));
        plugin.saveConfig();
        return true;
    }
}
