package net.atlas.combatify.config.impl.food;

import com.mojang.serialization.MapCodec;
import net.atlas.combatify.config.impl.JSImpl;
import net.atlas.combatify.config.wrapper.FoodDataWrapper;
import net.atlas.combatify.config.wrapper.FoodPropertiesWrapper;
import net.atlas.combatify.config.wrapper.PlayerWrapper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;

public record JSFoodImpl(JSImpl handler) implements FoodImpl {
	public static final Identifier ID = Identifier.withDefaultNamespace("javascript");
	public static final MapCodec<JSFoodImpl> CODEC = JSImpl.CODEC.fieldOf("script").xmap(JSFoodImpl::new, JSFoodImpl::handler);

	@Override
	public boolean addFood(FoodData foodData, int food, float saturation) {
		return handler.execFunc("addFood(foodData, food, saturation)", new FoodDataWrapper(foodData), food, saturation);
	}

	@Override
	public boolean processHungerTick(FoodData foodData, Player player) {
		return handler.execFunc("processHungerTick(foodData, player)", new FoodDataWrapper(foodData), new PlayerWrapper<>(player));
	}

	@Override
	public boolean canFastHeal(FoodData foodData, Player player) {
		return handler.execFunc("canFastHeal(foodData, player)", new FoodDataWrapper(foodData), new PlayerWrapper<>(player));
	}

	@Override
	public boolean canFastHealRaw(int foodLevel, float saturationLevel, float exhaustionLevel) {
		return handler.execFunc("canFastHealRaw(foodLevel, saturationLevel, exhaustionLevel)", foodLevel, saturationLevel, exhaustionLevel);
	}

	@Override
	public boolean fastHeal(FoodData foodData, Player player) {
		return handler.execFunc("fastHeal(foodData, player)", new FoodDataWrapper(foodData), new PlayerWrapper<>(player));
	}

	@Override
	public boolean heal(FoodData foodData, Player player) {
		return handler.execFunc("heal(foodData, player)", new FoodDataWrapper(foodData), new PlayerWrapper<>(player));
	}

	@Override
	public boolean shouldOverrideAppleSkin() {
		return handler.execFunc("shouldOverrideAppleSkin()");
	}

	@Override
	public float getMinimumSprintLevel(float original, Player player) {
		return (float) handler.execGetterFunc(original, "getMinimumSprintLevel(player)", new PlayerWrapper<>(player));
	}

	@Override
	public float editAppleSkinHealthGained(int foodLevel, float saturationLevel, float exhaustionLevel) {
		return (float) handler.execGetterFunc(0.0, "editAppleSkinHealthGained(foodLevel, saturationLevel, exhaustionLevel)", foodLevel, saturationLevel, exhaustionLevel);
	}

	@Override
	public float estimateNewSaturationLevel(float original, FoodData foodData, Player player, FoodProperties foodProperties) {
		return (float) handler.execGetterFunc(original, "estimateNewSaturationLevel(foodData, player, foodProperties)", new FoodDataWrapper(foodData), new PlayerWrapper<>(player), new FoodPropertiesWrapper(foodProperties));
	}

	@Override
	public float estimateGainedSaturationLevel(float original, FoodData foodData, Player player, FoodProperties foodProperties) {
		return (float) handler.execGetterFunc(original, "estimateGainedSaturationLevel(foodData, player, foodProperties)", new FoodDataWrapper(foodData), new PlayerWrapper<>(player), new FoodPropertiesWrapper(foodProperties));
	}

	@Override
	public int getMinimumHealingLevel(int original) {
		return (int) handler.execGetterFunc(original, "getMinimumHealingLevel()");
	}

	@Override
	public int getMinimumFastHealingLevel(int original) {
		return (int) handler.execGetterFunc(original, "getMinimumFastHealingLevel()");
	}

	@Override
	public int getFastHealTicks(int original) {
		return (int) (handler.execGetterFunc(original / 20.0, "getFastHealSeconds()") * 20);
	}

	@Override
	public int getHealTicks(int original) {
		return (int) (handler.execGetterFunc(original / 20.0, "getHealSeconds()") * 20);
	}

	@Override
	public int getStarvationTicks(int original) {
		return (int) (handler.execGetterFunc(original / 20.0, "getStarvationSeconds()") * 20);
	}

	@Override
	public int estimateNewFoodLevel(int original, FoodData foodData, Player player, FoodProperties foodProperties) {
		return (int) handler.execGetterFunc(original, "estimateNewFoodLevel(foodData, player, foodProperties)", new FoodDataWrapper(foodData), new PlayerWrapper<>(player), new FoodPropertiesWrapper(foodProperties));
	}

	@Override
	public int estimateGainedFoodLevel(int original, FoodData foodData, Player player, FoodProperties foodProperties) {
		return (int) handler.execGetterFunc(original, "estimateGainedFoodLevel(foodData, player, foodProperties)", new FoodDataWrapper(foodData), new PlayerWrapper<>(player), new FoodPropertiesWrapper(foodProperties));
	}

	@Override
	public MapCodec<? extends FoodImpl> type() {
		return CODEC;
	}
}
