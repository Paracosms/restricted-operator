package io.papermc.restrictedOperator;

public record CommandCheckResult(
        boolean allowed,
        Reason reason,
        String root,
        String matchedPattern
) {
    public static CommandCheckResult allowed(String root) {
        return new CommandCheckResult(true, Reason.ALLOWED, root, "");
    }

    public static CommandCheckResult emptyCommand() {
        return new CommandCheckResult(true, Reason.EMPTY_COMMAND, "", "");
    }

    public static CommandCheckResult blockedRoot(String root, String matchedPattern) {
        return new CommandCheckResult(false, Reason.BLOCKED_ROOT, root, matchedPattern);
    }

    public static CommandCheckResult blockedSelector(String root, String matchedPattern) {
        return new CommandCheckResult(false, Reason.BLOCKED_SELECTOR, root, matchedPattern);
    }

    public enum Reason {
        ALLOWED,
        EMPTY_COMMAND,
        BLOCKED_ROOT,
        BLOCKED_SELECTOR
    }
}
