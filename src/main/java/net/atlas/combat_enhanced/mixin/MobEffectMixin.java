package net.atlas.combat_enhanced.mixin;

import net.atlas.combat_enhanced.CombatEnhanced;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MobEffect.class)
public class MobEffectMixin {

	@ModifyConstant(method = "applyInstantenousEffect", constant = @Constant(intValue = 4))
	public int changeInstantHealth(int constant) {
		return CombatEnhanced.CONFIG.instantHealthBonus();
	}
}
