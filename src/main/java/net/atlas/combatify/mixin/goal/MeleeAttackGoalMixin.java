package net.atlas.combatify.mixin.goal;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableEntityData;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MeleeAttackGoal.class)
public class MeleeAttackGoalMixin {
	@Shadow
	@Final
	protected PathfinderMob mob;

	@WrapMethod(method = "isTimeToAttack")
	public boolean modResult(Operation<Boolean> original) {
		var attackSpeed = mob.getAttribute(Attributes.ATTACK_SPEED);
		boolean shouldOverrideDefaultAttackInterval = (attackSpeed != null && attackSpeed.getValue() == attackSpeed.getBaseValue()) || mob.getType().is(Combatify.HAS_BOOSTED_SPEED);
		if (Combatify.CONFIG.mobsUsePlayerAttributes() && shouldOverrideDefaultAttackInterval) return mob.combatify$isAttackAvailable(0, mob.getItemInHand(InteractionHand.MAIN_HAND));
		return original.call();
	}

	@ModifyExpressionValue(method = "canUse", at = @At(value = "CONSTANT", args = "longValue=20"))
	public long replaceConstWithConfig(long original) {
		var attackSpeed = mob.getAttribute(Attributes.ATTACK_SPEED);
		boolean shouldOverrideDefaultAttackInterval = (attackSpeed != null && attackSpeed.getValue() == attackSpeed.getBaseValue()) || mob.getType().is(Combatify.HAS_BOOSTED_SPEED);
		if (Combatify.CONFIG.mobsUsePlayerAttributes() && shouldOverrideDefaultAttackInterval) return MethodHandler.getCurrentItemAttackStrengthDelay(mob);
		ConfigurableEntityData configurableEntityData = MethodHandler.forEntity(mob);
		if (configurableEntityData != null && configurableEntityData.attackInterval() != null)
			return configurableEntityData.attackInterval();
		return original;
	}

	@Inject(method = "resetAttackCooldown", at = @At("HEAD"))
	public void addAttackCooldownReset(CallbackInfo ci) {
		mob.combatify$resetAttackStrengthTicker(true);
	}

	@ModifyExpressionValue(method = "resetAttackCooldown", at = @At(value = "CONSTANT", args = "intValue=20"))
	public int replaceConstWithConfig1(int original) {
		ConfigurableEntityData configurableEntityData = MethodHandler.forEntity(mob);
		if (configurableEntityData != null && configurableEntityData.attackInterval() != null)
			return configurableEntityData.attackInterval();
		return original;
	}

	@ModifyExpressionValue(method = "getAttackInterval", at = @At(value = "CONSTANT", args = "intValue=20"))
	public int replaceConstWithConfig2(int original) {
		var attackSpeed = mob.getAttribute(Attributes.ATTACK_SPEED);
		boolean shouldOverrideDefaultAttackInterval = (attackSpeed != null && attackSpeed.getValue() == attackSpeed.getBaseValue()) || mob.getType().is(Combatify.HAS_BOOSTED_SPEED);
		if (Combatify.CONFIG.mobsUsePlayerAttributes() && shouldOverrideDefaultAttackInterval) return MethodHandler.getCurrentItemAttackStrengthDelay(mob);
		ConfigurableEntityData configurableEntityData = MethodHandler.forEntity(mob);
		if (configurableEntityData != null && configurableEntityData.attackInterval() != null)
			return configurableEntityData.attackInterval();
		return original;
	}
}
