package dev.cammiescorner.townships.component.scoreboard;

import dev.cammiescorner.townships.Townships;
import dev.cammiescorner.townships.util.Town;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.scores.Scoreboard;
import org.ladysnake.cca.api.v8.component.CardinalComponent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TownsComponent implements CardinalComponent {
	private final Map<UUID, Town> towns = new HashMap<>();
	private final MinecraftServer server;

	public TownsComponent(Scoreboard scoreboard, MinecraftServer server) {
		this.server = server;
	}

	@Override
	public void readData(ValueInput readView) {
		towns.clear();

		var townsList = readView.childrenListOrEmpty("Towns");

		for(ValueInput input : townsList) {
			var townId = input.read("TownId", UUIDUtil.CODEC);

			townId.ifPresent(uuid -> input.read("TownData", Town.CODEC).ifPresent(town -> towns.put(uuid, town)));
		}
	}

	@Override
	public void writeData(ValueOutput writeView) {
		var townsList = writeView.childrenList("Towns");

		towns.forEach((uuid, town) -> {
			var output = townsList.addChild();

			output.store("TownId", UUIDUtil.CODEC, uuid);
			output.store("TownData", Town.CODEC, town);
		});
	}

	public Map<UUID, Town> viewTowns() {
		return Collections.unmodifiableMap(towns);
	}

	public Town getTown(UUID uuid) {
		if(!towns.containsKey(uuid)) {
			Townships.LOGGER.error("Town ID [{}] doesn't exist", uuid);

			return null;
		}

		return towns.get(uuid);
	}

	public void addTown(UUID uuid, Town town) {
		towns.put(uuid, town);
	}

	public void removeTown(UUID uuid) {
		towns.remove(uuid);
	}
}
