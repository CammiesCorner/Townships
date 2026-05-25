package dev.cammiescorner.townships.init;

import com.mojang.brigadier.CommandDispatcher;
import dev.cammiescorner.townships.Townships;
import dev.cammiescorner.townships.command.TownCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.util.Util;

public class TownshipsCommands {
	public static void init(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection environment) {
		dispatcher.register(Util.make(Commands.literal(Townships.MOD_ID), root -> {
			TownCommand.register(root);
		}));
		dispatcher.register(Util.make(Commands.literal("t"), root -> {
			TownCommand.register(root);
		}));
	}
}
