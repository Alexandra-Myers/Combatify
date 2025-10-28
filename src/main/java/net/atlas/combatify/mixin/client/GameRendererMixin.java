package net.atlas.combatify.mixin.client;

import net.atlas.combatify.util.ClientMethodHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow
	@Final
	Minecraft minecraft;

	@Inject(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V"))
	private void modifyHitResult(float f, CallbackInfo ci) {
		HitResult hitResult = ClientMethodHandler.redirectResult(this.minecraft.hitResult);
		if (hitResult == null) return;
		this.minecraft.hitResult = hitResult;
		if (hitResult instanceof EntityHitResult entityHitResult) { // SHOULD always be true
			Entity picked = entityHitResult.getEntity();
			if (picked instanceof LivingEntity || picked instanceof ItemFrame) this.minecraft.crosshairPickEntity = picked;
		}
	}
}
