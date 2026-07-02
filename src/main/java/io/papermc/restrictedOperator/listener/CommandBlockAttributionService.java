package io.papermc.restrictedOperator.listener;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;

public final class CommandBlockAttributionService {
    private static final int PRUNE_RECORD_INTERVAL = 128;
    private static final long MILLIS_PER_DAY = 24L * 60L * 60L * 1000L;

    private final Map<LocationKey, AttributionRecord> attributionByLocation = new HashMap<>();
    private final LongSupplier trackingPruneIntervalDaysSupplier;
    private int recordsSincePrune;

    public CommandBlockAttributionService(LongSupplier trackingPruneIntervalDaysSupplier) {
        this.trackingPruneIntervalDaysSupplier = trackingPruneIntervalDaysSupplier;
    }

    void recordPlacement(Player player, Block block) {
        record(block.getLocation(), player.getUniqueId(), System.currentTimeMillis());
    }

    void recordInteraction(Player player, Block block) {
        record(block.getLocation(), player.getUniqueId(), System.currentTimeMillis());
    }

    UUID findTrackedEditorId(Location location) {
        return findTrackedEditorId(LocationKey.fromLocation(location));
    }

    void record(Location location, UUID playerId, long recordedAtMillis) {
        record(LocationKey.fromLocation(location), playerId, recordedAtMillis);
    }

    UUID findTrackedEditorId(LocationKey key) {
        if (key == null) {
            return null;
        }

        AttributionRecord record = attributionByLocation.get(key);
        if (record == null) {
            return null;
        }

        return record.playerId();
    }

    void record(LocationKey key, UUID playerId, long recordedAtMillis) {
        if (key == null || playerId == null) {
            return;
        }

        attributionByLocation.put(key, new AttributionRecord(playerId, recordedAtMillis));
        recordsSincePrune++;
        if (recordsSincePrune >= PRUNE_RECORD_INTERVAL) {
            pruneExpired(recordedAtMillis);
            recordsSincePrune = 0;
        }
    }

    private void pruneExpired(long nowMillis) {
        long trackingPruneIntervalDays = trackingPruneIntervalDaysSupplier.getAsLong();
        long maxAgeMillis = trackingPruneIntervalDays * MILLIS_PER_DAY;
        Iterator<Map.Entry<LocationKey, AttributionRecord>> iterator = attributionByLocation.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<LocationKey, AttributionRecord> entry = iterator.next();
            if (nowMillis - entry.getValue().recordedAtMillis() > maxAgeMillis) {
                iterator.remove();
            }
        }
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
