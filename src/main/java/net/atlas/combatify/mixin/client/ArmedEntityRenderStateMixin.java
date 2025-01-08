package net.atlas.combatify.mixin.client;

import net.atlas.combatify.extensions.ArmedEntityRenderStateExtensions;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmedEntityRenderState.class)
public class ArmedEntityRenderStateMixin implements ArmedEntityRenderStateExtensions {
	@Unique
	public boolean mobIsGuarding;
	@Override
	public boolean combatify$mobIsGuarding() {
		return mobIsGuarding;
	}

	@Override
	public void combatify$setMobIsGuarding(boolean mobIsGuarding) {
		this.mobIsGuarding = mobIsGuarding;
	}

	@Inject(method = "extractArmedEntityRenderState", at = @At("RETURN"))
	private static void addGuardingFlag(LivingEntity livingEntity, ArmedEntityRenderState armedEntityRenderState, ItemModelResolver itemModelResolver, CallbackInfo ci) {
		armedEntityRenderState.combatify$setMobIsGuarding(false);
		if (livingEntity instanceof Mob mob) armedEntityRenderState.combatify$setMobIsGuarding(MethodHandler.isMobGuarding(mob));
	}
}
