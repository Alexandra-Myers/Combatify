package net.atlas.combatify.mixin.goal;

import net.atlas.combatify.Combatify;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FleeSunGoal.class)
public class FleeSunGoalMixin {
	@Shadow
	@Final
	protected PathfinderMob mob;

	@Inject(method = "start", at = @At("HEAD"))
	public void injectSprinting(CallbackInfo ci) {
		if (Combatify.CONFIG.mobsCanSprint()) {
			mob.combatify$setOverrideSprintLogic(true);
			mob.setSprinting(true);
		}
	}
}
