package net.atlas.combatify.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.client.ClientMethodHandler;
import net.atlas.combatify.extensions.MinecraftExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@ModifyExpressionValue(method = "pick(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;"))
	public HitResult injectBedrockBridging(HitResult original, @Local(ordinal = 0) Entity entity, @Local(ordinal = 0, argsOnly = true) float f) {
		HitResult redirectedResult = ClientMethodHandler.redirectResult(original);
		if ((original == null || original.getType() == HitResult.Type.MISS) && redirectedResult == null && Combatify.CONFIG.bedrockBridging() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			Vec3 viewVector = entity.getViewVector(1.0F);
			if (entity.onGround() && viewVector.y < -0.7) {
				Vec3 adjustedPos = entity.getPosition(f).add(0.0, -0.1, 0.0);
				Vec3 adjustedVector = adjustedPos.add(viewVector.x, 0.0, viewVector.z);
				BlockHitResult result = entity.level().clip(new ClipContext(adjustedVector, adjustedPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
				result.combatify$setIsLedgeEdge();

				original = result;
			}
		} else if (redirectedResult != null) {
			original = redirectedResult;
		}

		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA) || Combatify.CONFIG.aimAssistTicks() == 0)
			((MinecraftExtensions)minecraft).combatify$setAimAssistHitResult(null);
		else if (original.getType() == HitResult.Type.ENTITY)
			((MinecraftExtensions)minecraft).combatify$setAimAssistHitResult(original);
		return original;
	}
}
