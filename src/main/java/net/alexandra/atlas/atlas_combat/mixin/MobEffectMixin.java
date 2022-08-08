package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MobEffect.class)
public class MobEffectMixin {

	@ModifyConstant(method = "applyInstantenousEffect", constant = @Constant(intValue = 4))
	public int changeInstantHealth(int constant) {
		return 6;
	}
}
