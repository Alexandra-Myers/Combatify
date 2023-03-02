package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.config.AtlasConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(FoodData.class)
public class FoodDataMixin {

	@Shadow
	private int foodLevel;


	@ModifyConstant(method = "tick", constant = @Constant(intValue = 18))
	public int changeConst(int constant) {
		if(AtlasConfig.saturationHealing) return constant;
		return 7;
	}

	@ModifyConstant(method = "tick", constant = @Constant(intValue = 20))
	public int changeConst2(int constant) {
		return 1000000;
	}

	@ModifyConstant(method = "tick", constant = @Constant(intValue = 80,ordinal = 0))
	public int redirectTickTimer(int constant) {
		return 40;
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V",ordinal = 1))
	public void modifyNaturalHealing(FoodData instance, float exhaustion) {
		if(AtlasConfig.saturationHealing) {
			instance.addExhaustion(exhaustion);
			return;
		}
		int randomNumber = Mth.randomBetweenInclusive(new Random(), 1, 2);
		if(randomNumber == 2) {
			--foodLevel;
		}
	}

}
