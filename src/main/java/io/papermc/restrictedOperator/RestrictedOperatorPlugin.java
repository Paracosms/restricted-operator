// Main entry class
package io.papermc.restrictedOperator;

import io.papermc.restrictedOperator.commands.restrictedoperator.RestrictedOperatorCommand;
import io.papermc.restrictedOperator.commands.unrestrict.UnrestrictCommand;
import io.papermc.restrictedOperator.commands.unrestrict.CommandBlockTrustService;
import io.papermc.restrictedOperator.commands.permissions.PermissionCommand;
import io.papermc.restrictedOperator.config.ConfigManager;
import io.papermc.restrictedOperator.listener.CommandBlockAttributionListener;
import io.papermc.restrictedOperator.listener.CommandBlockAttributionService;
import io.papermc.restrictedOperator.listener.CommandListener;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class RestrictedOperatorPlugin extends JavaPlugin {
    private ConfigManager configManager;
    private CommandBlockTrustService commandBlockTrustService;
    private CommandBlockAttributionService commandBlockAttributionService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        configManager.reload();
        commandBlockTrustService = new CommandBlockTrustService(this);
        commandBlockAttributionService = new CommandBlockAttributionService(configManager::getTrackingPruneIntervalDays);

        Bukkit.getPluginManager().registerEvents(
                new CommandListener(this, configManager, commandBlockTrustService, commandBlockAttributionService),
                this
        );
        Bukkit.getPluginManager().registerEvents(new CommandBlockAttributionListener(commandBlockAttributionService), this);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(
                    RestrictedOperatorCommand.create(configManager).build(),
                    "RestrictedOperator maintenance commands."
            );
            event.registrar().register(
                    UnrestrictCommand.create(commandBlockTrustService, configManager).build(),
                    "Trust the current command snapshot for a command block at the given coordinates."
            );
            event.registrar().register(
                    PermissionCommand.createBypass(configManager).build(),
                    "Add or remove the restrictedoperator.bypass-usernames permission for an online player."
            );
            event.registrar().register(
                    PermissionCommand.createNotify(configManager).build(),
                    "Add or remove the restrictedoperator.notify permission for an online player."
            );
        });
    }
}
