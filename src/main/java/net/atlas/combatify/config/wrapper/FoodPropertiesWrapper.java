package net.atlas.combatify.config.wrapper;

import net.minecraft.world.food.FoodProperties;

public record FoodPropertiesWrapper(FoodProperties value) implements GenericAPIWrapper<FoodProperties> {

	public int nutrition() {
		return value.nutrition();
	}

	public float saturation() {
		return value.saturation();
	}

	public boolean canAlwaysEat() {
		return value.canAlwaysEat();
	}
	@Override
	public FoodProperties unwrap() {
		return value;
	}
}
