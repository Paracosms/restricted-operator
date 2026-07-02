package io.papermc.restrictedOperator.commands.unrestrict;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
