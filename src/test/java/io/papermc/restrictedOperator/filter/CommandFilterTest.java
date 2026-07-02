package io.papermc.restrictedOperator.filter;

import io.papermc.restrictedOperator.CommandCheckResult;
import io.papermc.restrictedOperator.CommandSourceType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandFilterTest {
    private final CommandFilter filter = new CommandFilter(
            Set.of("kill", "minecraft:kill", "paper", "bukkit:reload"),
            Set.of("@e", "@a", "@r"),
            true
    );

    @Test
    void blocksExactRoot() {
        CommandCheckResult result = filter.check("/kill @s", CommandSourceType.PLAYER);

        assertFalse(result.allowed());
        assertEquals(CommandCheckResult.Reason.BLOCKED_ROOT, result.reason());
        assertEquals("kill", result.root());
    }

    @Test
    void blocksNamespacedRoot() {
        CommandCheckResult result = filter.check("/Minecraft:Kill @s", CommandSourceType.PLAYER);

        assertFalse(result.allowed());
        assertEquals(CommandCheckResult.Reason.BLOCKED_ROOT, result.reason());
        assertEquals("minecraft:kill", result.root());
    }

    @Test
    void blocksSelectorToken() {
        CommandCheckResult result = filter.check("/tp @e[type=pig] ~ ~ ~", CommandSourceType.PLAYER);

        assertFalse(result.allowed());
        assertEquals(CommandCheckResult.Reason.BLOCKED_SELECTOR, result.reason());
        assertEquals("@e", result.matchedPattern());
    }

    @Test
    void allowsSafeSelector() {
        CommandCheckResult result = filter.check("/tp @p ~ ~1 ~", CommandSourceType.PLAYER);

        assertTrue(result.allowed());
        assertEquals(CommandCheckResult.Reason.ALLOWED, result.reason());
    }

    @Test
    void avoidsSubstringFalsePositives() {
        assertTrue(filter.check("/killjoy", CommandSourceType.PLAYER).allowed());
        assertTrue(filter.check("/say hello@e", CommandSourceType.PLAYER).allowed());
        assertTrue(filter.check("/say @everyone", CommandSourceType.PLAYER).allowed());
    }

    @Test
    void allowsSafeCommandBlockCommand() {
        CommandCheckResult result = filter.check("say hello", CommandSourceType.COMMAND_BLOCK);

        assertTrue(result.allowed());
        assertEquals(CommandCheckResult.Reason.ALLOWED, result.reason());
        assertEquals("say", result.root());
    }

    @Test
    void blocksCommandBlockKillCommand() {
        CommandCheckResult result = filter.check("kill @p", CommandSourceType.COMMAND_BLOCK);

        assertFalse(result.allowed());
        assertEquals(CommandCheckResult.Reason.BLOCKED_ROOT, result.reason());
        assertEquals("kill", result.root());
    }

    @Test
    void blocksNamespacedCommandBlockKillCommand() {
        CommandCheckResult result = filter.check("minecraft:kill @p", CommandSourceType.COMMAND_BLOCK);

        assertFalse(result.allowed());
        assertEquals(CommandCheckResult.Reason.BLOCKED_ROOT, result.reason());
        assertEquals("minecraft:kill", result.root());
    }

    @Test
    void blocksRepeatingCommandBlockSelectorCommand() {
        CommandCheckResult result = filter.check("tp @e ~ ~1 ~", CommandSourceType.COMMAND_BLOCK);

        assertFalse(result.allowed());
        assertEquals(CommandCheckResult.Reason.BLOCKED_SELECTOR, result.reason());
        assertEquals("@e", result.matchedPattern());
    }
}
