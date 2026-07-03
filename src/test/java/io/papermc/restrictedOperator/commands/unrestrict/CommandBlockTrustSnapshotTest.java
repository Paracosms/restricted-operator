package io.papermc.restrictedOperator.commands.unrestrict;

import org.junit.jupiter.api.Test;

import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CommandBlockTrustSnapshotTest {
    @Test
    void normalizesLeadingSlashRootCaseAndWhitespace() {
        assertEquals(
                "minecraft:kill @p",
                CommandBlockTrustHash.normalize(" /Minecraft:Kill   @p ")
        );
    }

    @Test
    void preservesArgumentCaseWhileNormalizingTheRoot() {
        assertEquals(
                "say HelloThere",
                CommandBlockTrustHash.normalize("say HelloThere")
        );
    }

    @Test
    void treatsEquivalentCommandFormattingAsTheSameSnapshot() {
        assertEquals(
                CommandBlockTrustHash.hashCommand("/say   hello"),
                CommandBlockTrustHash.hashCommand("say hello")
        );
    }

    @Test
    void includesWorldUuidInLocationHash() {
        UUID worldUuid = UUID.randomUUID();
        Location location = location(worldUuid, 10, 64, 10);

        assertEquals(
                CommandBlockTrustHash.hashLocation(location),
                CommandBlockTrustHash.hashLocation(location)
        );
    }

    @Test
    void differentWorldUuidsProduceDifferentLocationHashesForSameCoordinates() {
        assertNotEquals(
                CommandBlockTrustHash.hashLocation(location(UUID.randomUUID(), 10, 64, 10)),
                CommandBlockTrustHash.hashLocation(location(UUID.randomUUID(), 10, 64, 10))
        );
    }

    private static Location location(UUID worldUuid, int x, int y, int z) {
        World world = (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[] { World.class },
                new WorldInvocationHandler(worldUuid)
        );
        return new Location(world, x, y, z);
    }

    private static final class WorldInvocationHandler implements InvocationHandler {
        private final UUID worldUuid;

        private WorldInvocationHandler(UUID worldUuid) {
            this.worldUuid = worldUuid;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if (method.getName().equals("getUID")) {
                return worldUuid;
            }
            if (method.getName().equals("toString")) {
                return "World[" + worldUuid + "]";
            }
            throw new UnsupportedOperationException(method.getName());
        }
    }
}
