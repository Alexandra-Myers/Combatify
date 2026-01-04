package net.atlas.combatify.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwingAnimationType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static net.atlas.combatify.util.MethodHandler.getAttackStrengthScale;

@Mixin(LivingEntity.class)
public abstract class ClientAttackAnimMixin {

	@Shadow
	public abstract ItemStack getItemInHand(InteractionHand interactionHand);

	@ModifyReturnValue(method = "getAttackAnim", at = @At("RETURN"))
	public float modAnim(float original, @Local(ordinal = 0, argsOnly = true) float tickDelta) {
		if (CombatifyClient.rhythmicAttacks.get().toBoolean(!Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) && getItemInHand(InteractionHand.MAIN_HAND).getSwingAnimation().type().equals(SwingAnimationType.WHACK)) {
			float charge = (Combatify.CONFIG.chargedAttacks() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) ? 1.95F : 0.9F;
			return original > 0.4F && getAttackStrengthScale(LivingEntity.class.cast(this), tickDelta) < charge ? 0.4F + 0.6F * (float)Math.pow((original - 0.4F) / 0.6F, 4.0) : original;
		}
		return original;
	}
}
