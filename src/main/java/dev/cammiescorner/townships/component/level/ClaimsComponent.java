package dev.cammiescorner.townships.component.level;

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

public class ClaimsComponent implements CardinalComponent {
	private final Map<UUID, List<ChunkPos>> townClaims = new HashMap<>();
	private final Level level;

	public ClaimsComponent(Level level) {
		this.level = level;
	}

	@Override
	public void readData(ValueInput readView) {
		townClaims.clear();

		var claimsList = readView.childrenListOrEmpty("TownClaims");

		for(ValueInput input : claimsList) {
			List<ChunkPos> list = new ArrayList<>();
			var posList = input.childrenListOrEmpty("ChunkPoses");
			var townId = input.read("TownId", UUIDUtil.CODEC);

			for(ValueInput i : posList) {
				list.add(new ChunkPos(i.getIntOr("X", 0), i.getIntOr("Z", 0)));
			}

			townId.ifPresent(uuid -> townClaims.put(uuid, list));
		}
	}

	@Override
	public void writeData(ValueOutput writeView) {
		var claimsList = writeView.childrenList("TownClaims");

		townClaims.forEach((townId, chunkPos) -> {
			var output = claimsList.addChild();
			var posList = output.childrenList("ChunkPoses");

			output.store("TownId", UUIDUtil.CODEC, townId);

			for(ChunkPos pos : chunkPos) {
				var o = posList.addChild();

				o.putInt("X", pos.x());
				o.putInt("Z", pos.z());
			}
		});
	}

	public List<ChunkPos> getChunks(UUID uuid) {
		return List.copyOf(townClaims.getOrDefault(uuid, List.of()));
	}

	public void addChunk(UUID uuid, ChunkPos pos) {
		if(!townClaims.containsKey(uuid))
			return;

		var chunks = townClaims.get(uuid);

		chunks.add(pos);

		townClaims.replace(uuid, chunks);
	}

	public void removeChunk(UUID uuid, ChunkPos pos) {
		if(!townClaims.containsKey(uuid))
			return;

		var chunks = townClaims.get(uuid);

		chunks.remove(pos);

		townClaims.replace(uuid, chunks);
	}

	public Town getTown(ServerLevel level, ChunkPos pos) {
		var townData = level.getScoreboard().getComponent(TownshipsComponents.TOWNS_COMPONENT);
		Town town = null;

		for(Map.Entry<UUID, List<ChunkPos>> entry : townClaims.entrySet()) {
			if(entry.getValue().contains(pos))
				town = townData.getTown(entry.getKey());
		}

		return town;
	}
}
