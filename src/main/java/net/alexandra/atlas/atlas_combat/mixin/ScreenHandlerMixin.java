package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerMenu.class)
abstract class ScreenHandlerMixin {
    @Inject(
        method = "stillValid(Lnet/minecraft/world/inventory/ContainerLevelAccess;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/Block;)Z",
        at = @At(value = "HEAD"), cancellable = true)
    private static void getActualReachDistance(ContainerLevelAccess context, Player player, Block block, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(context.evaluate(
				(world, pos) -> world.getBlockState(pos).is(block) && player.distanceToSqr((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5) <= ((PlayerExtensions) player).getSquaredReach(player, 64.0),
				true
		));
		cir.cancel();
    }
}
