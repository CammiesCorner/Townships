package dev.cammiescorner.townships.mixin;

import com.mojang.authlib.GameProfile;
import dev.cammiescorner.townships.api.event.ServerPlayerChunkEvents;
import dev.cammiescorner.townships.api.inject.ServerPlayerExt;
import dev.cammiescorner.townships.component.scoreboard.TownsComponent;
import dev.cammiescorner.townships.util.Member;
import dev.cammiescorner.townships.util.Town;
import net.minecraft.core.BlockPos;
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

import java.util.Optional;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements ServerPlayerExt {
	@Shadow
	public abstract ServerLevel level();

	public ServerPlayerMixin(Level level, GameProfile gameProfile) { super(level, gameProfile); }

	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(CallbackInfo info) {
		var oldChunkPos = ChunkPos.containing(BlockPos.containing(oldPosition()));
		var newChunkPos = chunkPosition();

		if(!oldChunkPos.equals(newChunkPos)) {
			ServerPlayerChunkEvents.ON_CHUNKPOS_CHANGE.invoker().onChunkChange((ServerPlayer)(Object) this, oldChunkPos, newChunkPos);
		}
	}

	@Override
	public Optional<Town> townships$getTown() {
		var townComponent = TownsComponent.get(level());
		return townComponent.towns().values().stream().filter(it -> it.members().containsKey(this.getUUID())).findFirst();
	}

	@Override
	public Optional<Member> townships$asTownMember() {
		return TownsComponent.get(level()).towns().values().stream().flatMap(town -> town.members().values().stream()).filter(member -> member.getPlayer().matches(this)).findFirst();
	}
}
