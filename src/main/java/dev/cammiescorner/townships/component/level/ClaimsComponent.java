package dev.cammiescorner.townships.component.level;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.ladysnake.cca.api.v8.component.CardinalComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClaimsComponent implements CardinalComponent {
	private final Map<String, List<ChunkPos>> townClaims = new HashMap<>();
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
			var townName = input.getString("TownName");

			for(ValueInput i : posList) {
				list.add(new ChunkPos(i.getIntOr("X", 0), i.getIntOr("Z", 0)));
			}

			townName.ifPresent(s -> townClaims.put(s, list));
		}
	}

	@Override
	public void writeData(ValueOutput writeView) {
		var claimsList = writeView.childrenList("TownClaims");

		townClaims.forEach((name, chunkPos) -> {
			var output = claimsList.addChild();
			var posList = output.childrenList("ChunkPoses");

			output.putString("TownName", name);

			for(ChunkPos pos : chunkPos) {
				var o = posList.addChild();

				o.putInt("X", pos.x());
				o.putInt("Z", pos.z());
			}
		});
	}

	public List<ChunkPos> getChunks(String name) {
		return List.copyOf(townClaims.getOrDefault(name, List.of()));
	}

	public void addChunk(String name, ChunkPos pos) {
		if(!townClaims.containsKey(name))
			return;

		var chunks = townClaims.get(name);

		chunks.add(pos);

		townClaims.replace(name, chunks);
	}

	public void removeChunk(String name, ChunkPos pos) {
		if(!townClaims.containsKey(name))
			return;

		var chunks = townClaims.get(name);

		chunks.remove(pos);

		townClaims.replace(name, chunks);
	}
}
