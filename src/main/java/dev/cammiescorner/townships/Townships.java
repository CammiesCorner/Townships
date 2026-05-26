package dev.cammiescorner.townships;

import dev.cammiescorner.townships.api.event.ServerPlayerChunkEvents;
import dev.cammiescorner.townships.component.level.ClaimsComponent;
import dev.cammiescorner.townships.init.TownshipsCommands;
import dev.cammiescorner.townships.util.TownMessages;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Townships implements ModInitializer {
	public static final String MOD_ID = "townships";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register(TownshipsCommands::init);
		ServerPlayerChunkEvents.ON_CHUNKPOS_CHANGE.register((player, oldChunk, newChunk) -> {
			var claimData = ClaimsComponent.get(player.level());
			var oldTown = claimData.getTownAt(oldChunk).orElse(null);
			var currentTown = claimData.getTownAt(newChunk).orElse(null);

			if(oldTown != currentTown) {
				if(currentTown == null)
					player.sendOverlayMessage(TownMessages.ENTER_AREA_WILDERNESS);
				else
					player.sendOverlayMessage(TownMessages.enterArea_town(currentTown));
			}
			else if(newChunk.contains(currentTown.homePos().pos()))
				player.sendOverlayMessage(TownMessages.enterArea_townHome(currentTown));
		});
	}

	public static Identifier id(String name) {
		return Identifier.fromNamespaceAndPath(MOD_ID, name);
	}
}
