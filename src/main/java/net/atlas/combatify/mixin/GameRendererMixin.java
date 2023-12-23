package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@SuppressWarnings("unused")
@Mixin(GameRenderer.class)
abstract class GameRendererMixin implements ResourceManagerReloadListener/*, AutoCloseable*/ {
    @Shadow
	@Final
	Minecraft minecraft;

    @ModifyExpressionValue(method = "pick(F)V", at = @At(value = "CONSTANT", args = "doubleValue=3.0"))
    private double getActualAttackRange(double original, @Local(ordinal = 0) Entity entity) {
        if (entity instanceof Player player)
			return MethodHandler.getCurrentAttackReach(player,0.0F);
		else if (this.minecraft.player != null)
            return MethodHandler.getCurrentAttackReach(minecraft.player,0.0F);
        return original - 0.5;
    }

	@ModifyExpressionValue(method = "pick(F)V", at = @At(value = "CONSTANT", args = "doubleValue=9.0"))
    private double getActualAttackRange1(double original, @Local(ordinal = 0) Entity entity) {
		double d = Math.sqrt(original);
		d -= 0.5;
		d *= d;
		if (entity instanceof Player player)
			return MethodHandler.getSquaredCurrentAttackReach(player,0.0F);
		else if (this.minecraft.player != null)
            return MethodHandler.getSquaredCurrentAttackReach(minecraft.player,0.0F);
        return d;
    }
}
