package net.atlas.combatify.config.impl.food;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.extension.LateBoundIdMapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import org.jetbrains.annotations.NotNull;

public interface FoodImpl {
	boolean addFood(FoodData foodData, int food, float saturation);
	boolean processHungerTick(FoodData foodData, Player player);
	boolean canFastHeal(FoodData foodData, Player player);
	boolean canFastHealRaw(int foodLevel, float saturationLevel, float exhaustionLevel);
	boolean fastHeal(FoodData foodData, Player player);
	boolean heal(FoodData foodData, Player player);
	boolean shouldOverrideAppleSkin();
	float getMinimumSprintLevel(float original, Player player);
	float editAppleSkinHealthGained(int foodLevel, float saturationLevel, float exhaustionLevel);
	float estimateNewSaturationLevel(float original, FoodData foodData, Player player, FoodProperties foodProperties);
	float estimateGainedSaturationLevel(float original, FoodData foodData, Player player, FoodProperties foodProperties);
	int getMinimumHealingLevel(int original);
	int getMinimumFastHealingLevel(int original);
	int getFastHealTicks(int original);
	int getHealTicks(int original);
	int getStarvationTicks(int original);
	int estimateNewFoodLevel(int original, FoodData foodData, Player player, FoodProperties foodProperties);
	int estimateGainedFoodLevel(int original, FoodData foodData, Player player, FoodProperties foodProperties);

	MapCodec<? extends FoodImpl> type();
	LateBoundIdMapper<@NotNull ResourceLocation, @NotNull MapCodec<? extends FoodImpl>> ID_MAPPER = new LateBoundIdMapper<>();
	Codec<FoodImpl> CODEC = ID_MAPPER.codec(ResourceLocation.CODEC)
		.dispatch(FoodImpl::type, mapCodec -> mapCodec);

	static void bootstrap() {
		ID_MAPPER.put(JSFoodImpl.ID, JSFoodImpl.CODEC);
		ID_MAPPER.put(CTSFoodImpl.ID, CTSFoodImpl.CODEC);
		ID_MAPPER.put(CombatifyFoodImpl.ID, CombatifyFoodImpl.CODEC);
	}
}
