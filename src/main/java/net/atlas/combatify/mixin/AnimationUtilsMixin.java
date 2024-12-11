package net.atlas.combatify.mixin;

import net.atlas.combatify.util.ClientMethodHandler;
import net.minecraft.client.model.AnimationUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AnimationUtils.class)
public class AnimationUtilsMixin {
	@ModifyVariable(method = "animateZombieArms", at = @At(value = "STORE"), index = 7)
	private static float modifyWhenGuarding(float value) {
		return ClientMethodHandler.modifyGuardingXRot(value);
	}
}
