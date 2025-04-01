package net.atlas.combatify.mixin;

import net.atlas.combatify.extensions.BlockHitResultExtensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockHitResult.class)
public class BlockHitResultMixin implements BlockHitResultExtensions {
	@Unique
	public boolean isLedgeEdge;

	@Inject(method = "<init>(ZLnet/minecraft/world/phys/Vec3;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;Z)V", at = @At("TAIL"))
	public void injectLedgeEdgeInit(boolean bl, Vec3 vec3, Direction direction, BlockPos blockPos, boolean bl2, CallbackInfo ci) {
		isLedgeEdge = false;
	}

	@Override
	public void combatify$setIsLedgeEdge() {
		isLedgeEdge = true;
	}

	@Override
	public boolean combatify$isLedgeEdge() {
		return isLedgeEdge;
	}
}
