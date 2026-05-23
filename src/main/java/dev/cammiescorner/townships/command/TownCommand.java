package dev.cammiescorner.townships.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.cammiescorner.townships.init.TownshipsComponents;
import dev.cammiescorner.townships.util.Member;
import dev.cammiescorner.townships.util.Town;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.level.ChunkPos;

import java.util.Map;
import java.util.UUID;

public class TownCommand {
	public static void register(LiteralArgumentBuilder<CommandSourceStack> builder) {
		builder.then(Commands.literal("create")
				.then(Commands.argument("name", StringArgumentType.word())
						.executes(ctx -> createTown(ctx, ctx.getSource().getPlayerOrException()))
				))
		.then(Commands.literal("claim")
				.executes(ctx -> claimChunk(ctx, ctx.getSource().getPlayerOrException()))
		);
	}

	public static int createTown(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
		var town = new Town(Map.of(player.getUUID(), new Member(player, Member.Rank.MAYOR)), context.getInput(), 0);
		var uuid = UUID.randomUUID();
		var level = player.level();

		town.setHome(level.dimension(), player.getOnPos());
		level.getComponent(TownshipsComponents.CLAIMS_COMPONENT).addChunk(uuid, ChunkPos.containing(player.getOnPos()));
		level.getScoreboard().getComponent(TownshipsComponents.TOWNS_COMPONENT).addTown(uuid, town);

		return Command.SINGLE_SUCCESS;
	}

	public static int claimChunk(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
		var level = player.level();
		var townComponent = level.getScoreboard().getComponent(TownshipsComponents.TOWNS_COMPONENT);
		var claimComponent = level.getComponent(TownshipsComponents.CLAIMS_COMPONENT);

		for(Map.Entry<UUID, Town> entry : townComponent.viewTowns().entrySet()) {
			var uuid = entry.getKey();
			var town = entry.getValue();

			if(town.getMember(EntityReference.of(player)).getRank().canClaim()) {
				var chunkPos = ChunkPos.containing(player.getOnPos());

				if(chunkPos.contains(town.getHomePos()))
					return 0;

				if(!claimComponent.getChunks(uuid).contains(chunkPos))
					claimComponent.addChunk(uuid, chunkPos);
				else
					claimComponent.removeChunk(uuid, chunkPos);

				break;
			}
		}

		return Command.SINGLE_SUCCESS;
	}
}
