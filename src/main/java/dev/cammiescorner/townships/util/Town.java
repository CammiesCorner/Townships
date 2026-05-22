package dev.cammiescorner.townships.util;

import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public class Town {
	private final List<EntityReference<Player>> members = new ArrayList<>();
	private EntityReference<Player> owner;
	private String displayName;
	private float gold;

	public Town(EntityReference<Player> owner, String displayName, float gold) {
		this.owner = owner;
		this.displayName = displayName;
		this.gold = gold;
	}

	public List<EntityReference<Player>> getMembers() {
		List<EntityReference<Player>> members = new ArrayList<>();

		members.add(this.owner);
		members.addAll(this.members);

		return List.copyOf(members);
	}

	public void addMember(EntityReference<Player> reference) {
		if(!members.contains(reference))
			members.add(reference);
	}

	public void addMember(Player player) {
		addMember(EntityReference.of(player));
	}

	public void removeMember(Player player) {
		members.remove(EntityReference.of(player));
	}

	public EntityReference<Player> getOwner() {
		return owner;
	}

	public void setOwner(EntityReference<Player> owner) {
		this.owner = owner;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public float getGold() {
		return gold;
	}

	public void setGold(float gold) {
		this.gold = gold;
	}

	public static Town fromData(ValueInput input) {
		EntityReference<Player> owner = EntityReference.read(input, "OwnerId");
		var displayName = input.getStringOr("DisplayName", "");
		var gold = input.getFloatOr("Gold", 0);
		var memberData = input.childrenListOrEmpty("Members");
		var town = new Town(owner, displayName, gold);

		for(ValueInput memberDatum : memberData) {
			town.addMember(EntityReference.read(memberDatum, "MemberId"));
		}

		return town;
	}

	public void toData(ValueOutput output) {
		EntityReference.store(getOwner(), output, "OwnerId");
		output.putString("DisplayName", getDisplayName());
		output.putFloat("Gold", getGold());

		var memberData = output.childrenList("Members");

		for(EntityReference<Player> member : members) {
			EntityReference.store(member, memberData.addChild(), "MemberId");
		}
	}
}
