package net.atlas.combatify.mixin.goal;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.mixin.accessor.FleeSunGoalAccessor;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Goal.class)
public class GoalMixin {
	@Inject(method = "stop", at = @At(value = "HEAD"))
	public void injectSprinting(CallbackInfo ci) {
		if (Combatify.CONFIG.mobsCanSprint() && Goal.class.cast(this) instanceof FleeSunGoal fleeSunGoal) {
			try {
				PathfinderMob mob = ((FleeSunGoalAccessor)fleeSunGoal).getMob();
				mob.combatify$setOverrideSprintLogic(false);
				mob.setSprinting(false);
			} catch (Exception e) {
				Combatify.LOGGER.error("Failed to get mob to update sprint for! Exception: {}", e);
			}
		}
	}
}
