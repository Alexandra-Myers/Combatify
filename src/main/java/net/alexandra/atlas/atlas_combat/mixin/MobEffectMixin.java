package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.Collections;

@Mixin(MobEffect.class)
public class MobEffectMixin {

	@Unique
	public final int instantHealthBonus = (int) AtlasCombat.helper.getValue(Collections.singleton("instantHealthBonus")).value();

	@ModifyConstant(method = "applyInstantenousEffect", constant = @Constant(intValue = 4))
	public int changeInstantHealth(int constant) {
		return instantHealthBonus;
	}
}
