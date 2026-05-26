package dev.cammiescorner.townships.component.level;

import com.mojang.serialization.Codec;
import dev.cammiescorner.townships.component.scoreboard.TownsComponent;
import dev.cammiescorner.townships.init.TownshipsComponents;
import dev.cammiescorner.townships.util.Town;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.ladysnake.cca.api.v8.component.CardinalComponent;

import java.util.*;
import java.util.function.Function;

public class ClaimsComponent implements CardinalComponent {

	private static final Codec<Map<UUID, List<ChunkPos>>> CODEC = Codec.unboundedMap(
			UUIDUtil.STRING_CODEC,
			ChunkPos.CODEC.listOf().xmap(ArrayList::new, Function.identity()) // ensure mutable lists
	);

	private final Map<UUID, List<ChunkPos>> townClaims = new HashMap<>();
	private final ServerLevel level;

	public ClaimsComponent(Level level) {
		this.level = level instanceof ServerLevel serverLevel ? serverLevel : null;
	}

	@Override
	public void readData(ValueInput input) {
		townClaims.clear();
		input.read("claims", CODEC).ifPresent(townClaims::putAll);
	}

	@Override
	public void writeData(ValueOutput output) {
		output.store("claims", CODEC, townClaims);
	}

	public List<ChunkPos> getChunks(UUID uuid) {
		return Collections.unmodifiableList(townClaims.getOrDefault(uuid, List.of()));
	}

	public void addChunk(UUID uuid, ChunkPos pos) {
		townClaims.computeIfAbsent(uuid, _ -> new ArrayList<>()).add(pos);
	}

	public void removeChunk(UUID uuid, ChunkPos pos) {
		townClaims.computeIfAbsent(uuid, _ -> new ArrayList<>()).remove(pos);
	}

	public Optional<Town> getTownAt(ChunkPos pos) {
		var townData = TownsComponent.get(level);

		return townClaims.entrySet().stream().filter(e -> e.getValue().contains(pos)).findFirst()
				.map(Map.Entry::getKey).flatMap(townData::getTown);
	}

	public static ClaimsComponent get(ServerLevel level) {
		return level.getComponent(TownshipsComponents.CLAIMS_COMPONENT);
	}
}
