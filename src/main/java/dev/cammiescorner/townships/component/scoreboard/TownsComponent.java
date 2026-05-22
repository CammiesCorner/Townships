package dev.cammiescorner.townships.component.scoreboard;

import dev.cammiescorner.townships.Townships;
import dev.cammiescorner.townships.util.Town;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.scores.Scoreboard;
import org.ladysnake.cca.api.v8.component.CardinalComponent;

import java.util.HashMap;
import java.util.Map;

public class TownsComponent implements CardinalComponent {
	private final Map<String, Town> towns = new HashMap<>();
	private final MinecraftServer server;

	public TownsComponent(Scoreboard scoreboard, MinecraftServer server) {
		this.server = server;
	}

	@Override
	public void readData(ValueInput readView) {
		towns.clear();

		var townsList = readView.childrenListOrEmpty("Towns");

		for(ValueInput input : townsList) {
			var townName = input.getString("TownName");

			townName.ifPresent(name -> towns.put(name, Town.fromData(input.childOrEmpty("TownData"))));
		}
	}

	@Override
	public void writeData(ValueOutput writeView) {
		var townsList = writeView.childrenList("Towns");

		towns.forEach((name, town) -> {
			var output = townsList.addChild();

			output.putString("TownName", name);
			town.toData(output.child("TownData"));
		});
	}

	public Map<String, Town> getTowns() {
		return Map.copyOf(towns);
	}

	public Town getTown(String name) {
		if(!towns.containsKey(name)) {
			Townships.LOGGER.error("Town [{}] doesn't exist", name);

			return null;
		}

		return towns.get(name);
	}

	public void addTown(String name, Town town) {
		if(!towns.containsKey(name))
			towns.put(name, town);
	}

	public void removeTown(String name) {
		towns.remove(name);
	}
}
