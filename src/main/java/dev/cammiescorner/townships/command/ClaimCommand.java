package dev.cammiescorner.townships.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.cammiescorner.townships.api.event.ClaimEvents;
import dev.cammiescorner.townships.component.level.ClaimsComponent;
import dev.cammiescorner.townships.util.Member;
import dev.cammiescorner.townships.util.Town;
import dev.cammiescorner.townships.util.TownMessages;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
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
            context.getSource().sendFailure(TownMessages.Towns.Error.NOT_IN_TOWN);
            return 0;
        }

        var existingClaim = claimComponent.getTownAt(chunkPos).orElse(null);
        if(existingClaim != null) {
            context.getSource().sendFailure(TownMessages.Towns.Claim.Error.ownedByOtherTown(chunkPos, existingClaim));
            return 0;
        }

        if(!member.getRank().canClaim()) {
            context.getSource().sendFailure(TownMessages.Towns.Claim.Error.missingPermission(member.getRank()));
        }

        var errorMsg = ClaimEvents.TRY_CLAIM_CHUNK.invoker().allowAction(town, member, level, chunkPos);
        if(errorMsg != null) {
            context.getSource().sendFailure(errorMsg);
            return 0;
        }

        claimComponent.addChunk(town.id(), chunkPos);

        context.getSource().sendSuccess(() -> TownMessages.Towns.Claim.success(chunkPos), false);
        ClaimEvents.CLAIMED_CHUNK.invoker().onClaimAction(town, member, level, chunkPos);
        return Command.SINGLE_SUCCESS;
    }

    private static int unclaimChunk(CommandContext<CommandSourceStack> context, @Nullable Town town, @Nullable Member member) throws CommandSyntaxException {
        var level = context.getSource().getLevel();
        var chunkPos = ChunkPos.containing(BlockPos.containing(context.getSource().getPosition()));
        var claimComponent = ClaimsComponent.get(level);

        if(member == null || town == null) {
            context.getSource().sendFailure(TownMessages.Towns.Error.NOT_IN_TOWN);
            return 0;
        }

        var townAtLocation = claimComponent.getTownAt(chunkPos).orElse(null);
        if(townAtLocation == null) {
            context.getSource().sendFailure(TownMessages.Towns.Unclaim.Error.notClaimed(chunkPos));
            return 0;
        }

        if(!townAtLocation.members().containsKey(member.id())) {
            context.getSource().sendFailure(TownMessages.Towns.Unclaim.Error.claimedByOtherTown(chunkPos, townAtLocation));
            return 0;
        }

        if(!member.getRank().canClaim()) {
            context.getSource().sendFailure(TownMessages.Towns.Unclaim.Error.missingPermission(member.getRank()));
            return 0;
        }

        var errorMsg = ClaimEvents.TRY_UNCLAIM_CHUNK.invoker().allowAction(town, member, level, chunkPos);
        if (errorMsg != null) {
            context.getSource().sendFailure(errorMsg);
            return 0;
        }

        claimComponent.removeChunk(town.id(), chunkPos);

        context.getSource().sendSuccess(() -> TownMessages.Towns.Unclaim.success(chunkPos), false);
        ClaimEvents.UNCLAIMED_CHUNK.invoker().onClaimAction(town, member, level, chunkPos);
        return Command.SINGLE_SUCCESS;
    }
}
