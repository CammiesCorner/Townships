package dev.cammiescorner.townships.init;

import dev.cammiescorner.townships.Townships;
import dev.cammiescorner.townships.component.level.ClaimsComponent;
import dev.cammiescorner.townships.component.scoreboard.TownsComponent;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentInitializer;
import org.ladysnake.cca.api.v8.component.CardinalComponent;
import org.ladysnake.cca.api.v8.level.LevelComponentFactoryRegistry;
import org.ladysnake.cca.api.v8.level.LevelComponentInitializer;

public class TownshipsComponents implements ScoreboardComponentInitializer, LevelComponentInitializer {
	public static final ComponentKey<TownsComponent> TOWNS_COMPONENT = createComponent("towns", TownsComponent.class);
	public static final ComponentKey<ClaimsComponent> CLAIMS_COMPONENT = createComponent("claims", ClaimsComponent.class);

	@Override
	public void registerScoreboardComponentFactories(ScoreboardComponentFactoryRegistry registry) {
		registry.registerScoreboardComponent(TOWNS_COMPONENT, TownsComponent::new);
	}

	@Override
	public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry) {
		registry.register(CLAIMS_COMPONENT, ClaimsComponent::new);
	}

	private static <T extends CardinalComponent> ComponentKey<T> createComponent(String name, Class<T> component) {
		return ComponentRegistry.getOrCreate(Townships.id(name), component);
	}
}
