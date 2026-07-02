// Adds the following bypass-users-only command:
// /unrestrict [x] [y] [z]
// Unrestricts a command block at a given location for the current command
// If the command changes, the command block becomes monitored again

package io.papermc.restrictedOperator.commands.unrestrict;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.math.BlockPosition;
import io.papermc.restrictedOperator.PermissionNodes;
import io.papermc.restrictedOperator.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class UnrestrictCommand {
    private UnrestrictCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> create(CommandBlockTrustService trustService, ConfigManager configManager) {
        return Commands.literal("unrestrict")
                .requires(source -> source.getSender().hasPermission(PermissionNodes.BYPASS_USERS)
                        || source.getSender() instanceof Player player && configManager.isBypassUsername(player.getName()))
                .then(Commands.argument("position", ArgumentTypes.blockPosition())
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            if (!(sender instanceof Player player)) {
                                sender.sendMessage("Only players can use this command.");
                                return Command.SINGLE_SUCCESS;
                            }

                            BlockPositionResolver resolver = ctx.getArgument("position", BlockPositionResolver.class);
                            BlockPosition position = resolver.resolve(ctx.getSource());
                            Block block = player.getWorld().getBlockAt(position.blockX(), position.blockY(), position.blockZ());
                            BlockState state = block.getState();
                            if (!(state instanceof CommandBlock commandBlock)) {
                                sender.sendMessage(Component.text("That block is not a command block!", NamedTextColor.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            trustService.trust(block);

                            sender.sendMessage(Component.text()
                                    .append(Component.text("Unrestricted the command block at "))
                                    .append(Component.text(position.blockX() + " " + position.blockY() + " " + position.blockZ()))
                                    .append(Component.text(" with the command="))
                                    .append(Component.text(commandBlock.getCommand(), NamedTextColor.GRAY))
                                    .build());
                            return Command.SINGLE_SUCCESS;
                        }));
    }
}
