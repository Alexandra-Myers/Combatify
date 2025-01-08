package net.atlas.combatify.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static net.atlas.combatify.util.MethodHandler.getAttackStrengthScale;

@Mixin(LivingEntity.class)
public class ClientAttackAnimMixin {
	@ModifyReturnValue(method = "getAttackAnim", at = @At("RETURN"))
	public float modAnim(float original, @Local(ordinal = 0, argsOnly = true) float tickDelta) {
		if(CombatifyClient.rhythmicAttacks.get()) {
			float charge = Combatify.CONFIG.chargedAttacks() ? 1.95F : 0.9F;
			return original > 0.4F && getAttackStrengthScale(LivingEntity.class.cast(this), tickDelta) < charge ? 0.4F + 0.6F * (float)Math.pow((original - 0.4F) / 0.6F, 4.0) : original;
		}
		return original;
	}
}
