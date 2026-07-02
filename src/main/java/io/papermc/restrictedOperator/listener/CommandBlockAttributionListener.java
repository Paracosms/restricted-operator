package io.papermc.restrictedOperator.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;

public final class CommandBlockAttributionListener implements Listener {
    private final CommandBlockAttributionService attributionService;

    public CommandBlockAttributionListener(CommandBlockAttributionService attributionService) {
        this.attributionService = attributionService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onBlockPlace(BlockPlaceEvent event) {
        if (!isCommandBlock(event.getBlockPlaced().getType())) {
            return;
        }

        attributionService.recordPlacement(event.getPlayer(), event.getBlockPlaced());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !(clickedBlock.getState() instanceof CommandBlock)) {
            return;
        }

        attributionService.recordInteraction(event.getPlayer(), clickedBlock);
    }

    private boolean isCommandBlock(Material material) {
        return material == Material.COMMAND_BLOCK
                || material == Material.CHAIN_COMMAND_BLOCK
                || material == Material.REPEATING_COMMAND_BLOCK;
    }
}
