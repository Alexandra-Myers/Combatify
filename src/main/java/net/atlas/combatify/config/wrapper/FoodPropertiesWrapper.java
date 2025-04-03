package net.atlas.combatify.config.wrapper;

import net.minecraft.world.food.FoodProperties;

import java.util.List;
import java.util.Optional;

public record FoodPropertiesWrapper(FoodProperties value) implements GenericAPIWrapper<FoodProperties> {

	public int eatDurationTicks() {
		return value.eatDurationTicks();
	}

	public int nutrition() {
		return value.nutrition();
	}

	public float saturation() {
		return value.saturation();
	}

	public boolean canAlwaysEat() {
		return value.canAlwaysEat();
	}

	public float eatSeconds() {
		return value.eatSeconds();
	}

	public Optional<ItemStackWrapper> usingConvertsTo() {
		return value.usingConvertsTo().map(ItemStackWrapper::new);
	}

	public List<FoodProperties.PossibleEffect> effects() {
		return value.effects();
	}
	@Override
	public FoodProperties unwrap() {
		return value;
	}
}
