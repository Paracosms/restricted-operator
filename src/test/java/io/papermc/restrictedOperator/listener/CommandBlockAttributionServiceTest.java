package io.papermc.restrictedOperator.listener;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CommandBlockAttributionServiceTest {
    @Test
    void returnsTrackedEditorWithoutReadTimeExpiry() {
        CommandBlockAttributionService service = new CommandBlockAttributionService(() -> 7L);
        CommandBlockAttributionService.LocationKey key = new CommandBlockAttributionService.LocationKey(UUID.randomUUID(), 10, 64, 10);
        UUID playerId = UUID.randomUUID();

        service.record(key, playerId, 1_000L);

        assertEquals(playerId, service.findTrackedEditorId(key));
    }

    @Test
    void latestInteractionWinsForSameCommandBlock() {
        CommandBlockAttributionService service = new CommandBlockAttributionService(() -> 7L);
        CommandBlockAttributionService.LocationKey key = new CommandBlockAttributionService.LocationKey(UUID.randomUUID(), 10, 64, 10);
        UUID firstPlayerId = UUID.randomUUID();
        UUID secondPlayerId = UUID.randomUUID();

        service.record(key, firstPlayerId, 1_000L);
        service.record(key, secondPlayerId, 2_000L);

        assertEquals(secondPlayerId, service.findTrackedEditorId(key));
    }

    @Test
    void prunesEntriesOlderThanConfiguredIntervalEvery128Records() {
        long pruneIntervalDays = 7L;
        long pruneIntervalMillis = pruneIntervalDays * 24L * 60L * 60L * 1000L;
        CommandBlockAttributionService service = new CommandBlockAttributionService(() -> pruneIntervalDays);
        CommandBlockAttributionService.LocationKey staleKey = new CommandBlockAttributionService.LocationKey(UUID.randomUUID(), 10, 64, 10);
        UUID stalePlayerId = UUID.randomUUID();

        service.record(staleKey, stalePlayerId, 1_000L);

        for (int i = 1; i < 128; i++) {
            service.record(
                    new CommandBlockAttributionService.LocationKey(UUID.randomUUID(), i, 64, i),
                    UUID.randomUUID(),
                    1_000L + pruneIntervalMillis + i
            );
        }

        assertNull(service.findTrackedEditorId(staleKey));
    }
}
