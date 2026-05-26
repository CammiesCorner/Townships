package dev.cammiescorner.townships.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class Town {
	public static final Codec<Town> CODEC = RecordCodecBuilder.create(townInstance -> townInstance.group(
			UUIDUtil.STRING_CODEC.fieldOf("id").forGetter(Town::id),
			Codec.unboundedMap(UUIDUtil.STRING_CODEC, Member.CODEC).fieldOf("members").forGetter(Town::members),
			Codec.STRING.fieldOf("command_name").forGetter(Town::commandName),
			Codec.STRING.fieldOf("display_name").forGetter(Town::displayName),
			GlobalPos.CODEC.fieldOf("home_pos").forGetter(Town::homePos),
			Codec.INT.fieldOf("balance").forGetter(Town::balance)
	).apply(townInstance, Town::new));

    public static Town createFor(ServerPlayer player, ServerLevel homeDimension, BlockPos homePos, String name, @Nullable String displayName) {
		var id = UUID.randomUUID();
		var members = Map.of(player.getUUID(), new Member(player, Member.Rank.MAYOR));
		var displayNameActual = displayName != null ? displayName : name;
		return new Town(id, members, name.toLowerCase(Locale.ROOT), displayNameActual, new GlobalPos(homeDimension.dimension(), homePos), 0);
	}

	public UUID id() {
		return this.id;
	}

	private final UUID id;
	private final Map<UUID, Member> members = new HashMap<>();
	private String commandName;
	private String displayName;
	private GlobalPos homePos;
	private int balance;

	public Town(UUID id, Map<UUID, Member> members, String commandName, String displayName, GlobalPos homePos, int balance) {
		this.id = id;
		this.members.putAll(members);
		this.commandName = commandName;
		this.displayName = displayName;
		this.homePos = homePos;
		this.balance = balance;
	}

	public Map<UUID, Member> members() {
		return Collections.unmodifiableMap(members);
	}

	@Nullable
	public Member asMember(EntityReference<Player> player) {
		return members.get(player.getUUID());
	}

	public void addMember(Member member) {
		members.put(member.getPlayer().getUUID(), member);
	}

	public void addMember(EntityReference<Player> player) {
		addMember(new Member(player, Member.Rank.MEMBER));
	}

	public void removeMember(EntityReference<Player> player) {
		members.remove(player.getUUID());
	}

	public void setMemberRank(EntityReference<Player> player, Member.Rank rank) {
		members.put(player.getUUID(), new Member(player, rank));
	}

	public String commandName() {
		return commandName;
	}

	public void setName(String name) {
		this.commandName = name;
	}

	public String displayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public GlobalPos homePos() {
		return homePos;
	}

	public void setHomePos(GlobalPos homePos) {
		this.homePos = homePos;
	}

	public void setHome(Level level, BlockPos pos) {
		setHomePos(new GlobalPos(level.dimension(), pos));
	}

	public void setHome(Entity entity) {
		setHome(entity.level(), entity.blockPosition());
	}

	public int balance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}
}
