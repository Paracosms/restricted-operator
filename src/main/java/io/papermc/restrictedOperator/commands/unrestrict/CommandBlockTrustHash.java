// Hashing helper function for the CommandBlockTrustService

package io.papermc.restrictedOperator.commands.unrestrict;

import org.bukkit.Location;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

final class CommandBlockTrustHash {
    private CommandBlockTrustHash() {
    }

    static String normalize(String rawCommand) {
        if (rawCommand == null) {
            return "";
        }

        String trimmed = rawCommand.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        String withoutSlash = trimmed.startsWith("/") ? trimmed.substring(1) : trimmed;
        String[] tokens = withoutSlash.split("\\s+");
        if (tokens.length == 0) {
            return "";
        }

        tokens[0] = tokens[0].toLowerCase(Locale.ROOT);
        return String.join(" ", tokens);
    }

    static String hashCommand(String rawCommand) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalize(rawCommand).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    // random bs equation go !
    static Integer hashLocation(Location location) {
        int hash = location.getWorld().getUID().hashCode() ^ location.getBlockX() * 73428767 ^ location.getBlockY() * 91227153 ^ location.getBlockZ() * 43828933;
        hash ^= hash >>> 16;
        return hash;
    }

}
