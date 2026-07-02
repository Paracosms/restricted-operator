package io.papermc.restrictedOperator.listener;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CommandBlockAttributionServiceTest {
    @Test
    void returnsTrackedEditorBeforeExpiry() {
        CommandBlockAttributionService service = new CommandBlockAttributionService();
        CommandBlockAttributionService.LocationKey key = new CommandBlockAttributionService.LocationKey(UUID.randomUUID(), 10, 64, 10);
        UUID playerId = UUID.randomUUID();

        service.record(key, playerId, 1_000L);

        assertEquals(playerId, service.findTrackedEditorId(key, 61_000L, 60_001L));
    }

    @Test
    void expiresTrackedEditorAfterMaxAge() {
        CommandBlockAttributionService service = new CommandBlockAttributionService();
        CommandBlockAttributionService.LocationKey key = new CommandBlockAttributionService.LocationKey(UUID.randomUUID(), 10, 64, 10);
        UUID playerId = UUID.randomUUID();

        service.record(key, playerId, 1_000L);

        assertNull(service.findTrackedEditorId(key, 61_001L, 60_000L));
        assertNull(service.findTrackedEditorId(key, 61_001L, 60_000L));
    }

    @Test
    void latestInteractionWinsForSameCommandBlock() {
        CommandBlockAttributionService service = new CommandBlockAttributionService();
        CommandBlockAttributionService.LocationKey key = new CommandBlockAttributionService.LocationKey(UUID.randomUUID(), 10, 64, 10);
        UUID firstPlayerId = UUID.randomUUID();
        UUID secondPlayerId = UUID.randomUUID();

        service.record(key, firstPlayerId, 1_000L);
        service.record(key, secondPlayerId, 2_000L);

        assertEquals(secondPlayerId, service.findTrackedEditorId(key, 2_500L, 60_000L));
    }
}
