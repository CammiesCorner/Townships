package dev.cammiescorner.townships.api.event;

import dev.cammiescorner.townships.util.Member;
import dev.cammiescorner.townships.util.Town;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public final class ClaimEvents {

    public static final Event<TryClaimChunk> TRY_CLAIM_CHUNK = EventFactory.createArrayBacked(TryClaimChunk.class, listeners -> (town, member, level, chunkPos) -> {
        for (var listener : listeners) {
            if(listener.allowAction(town, member, level, chunkPos) instanceof Component error) {
                return error;
            }
        }

        return null;
    });

    public static final Event<DidClaimChunk> CLAIMED_CHUNK = EventFactory.createArrayBacked(DidClaimChunk.class, listeners -> (town, member, level, chunkPos) -> {
        for (var listener : listeners) {
            listener.onClaimAction(town, member, level, chunkPos);
        }
    });

    public static final Event<TryClaimChunk> TRY_UNCLAIM_CHUNK = EventFactory.createArrayBacked(TryClaimChunk.class, listeners -> (town, member, level, chunkPos) -> {
        for (var listener : listeners) {
            if(listener.allowAction(town, member, level, chunkPos) instanceof Component error) {
                return error;
            }
        }

        return null;
    });

    public static final Event<DidClaimChunk> UNCLAIMED_CHUNK = EventFactory.createArrayBacked(DidClaimChunk.class, listeners -> (town, member, level, chunkPos) -> {
        for (var listener : listeners) {
            listener.onClaimAction(town, member, level, chunkPos);
        }
    });

    public interface TryClaimChunk {

        /**
         * Check whether a player can claim/unclaim a chunk.
         * @return an error message to be displayed to the user, or {@code null} to allow the action
         */
        Component allowAction(Town town, Member member, Level level, ChunkPos chunkPos);
    }

    public interface DidClaimChunk {
        void onClaimAction(Town town, Member member, Level level, ChunkPos chunkPos);
    }
}
