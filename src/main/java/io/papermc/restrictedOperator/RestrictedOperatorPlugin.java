// Main entry class
package io.papermc.restrictedOperator;

import io.papermc.restrictedOperator.config.ConfigManager;
import io.papermc.restrictedOperator.listener.CommandListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class RestrictedOperatorPlugin extends JavaPlugin {
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        configManager.reload();

        Bukkit.getPluginManager().registerEvents(new CommandListener(this, configManager), this);
    }
}
