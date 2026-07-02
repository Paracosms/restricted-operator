package io.papermc.restrictedOperator.listener;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CommandBlockAttributionService {
    private final Map<LocationKey, AttributionRecord> attributionByLocation = new HashMap<>();

    void recordPlacement(Player player, Block block) {
        record(block.getLocation(), player.getUniqueId(), System.currentTimeMillis());
    }

    void recordInteraction(Player player, Block block) {
        record(block.getLocation(), player.getUniqueId(), System.currentTimeMillis());
    }

    UUID findTrackedEditorId(Location location, long maxAgeMillis) {
        return findTrackedEditorId(LocationKey.fromLocation(location), System.currentTimeMillis(), maxAgeMillis);
    }

    void record(Location location, UUID playerId, long recordedAtMillis) {
        record(LocationKey.fromLocation(location), playerId, recordedAtMillis);
    }

    UUID findTrackedEditorId(LocationKey key, long nowMillis, long maxAgeMillis) {
        if (key == null) {
            return null;
        }

        AttributionRecord record = attributionByLocation.get(key);
        if (record == null) {
            return null;
        }

        if (maxAgeMillis > 0L && nowMillis - record.recordedAtMillis() > maxAgeMillis) {
            attributionByLocation.remove(key);
            return null;
        }

        return record.playerId();
    }

    void record(LocationKey key, UUID playerId, long recordedAtMillis) {
        if (key == null || playerId == null) {
            return;
        }

        attributionByLocation.put(key, new AttributionRecord(playerId, recordedAtMillis));
    }

    private record AttributionRecord(UUID playerId, long recordedAtMillis) {
    }

    record LocationKey(UUID worldId, int x, int y, int z) {
        static LocationKey fromLocation(Location location) {
            if (location == null || location.getWorld() == null) {
                return null;
            }

            return new LocationKey(
                    location.getWorld().getUID(),
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ()
            );
        }
    }
}
