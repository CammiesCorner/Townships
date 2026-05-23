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
		MAYOR("mayor"), ADMINISTRATOR("administrator"), BANKER("banker"), MEMBER("member");

		public static final Codec<Rank> CODEC = StringRepresentable.fromEnum(Rank::values);
		private final String serializedName;

		Rank(String serializedName) {
			this.serializedName = serializedName;
		}

		@Override
		public String getSerializedName() {
			return serializedName;
		}
	}
}
