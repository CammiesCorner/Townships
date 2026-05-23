package dev.cammiescorner.townships.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class Town {
	public static final Codec<Town> CODEC = RecordCodecBuilder.create(townInstance -> townInstance.group(
			Codec.unboundedMap(UUIDUtil.STRING_CODEC, Member.CODEC).fieldOf("members").forGetter(Town::getMembers),
			Codec.STRING.fieldOf("name").forGetter(Town::getName),
			Codec.STRING.fieldOf("display_name").forGetter(Town::getDisplayName),
			Codec.INT.fieldOf("gold").forGetter(Town::getGold)
	).apply(townInstance, (members, name, displayName, gold) -> {
		var town = new Town(members, name, gold);

		town.setDisplayName(displayName);

		return town;
	}));
	private final Map<UUID, Member> members = new HashMap<>();
	private String name;
	private String displayName;
	private int gold;

	public Town(Map<UUID, Member> members, String name, int gold) {
		this.members.putAll(members);
		this.name = name;
		this.displayName = name;
		this.gold = gold;
	}

	public Map<UUID, Member> getMembers() {
		return Map.copyOf(members);
	}

	public Member getMember(EntityReference<Player> player) {
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}
}
