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

    private boolean playerCommandsEnabled;
    private Set<String> bypassUsernames;
    private String blockedPlayerCommandMessage;
    private boolean logBlockedCommands;
    private boolean notifyInstructors;
    private CommandFilter commandFilter;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        playerCommandsEnabled = config.getBoolean("filter.player-commands", true);
        bypassUsernames = normalizeList(config.getStringList("bypass-usernames"));
        blockedPlayerCommandMessage = config.getString(
                "messages.blocked-player-command",
                "That command is disabled for camp safety. Ask an instructor if you need help."
        );
        logBlockedCommands = config.getBoolean("logging.log-blocked-commands", true);
        notifyInstructors = config.getBoolean("logging.notify-instructors", true);

        boolean normalizeRootsLowercase = config.getBoolean("filter.normalize-roots-lowercase", true);
        Set<String> blockedRoots = normalizeList(config.getStringList("blocked-roots"));
        Set<String> blockedSelectors = normalizeList(config.getStringList("blocked-selectors"));
        commandFilter = new CommandFilter(blockedRoots, blockedSelectors, normalizeRootsLowercase);
    }

    public boolean isPlayerCommandsEnabled() {
        return playerCommandsEnabled;
    }

    public String getBlockedPlayerCommandMessage() {
        return blockedPlayerCommandMessage;
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
