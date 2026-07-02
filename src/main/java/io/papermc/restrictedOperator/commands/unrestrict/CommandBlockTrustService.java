// Stores unrestricted command text and location in block data which can be later checked to verify if a command block should run unrestricted

package io.papermc.restrictedOperator.commands.unrestrict;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class CommandBlockTrustService {
    private final NamespacedKey unrestrictedCommand;
    private final NamespacedKey unrestrictedLocation;

    public CommandBlockTrustService(JavaPlugin plugin) {
        this.unrestrictedCommand = new NamespacedKey(plugin, "unrestricted-command-hash");
        this.unrestrictedLocation = new NamespacedKey(plugin, "unrestricted-location-hash");
    }

    public void trust(Block block) {
        BlockState state = block.getState();
        CommandBlock commandBlock = (CommandBlock) state;

        PersistentDataContainer data = commandBlock.getPersistentDataContainer();
        data.set(unrestrictedCommand, PersistentDataType.STRING, CommandBlockTrustHash.hashCommand(commandBlock.getCommand()));
        data.set(unrestrictedLocation, PersistentDataType.INTEGER, CommandBlockTrustHash.hashLocation(commandBlock.getLocation()));
        commandBlock.update();
    }

    public boolean isTrusted(BlockCommandSender sender) {
        BlockState state = sender.getBlock().getState();
        if (!(state instanceof CommandBlock commandBlock)) {
            return false;
        }

        PersistentDataContainer data = commandBlock.getPersistentDataContainer();
        String storedCommandHash = data.get(unrestrictedCommand, PersistentDataType.STRING);
        Integer storedLocationHash = data.get(unrestrictedLocation, PersistentDataType.INTEGER);
        if (storedCommandHash == null || storedLocationHash == null) {
            return false;
        }

        String currentCommandHash = CommandBlockTrustHash.hashCommand(commandBlock.getCommand());
        Integer currentLocationHash = CommandBlockTrustHash.hashLocation(commandBlock.getLocation());
        if (storedCommandHash.equals(currentCommandHash) && storedLocationHash.equals(currentLocationHash)) {
            return true;
        }

        data.remove(unrestrictedCommand);
        data.remove(unrestrictedLocation);
        commandBlock.update();
        return false;
    }
}
