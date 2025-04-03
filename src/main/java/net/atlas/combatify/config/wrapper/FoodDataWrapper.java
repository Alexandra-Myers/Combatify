package net.atlas.combatify.config.wrapper;

import net.minecraft.world.food.FoodData;

public record FoodDataWrapper(FoodData value) implements GenericAPIWrapper<FoodData> {
	public void eat(int i, float f) {
		value.eat(i, f);
	}

	public void eat(FoodPropertiesWrapper foodProperties) {
		value.eat(foodProperties.unwrap());
	}

	public int getFoodLevel() {
		return value.getFoodLevel();
	}

	public int getLastFoodLevel() {
		return value.getLastFoodLevel();
	}

	public boolean needsFood() {
		return value.needsFood();
	}

	public void addExhaustion(float exhaustion) {
		value.addExhaustion(exhaustion);
	}

	public float getExhaustionLevel() {
		return value.getExhaustionLevel();
	}

	public float getSaturationLevel() {
		return value.getSaturationLevel();
	}

	public void setFoodLevel(int foodLevel) {
		value.setFoodLevel(foodLevel);
	}

	public void setSaturation(float saturation) {
		value.setSaturation(saturation);
	}

	public void setExhaustion(float exhaustion) {
		value.setExhaustion(exhaustion);
	}
	@Override
	public FoodData unwrap() {
		return value;
	}
}
