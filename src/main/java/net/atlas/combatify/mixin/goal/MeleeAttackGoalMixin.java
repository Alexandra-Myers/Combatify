package net.atlas.combatify.mixin.goal;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.atlas.combatify.config.ConfigurableEntityData;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MeleeAttackGoal.class)
public class MeleeAttackGoalMixin {
	@Shadow
	@Final
	protected PathfinderMob mob;


	@ModifyExpressionValue(method = "canUse", at = @At(value = "CONSTANT", args = "longValue=20"))
	public long replaceConstWithConfig(long original) {
		ConfigurableEntityData configurableEntityData = MethodHandler.forEntity(mob);
		if (configurableEntityData != null && configurableEntityData.attackInterval() != null)
			return configurableEntityData.attackInterval();
		return original;
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
		ConfigurableEntityData configurableEntityData = MethodHandler.forEntity(mob);
		if (configurableEntityData != null && configurableEntityData.attackInterval() != null)
			return configurableEntityData.attackInterval();
		return original;
	}
}
