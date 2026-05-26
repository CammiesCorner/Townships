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
import net.minecraft.network.chat.Component;
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
				)
		).then(Commands.literal("info")
				.executes(ctx -> townInfo(ctx, ctx.getSource().getPlayerOrException()))
		).then(Commands.literal("claim")
				.executes(ctx -> claimChunk(ctx, ctx.getSource().getPlayerOrException()))
		).then(Commands.literal("unclaim")
				.executes(ctx -> unclaimChunk(ctx, ctx.getSource().getPlayerOrException()))
		);
	}

	public static int createTown(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
		var level = player.level();
		var townComponent = level.getScoreboard().getComponent(TownshipsComponents.TOWNS_COMPONENT);
		var claimComponent = level.getComponent(TownshipsComponents.CLAIMS_COMPONENT);

		if(townComponent.viewTowns().entrySet().stream().anyMatch(entry -> entry.getValue().viewMembers().containsKey(player.getUUID()))) {
			player.sendSystemMessage(Component.literal("You're already part of a town!"));
			return 0;
		}

		var town = new Town(Map.of(player.getUUID(), new Member(player, Member.Rank.MAYOR)), context.getArgument("name", String.class), 0);
		var uuid = UUID.randomUUID();

		town.setHome(level.dimension(), player.getOnPos());
		claimComponent.addChunk(uuid, ChunkPos.containing(player.getOnPos()));
		townComponent.addTown(uuid, town);

		player.sendSystemMessage(Component.literal(String.format("Created town %s", town.getDisplayName())));

		return Command.SINGLE_SUCCESS;
	}

	public static int townInfo(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
		var level = player.level();
		var townComponent = level.getScoreboard().getComponent(TownshipsComponents.TOWNS_COMPONENT);
		var claimComponent = level.getComponent(TownshipsComponents.CLAIMS_COMPONENT);

		for(Map.Entry<UUID, Town> entry : townComponent.viewTowns().entrySet()) {
			var town = entry.getValue();
			var member = town.getMember(EntityReference.of(player));

			if(member != null) {
				var text = Component.literal("Town: " + town.getDisplayName()).append("\n")
						.append("Members: " + town.viewMembers().values().stream().map(member1 -> level.getServer().getPlayerList().getPlayer(member1.getPlayer().getUUID()).getName().getString()).toList()).append("\n")
						.append("Gold: " + town.getGold()).append("\n")
						.append("Chunks Claimed: " + claimComponent.getChunks(entry.getKey()).size());
				player.sendSystemMessage(text);
			}
		}

		return Command.SINGLE_SUCCESS;
	}

	public static int claimChunk(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
		var level = player.level();
		var townComponent = level.getScoreboard().getComponent(TownshipsComponents.TOWNS_COMPONENT);
		var claimComponent = level.getComponent(TownshipsComponents.CLAIMS_COMPONENT);

		for(Map.Entry<UUID, Town> entry : townComponent.viewTowns().entrySet()) {
			var uuid = entry.getKey();
			var town = entry.getValue();
			var member = town.getMember(EntityReference.of(player));

			if(member != null && member.getRank().canClaim()) {
				var chunkPos = ChunkPos.containing(player.getOnPos());

				if(!claimComponent.getChunks(uuid).contains(chunkPos)) {
					claimComponent.addChunk(uuid, chunkPos);
					player.sendSystemMessage(Component.literal(String.format("Claimed chunk %s", chunkPos)));
					break;
				}
				else {
					player.sendSystemMessage(Component.literal(String.format("Chunk %s is already claimed!", chunkPos)));
					return 0;
				}
			}
		}

		return Command.SINGLE_SUCCESS;
	}

	public static int unclaimChunk(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
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

				if(claimComponent.getChunks(uuid).contains(chunkPos)) {
					claimComponent.removeChunk(uuid, chunkPos);
					player.sendSystemMessage(Component.literal(String.format("Unclaimed chunk %s", chunkPos)));
				}
				else {
					player.sendSystemMessage(Component.literal(String.format("Chunk %s is already unclaimed!", chunkPos)));
					return 0;
				}

				break;
			}
		}

		return Command.SINGLE_SUCCESS;
	}
}
