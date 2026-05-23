package dev.cammiescorner.townships.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.cammiescorner.townships.Townships;
import dev.cammiescorner.townships.command.TownCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class TownshipsCommands {
	public static void init(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection environment) {
		LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(Townships.MOD_ID);

		TownCommand.register(root);
	}
}
