package dev.cammiescorner.townships.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.cammiescorner.townships.component.level.ClaimsComponent;
import dev.cammiescorner.townships.component.scoreboard.TownsComponent;
import dev.cammiescorner.townships.util.Town;
import dev.cammiescorner.townships.util.TownMessages;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public class TownCommand {
	public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(Commands.literal("town").then(Commands.literal("create")
				.then(Commands.argument("name", StringArgumentType.word())
						.executes(ctx -> {
							var owner = ctx.getSource().getPlayerOrException();
							var name = StringArgumentType.getString(ctx, "name");
							return createTown(ctx, owner, name, name);
						})
						.then(Commands.argument("display_name", StringArgumentType.greedyString())
								.executes(ctx -> {
									var owner = ctx.getSource().getPlayerOrException();
									var commandName = StringArgumentType.getString(ctx, "name");
									var displayName = StringArgumentType.getString(ctx, "display_name");
									return createTown(ctx, owner, commandName, displayName);
								})
						)
				)
		).then(Commands.literal("info")
				.executes(ctx -> townInfo(ctx, ctx.getSource().getPlayerOrException()))
		).then(Commands.literal("claim")
				.executes(ctx -> claimChunk(ctx, ctx.getSource().getPlayerOrException()))
		).then(Commands.literal("unclaim")
				.executes(ctx -> unclaimChunk(ctx, ctx.getSource().getPlayerOrException()))
		));
	}

	public static int createTown(CommandContext<CommandSourceStack> context, ServerPlayer player, String name, String displayName) throws CommandSyntaxException {
		// important so it also works with /execute in <dimension>
		var level = context.getSource().getLevel();
		var pos = BlockPos.containing(context.getSource().getPosition());

		var townComponent = TownsComponent.get(level);
		var claimComponent = ClaimsComponent.get(level);

		var existingTown = player.townships$getTown().orElse(null);
		if(existingTown != null) {
			player.sendSystemMessage(TownMessages.createTown_E_inTown(existingTown));
			return 0;
		}

		var town = Town.createFor(player, level, pos, name, displayName);
		townComponent.addTown(town);
		claimComponent.addChunk(town.id(), ChunkPos.containing(player.getOnPos()));

		player.sendSystemMessage(TownMessages.createTown_success(town));

		return Command.SINGLE_SUCCESS;
	}

	public static int townInfo(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
		var level = context.getSource().getLevel();
		var claimComponent = ClaimsComponent.get(level);

		var town = player.townships$getTown().orElse(null);
		if(town != null) {
			player.sendSystemMessage(TownMessages.townInfo(town, claimComponent));

			return Command.SINGLE_SUCCESS;
		}

		return 0;
	}

	public static int claimChunk(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
		var level = context.getSource().getLevel();
		var chunkPos = ChunkPos.containing(BlockPos.containing(context.getSource().getPosition()));
		var townComponent = TownsComponent.get(level);
		var claimComponent = ClaimsComponent.get(level);

		var playerTown = player.townships$getTown().orElse(null);
		if(playerTown == null) {
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

		claimComponent.addChunk(playerTown.id(), chunkPos);

		// TODO move to messages
		context.getSource().sendSuccess(() -> Component.literal("Claimed chunk %s/%s".formatted(chunkPos.x(), chunkPos.z())), false);
		return Command.SINGLE_SUCCESS;
	}

	public static int unclaimChunk(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
		var level = context.getSource().getLevel();
		var chunkPos = ChunkPos.containing(BlockPos.containing(context.getSource().getPosition()));
		var claimComponent = ClaimsComponent.get(level);

		var member = player.townships$asTownMember().orElse(null);
		if(member == null) {
			// TODO move to messages
			context.getSource().sendFailure(Component.literal("You must first create a town!"));
			return 0;
		}

		var town = claimComponent.getTownAt(chunkPos).orElse(null);
		if(town == null) {
			// TODO move to messages
			context.getSource().sendFailure(Component.literal(String.format("Chunk %s is already unclaimed!", chunkPos)));
			return 0;
		}

		if(!town.members().containsKey(member.getPlayer().getUUID())) {
			// TODO move to messages
			context.getSource().sendFailure(Component.literal("This chunk belongs to %s, but you are not a member of that town. you cannot unclaim it!".formatted(town.displayName())));
			return 0;
		}

		if(!member.getRank().canClaim()) {
			// TODO move to messages
			context.getSource().sendFailure(Component.literal("Your current rank does not allow you to unclaim territory!"));
			return 0;
		}

		claimComponent.removeChunk(town.id(), chunkPos);

		// TODO move to messages
		context.getSource().sendSuccess(() -> Component.literal("Unclaimed chunk %s/%s".formatted(chunkPos.x(), chunkPos.z())), false);
		return Command.SINGLE_SUCCESS;
	}
}
