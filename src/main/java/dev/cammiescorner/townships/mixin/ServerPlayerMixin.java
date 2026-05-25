package dev.cammiescorner.townships.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
	public ServerPlayerMixin(Level level, GameProfile gameProfile) { super(level, gameProfile); }

	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(CallbackInfo info) {
		var oldChunkPos = ChunkPos.containing(BlockPos.containing(oldPosition()));

		if(oldChunkPos != chunkPosition()) {
			
		}
	}
}