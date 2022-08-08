package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FoodData.class)
public class FoodDataMixin {

	@ModifyConstant(method = "tick", constant = @Constant(intValue = 18))
	public int changeConst(int constant) {
		return 6;
	}

	@ModifyConstant(method = "tick", constant = @Constant(intValue = 80,ordinal = 0))
	public int redirectTickTimer(int constant) {
		return 40;
	}

	@ModifyConstant(method = "tick", constant = @Constant(floatValue = 6.0F,ordinal = 2))
	public float redirectExhaustion(float constant) {
		return 3.0F;
	}

}
