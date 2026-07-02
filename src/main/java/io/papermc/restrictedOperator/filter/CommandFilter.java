// Determines if commands should be restricted according to config.yml

package io.papermc.restrictedOperator.filter;

import io.papermc.restrictedOperator.CommandCheckResult;
import io.papermc.restrictedOperator.CommandSourceType;

import java.util.Locale;
import java.util.Set;

public final class CommandFilter {
    private final Set<String> blockedRoots;
    private final Set<String> blockedSelectors;
    private final boolean normalizeRootsLowercase;

    public CommandFilter(Set<String> blockedRoots, Set<String> blockedSelectors, boolean normalizeRootsLowercase) {
        this.blockedRoots = blockedRoots;
        this.blockedSelectors = blockedSelectors;
        this.normalizeRootsLowercase = normalizeRootsLowercase;
    }

    // Parses commands
    public CommandCheckResult check(String rawCommand, CommandSourceType sourceType) {
        if (rawCommand == null) {
            return CommandCheckResult.emptyCommand();
        }

        String trimmed = rawCommand.trim();
        if (trimmed.isEmpty()) {
            return CommandCheckResult.emptyCommand();
        }

        String withoutSlash = trimmed.startsWith("/") ? trimmed.substring(1) : trimmed;
        if (withoutSlash.isBlank()) {
            return CommandCheckResult.emptyCommand();
        }

        String[] tokens = withoutSlash.split("\\s+");
        String rawRoot = tokens[0];
        String normalizedRoot = normalizeRootsLowercase ? rawRoot.toLowerCase(Locale.ROOT) : rawRoot;

        // Block all commands starting with blocked-roots in config.yml
        if (blockedRoots.contains(normalizedRoot)) {
            return CommandCheckResult.blockedRoot(normalizedRoot, normalizedRoot);
        }

        // Tokenize to block selectors like @e
        for (int i = 1; i < tokens.length; i++) {
            String normalizedToken = tokens[i].toLowerCase(Locale.ROOT);
            for (String blockedSelector : blockedSelectors) {
                if (normalizedToken.equals(blockedSelector) || normalizedToken.startsWith(blockedSelector + "[")) {
                    return CommandCheckResult.blockedSelector(normalizedRoot, blockedSelector);
                }
            }
        }

        return CommandCheckResult.allowed(normalizedRoot);
    }
}
