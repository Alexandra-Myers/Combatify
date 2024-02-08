package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.atlas.combatify.Combatify;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractWindCharge.class)
public abstract class AbstractWindChargeMixin {
	@ModifyExpressionValue(method = "onHitEntity", at = @At(value = "CONSTANT", args = "floatValue=1.0F"))
	public float configDamage(float original) {
		return Combatify.CONFIG.windChargeDamage().floatValue();
	}
}
