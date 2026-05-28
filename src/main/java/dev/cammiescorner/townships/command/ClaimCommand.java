package dev.cammiescorner.townships.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.cammiescorner.townships.api.event.ClaimEvents;
import dev.cammiescorner.townships.component.level.ClaimsComponent;
import dev.cammiescorner.townships.util.Member;
import dev.cammiescorner.townships.util.Town;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

public class ClaimCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("claim")
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayerOrException();
                    var town = player.townships$getTown().orElse(null);
                    var playerAsMember = player.townships$asTownMember().orElse(null);
                    return claimChunk(ctx, town, playerAsMember);
                })
        ).then(Commands.literal("unclaim")
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayerOrException();
                    var town = player.townships$getTown().orElse(null);
                    var playerAsMember = player.townships$asTownMember().orElse(null);
                    return unclaimChunk(ctx, town, playerAsMember);
                })
        );
    }

    private static int claimChunk(CommandContext<CommandSourceStack> context, @Nullable Town town, @Nullable Member member) throws CommandSyntaxException {
        var level = context.getSource().getLevel();
        var chunkPos = ChunkPos.containing(BlockPos.containing(context.getSource().getPosition()));
        var claimComponent = ClaimsComponent.get(level);

        if(town == null || member == null) {
            // TODO move to messages
            context.getSource().sendFailure(Component.literal("You must first create a town!"));
            return 0;
        }

        var existingClaim = claimComponent.getTownAt(chunkPos).orElse(null);
        if(existingClaim != null) {
            // TODO move to messages
            context.getSource().sendFailure(Component.literal("Chunk %s/%s is already claimed by %s!".formatted(chunkPos.x(), chunkPos.z(), existingClaim.displayName())));
            return 0;
        }

        var errorMsg = ClaimEvents.TRY_CLAIM_CHUNK.invoker().allowAction(town, member, level, chunkPos);
        if(errorMsg != null) {
            context.getSource().sendFailure(errorMsg);
            return 0;
        }

        claimComponent.addChunk(town.id(), chunkPos);

        // TODO move to messages
        context.getSource().sendSuccess(() -> Component.literal("Claimed chunk %s/%s".formatted(chunkPos.x(), chunkPos.z())), false);
        ClaimEvents.CLAIMED_CHUNK.invoker().onClaimAction(town, member, level, chunkPos);
        return Command.SINGLE_SUCCESS;
    }

    private static int unclaimChunk(CommandContext<CommandSourceStack> context, @Nullable Town town, @Nullable Member member) throws CommandSyntaxException {
        var level = context.getSource().getLevel();
        var chunkPos = ChunkPos.containing(BlockPos.containing(context.getSource().getPosition()));
        var claimComponent = ClaimsComponent.get(level);

        if(member == null || town == null) {
            // TODO move to messages
            context.getSource().sendFailure(Component.literal("You must first create a town!"));
            return 0;
        }

        var townAtLocation = claimComponent.getTownAt(chunkPos).orElse(null);
        if(townAtLocation == null) {
            // TODO move to messages
            context.getSource().sendFailure(Component.literal(String.format("Chunk %s is not claimed by anybody!", chunkPos)));
            return 0;
        }

        if(!townAtLocation.members().containsKey(member.id())) {
            // TODO move to messages
            context.getSource().sendFailure(Component.literal("This chunk belongs to %s, but you are not a member of that town. you cannot unclaim it!".formatted(townAtLocation.displayName())));
            return 0;
        }

        if(!member.getRank().canClaim()) {
            // TODO move to messages
            context.getSource().sendFailure(Component.literal("Your current rank does not allow you to unclaim territory!"));
            return 0;
        }

        var errorMsg = ClaimEvents.TRY_UNCLAIM_CHUNK.invoker().allowAction(town, member, level, chunkPos);
        if (errorMsg != null) {
            context.getSource().sendFailure(errorMsg);
            return 0;
        }

        claimComponent.removeChunk(town.id(), chunkPos);

        // TODO move to messages
        context.getSource().sendSuccess(() -> Component.literal("Unclaimed chunk %s/%s".formatted(chunkPos.x(), chunkPos.z())), false);
        ClaimEvents.UNCLAIMED_CHUNK.invoker().onClaimAction(town, member, level, chunkPos);
        return Command.SINGLE_SUCCESS;
    }
}
