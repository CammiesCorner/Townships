package dev.cammiescorner.townships.component.scoreboard;

import dev.cammiescorner.townships.init.TownshipsComponents;
import dev.cammiescorner.townships.util.Town;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.scores.Scoreboard;
import org.ladysnake.cca.api.v8.component.CardinalComponent;

import java.util.*;

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

	public Map<UUID, Town> towns() {
		return Collections.unmodifiableMap(towns);
	}

	public Optional<Town> getTown(UUID uuid) {
		return Optional.ofNullable(towns.get(uuid));
	}

	public Optional<Town> townFor(EntityReference<Player> reference) {
		return towns.values().stream().filter(it -> it.members().containsKey(reference.getUUID())).findFirst();
	}

	public void addTown(Town town) {
		towns.put(town.id(), town);
	}

	public void removeTown(UUID uuid) {
		towns.remove(uuid);
	}

	public static TownsComponent get(ServerLevel level) {
		return level.getScoreboard().getComponent(TownshipsComponents.TOWNS_COMPONENT);
	}

	public static TownsComponent get(MinecraftServer server) {
		return server.getScoreboard().getComponent(TownshipsComponents.TOWNS_COMPONENT);
	}
}
