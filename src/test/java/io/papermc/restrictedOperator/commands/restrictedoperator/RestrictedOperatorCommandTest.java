package io.papermc.restrictedOperator.commands.restrictedoperator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestrictedOperatorCommandTest {
    @Test
    void allowsConfiguredBypassPlayerWithoutPermission() {
        boolean allowed = RestrictedOperatorCommand.canUse(false, false, false, true);

        assertTrue(allowed);
    }

    @Test
    void allowsPlayerWithPermission() {
        boolean allowed = RestrictedOperatorCommand.canUse(false, false, true, false);

        assertTrue(allowed);
    }

    @Test
    void blocksPlayerWithoutPermissionOrBypassEntry() {
        boolean allowed = RestrictedOperatorCommand.canUse(false, false, false, false);

        assertFalse(allowed);
    }

    @Test
    void configAllowsConfiguredBypassPlayerWithoutPermission() {
        boolean allowed = RestrictedOperatorCommand.canUseConfig(false, false, true);

        assertTrue(allowed);
    }

    @Test
    void configBlocksPermissionOnlyPlayer() {
        boolean allowed = RestrictedOperatorCommand.canUseConfig(false, false, false);

        assertFalse(allowed);
    }

    @Test
    void normalizesInputBeforeStorage() {
        assertEquals("minecraft", RestrictedOperatorCommand.normalizeValue("MineCraft"));
    }

    @Test
    void formatsListEntriesByCategory() {
        assertEquals("/minecraft:", RestrictedOperatorCommand.formatListedValue("minecraft", true));
        assertEquals("/paper:", RestrictedOperatorCommand.formatListedValue("paper", true));
        assertEquals("/kill", RestrictedOperatorCommand.formatListedValue("kill", false));
        assertEquals("@e", RestrictedOperatorCommand.formatListedValue("@e", false));
    }
}
