package dev.cammiescorner.townships.mixin;

import com.mojang.authlib.GameProfile;
import dev.cammiescorner.townships.init.TownshipsComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
	@Shadow
	public abstract ServerLevel level();

	public ServerPlayerMixin(Level level, GameProfile gameProfile) { super(level, gameProfile); }

	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(CallbackInfo info) {
		var oldChunkPos = ChunkPos.containing(BlockPos.containing(oldPosition()));

		if(oldChunkPos != chunkPosition()) {
			var oldClaimData = level().getComponent(TownshipsComponents.CLAIMS_COMPONENT);
			var claimData = level().getComponent(TownshipsComponents.CLAIMS_COMPONENT);
			var oldTown = oldClaimData.getTown(level(), oldChunkPos);
			var town = claimData.getTown(level(), chunkPosition());

			if(oldTown != town) {
				if(town == null)
					sendOverlayMessage(Component.literal("Wilderness").withStyle(ChatFormatting.GREEN));
				else
					sendOverlayMessage(Component.literal(town.getDisplayName()).withStyle(ChatFormatting.GOLD));
			}
		}
	}
}