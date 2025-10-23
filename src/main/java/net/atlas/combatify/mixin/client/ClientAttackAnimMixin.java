package net.atlas.combatify.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.atlas.combatify.CombatifyClient;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class ClientAttackAnimMixin {
	@ModifyReturnValue(method = "getAttackAnim", at = @At(value = "RETURN"))
	public float editAttackAnim(float original) {
		if (CombatifyClient.rhythmicAttacks.get()) {
			return original > 0.4F ? 0.4F + 0.6F * (float)Math.pow((original - 0.4F) / 0.6F, 4.0) : original;
		}
		return original;
	}
}
