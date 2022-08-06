package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.item.WeaponType;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.network.ServerGamePacketListenerImpl$1")
public abstract class PlayerEntityInteractionMixin implements ServerboundInteractPacket.Handler {
	@Shadow(aliases = "field_28963") @Final
	private ServerGamePacketListenerImpl field_28963;
	@Shadow(aliases = "field_28962") @Final private Entity field_28962;
	@Inject(method = "onAttack()V", at = @At("HEAD"), require = 1, allow = 1, cancellable = true)
	private void ensureWithinAttackRange(final CallbackInfo ci) {
		if ((!WeaponType.isWithinAttackRange(this.field_28963.player, this.field_28962))) {
			ci.cancel();
		}
	}
}
