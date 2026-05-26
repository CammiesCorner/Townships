package dev.cammiescorner.townships.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public class ServerPlayerChunkEvents {

    public static final Event<ChunkposChange> ON_CHUNKPOS_CHANGE = EventFactory.createArrayBacked(ChunkposChange.class, listeners -> (player, oldChunk, newChunk) -> {
        for (ChunkposChange listener : listeners) {
            listener.onChunkChange(player, oldChunk, newChunk);
        }
    });

    @FunctionalInterface
    public interface ChunkposChange {

        void onChunkChange(ServerPlayer player, ChunkPos oldChunk, ChunkPos newChunk);
    }
}
