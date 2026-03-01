package net.atlas.combatify.config.impl.food;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;

import static java.lang.Float.isFinite;

public record CTSFoodImpl(boolean ctsSaturationCap, boolean ctsHealing, int minimumSprintLevel, int minimumHealingLevel, int minimumFastHealingLevel, float fastHealSeconds, float healSeconds, float starvationSeconds) implements FoodImpl {
	public static final Identifier ID = Identifier.withDefaultNamespace("combat_test_8c");
	public static final MapCodec<CTSFoodImpl> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(Codec.BOOL.optionalFieldOf("cts_saturation_cap", true).forGetter(CTSFoodImpl::ctsSaturationCap),
				Codec.BOOL.optionalFieldOf("cts_healing", true).forGetter(CTSFoodImpl::ctsHealing),
				ExtraCodecs.intRange(0, 20).optionalFieldOf("minimum_sprint_level", 6).forGetter(CTSFoodImpl::minimumSprintLevel),
				ExtraCodecs.intRange(0, 20).optionalFieldOf("minimum_healing_level", 7).forGetter(CTSFoodImpl::minimumHealingLevel),
				ExtraCodecs.intRange(0, 21).optionalFieldOf("minimum_fast_healing_level", 21).forGetter(CTSFoodImpl::minimumFastHealingLevel),
				ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("fast_heal_seconds", 0.5F).forGetter(CTSFoodImpl::fastHealSeconds),
				ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("heal_seconds", 2F).forGetter(CTSFoodImpl::healSeconds),
				ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("starvation_seconds", 2F).forGetter(CTSFoodImpl::healSeconds))
			.apply(instance, CTSFoodImpl::new));

	@Override
	public boolean addFood(FoodData foodData, int food, float saturation) {
		if (!this.ctsSaturationCap) return false;
		foodData.setFoodLevel(Math.max(Math.min(food + foodData.getFoodLevel(), 20), 0));
		foodData.setSaturation(Math.max(foodData.getSaturationLevel(), saturation));
		return true;
	}

	@Override
	public boolean processHungerTick(FoodData foodData, Player player) {
		return false;
	}

	@Override
	public boolean canFastHeal(FoodData foodData, Player player) {
		return this.minimumFastHealingLevel < 21;
	}

	@Override
	public boolean canFastHealRaw(int foodLevel, float saturationLevel, float exhaustionLevel) {
		return this.minimumFastHealingLevel < 21;
	}

	@Override
	public boolean fastHeal(FoodData foodData, Player player) {
		return false;
	}

	@Override
	public boolean heal(FoodData foodData, Player player) {
		if (!this.ctsHealing) return false;
		player.heal(1.0F);
		if (player.level().getRandom().nextBoolean()) foodData.setFoodLevel(foodData.getFoodLevel() - 1);
		return true;
	}

	@Override
	public boolean shouldOverrideAppleSkin() {
		return this.ctsHealing;
	}

	@Override
	public float getMinimumSprintLevel(float original, Player player) {
		return this.minimumSprintLevel;
	}

	@Override
	public float editAppleSkinHealthGained(int foodLevel, float saturationLevel, float exhaustionLevel) {
		var health = 0;
		if (isFinite(exhaustionLevel) && isFinite(saturationLevel)) {
			while(foodLevel >= getMinimumHealingLevel(0)) {
				while(exhaustionLevel > 4.0F) {
					exhaustionLevel -= 4.0F;
					if (saturationLevel > 0) {
						saturationLevel = Math.max(saturationLevel - 1, 0);
					} else {
						--foodLevel;
					}
				}

				if (canFastHealRaw(foodLevel, saturationLevel, exhaustionLevel) && foodLevel >= getMinimumFastHealingLevel(0)) {
					var limitedSaturationLevel = Math.min(saturationLevel, 6.0);
					var exhaustionUntilAboveMax = 4.0 - exhaustionLevel + 0.00000001;
					var numIterationsUntilAboveMax = Math.max(1, Math.ceil(exhaustionUntilAboveMax / limitedSaturationLevel));
					health += (int) (limitedSaturationLevel / 6.0 * numIterationsUntilAboveMax);
					exhaustionLevel += (float) (limitedSaturationLevel * numIterationsUntilAboveMax);
				} else if (foodLevel >= getMinimumHealingLevel(0)) {
					++health;
					if (health % 2 == 0) foodLevel--;
				}
			}

			return health;
		} else {
			return 0;
		}
	}

	@Override
	public float estimateNewSaturationLevel(float original, FoodData foodData, Player player, FoodProperties foodProperties) {
		if (!this.ctsSaturationCap) return Math.min(foodData.getSaturationLevel() + foodProperties.saturation(), estimateNewFoodLevel(0, foodData, player, foodProperties));
		return Math.max(foodData.getSaturationLevel(), foodProperties.saturation());
	}

	@Override
	public float estimateGainedSaturationLevel(float original, FoodData foodData, Player player, FoodProperties foodProperties) {
		return estimateNewSaturationLevel(0, foodData, player, foodProperties) - foodData.getSaturationLevel();
	}

	@Override
	public int getMinimumHealingLevel(int original) {
		return this.minimumHealingLevel;
	}

	@Override
	public int getMinimumFastHealingLevel(int original) {
		return this.minimumFastHealingLevel;
	}

	@Override
	public int getFastHealTicks(int original) {
		return (int) (this.fastHealSeconds * 20);
	}

	@Override
	public int getHealTicks(int original) {
		return (int) (this.healSeconds * 20);
	}

	@Override
	public int getStarvationTicks(int original) {
		return (int) (this.starvationSeconds * 20);
	}

	@Override
	public int estimateNewFoodLevel(int original, FoodData foodData, Player player, FoodProperties foodProperties) {
		return Math.min(foodData.getFoodLevel() + foodProperties.nutrition(), 20);
	}

	@Override
	public int estimateGainedFoodLevel(int original, FoodData foodData, Player player, FoodProperties foodProperties) {
		return estimateNewFoodLevel(0, foodData, player, foodProperties) - foodData.getFoodLevel();
	}

	@Override
	public MapCodec<? extends FoodImpl> type() {
		return CODEC;
	}
}
