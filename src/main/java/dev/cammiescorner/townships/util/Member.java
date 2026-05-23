package dev.cammiescorner.townships.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.player.Player;

public class Member {
	public static final Codec<Member> CODEC = RecordCodecBuilder.create(memberInstance -> memberInstance.group(
			EntityReference.<Player>codec().fieldOf("player").forGetter(Member::getPlayer),
			Rank.CODEC.fieldOf("rank").forGetter(Member::getRank)
	).apply(memberInstance, Member::new));

	private final EntityReference<Player> player;
	private Rank rank;

	public Member(EntityReference<Player> player, Rank rank) {
		this.player = player;
		this.rank = rank;
	}

	public Member(Player player, Rank rank) {
		this(EntityReference.of(player), rank);
	}

	public EntityReference<Player> getPlayer() {
		return player;
	}

	public Rank getRank() {
		return rank;
	}

	public void setRank(Rank rank) {
		this.rank = rank;
	}

	public enum Rank implements StringRepresentable {
		MAYOR("mayor", true), ADMINISTRATOR("administrator", true), BANKER("banker", false), MEMBER("member", false);

		public static final Codec<Rank> CODEC = StringRepresentable.fromEnum(Rank::values);
		private final String serializedName;
		private final boolean canClaim;

		Rank(String serializedName, boolean canClaim) {
			this.serializedName = serializedName;
			this.canClaim = canClaim;
		}

		@Override
		public String getSerializedName() {
			return serializedName;
		}

		public boolean canClaim() {
			return canClaim;
		}
	}
}
