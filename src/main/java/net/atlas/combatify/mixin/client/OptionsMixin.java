package net.atlas.combatify.mixin.client;

import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.networking.NetworkingHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public abstract class OptionsMixin {
	@Shadow
	protected Minecraft minecraft;

	@Inject(method = "broadcastOptions", at = @At(value = "TAIL"))
	public void broadcastExtras(CallbackInfo ci) {
		if (this.minecraft.player != null) {
			CustomPacketPayload payload = new NetworkingHandler.ServerboundClientInformationExtensionPacket(CombatifyClient.shieldCrouch.get());
			this.minecraft.player.connection.send(ClientPlayNetworking.createC2SPacket(payload));
		}
	}

	@Inject(method = "processOptions", at = @At(value = "HEAD"))
	public void injectOptions(Options.FieldAccess visitor, CallbackInfo ci) {
		visitor.process("autoAttack", CombatifyClient.autoAttack);
		visitor.process("shieldCrouch", CombatifyClient.shieldCrouch);
		visitor.process("rhythmicAttacks", CombatifyClient.rhythmicAttacks);
		visitor.process("augmentedArmHeight", CombatifyClient.augmentedArmHeight);
		visitor.process("combatifyState", CombatifyClient.combatifyState);
		visitor.process("projectileChargeIndicator", CombatifyClient.projectileChargeIndicator);
		visitor.process("dualAttackIndicator", CombatifyClient.dualAttackIndicator);
		visitor.process("attackIndicatorMaxValue", CombatifyClient.attackIndicatorMaxValue);
		visitor.process("attackIndicatorMinValue", CombatifyClient.attackIndicatorMinValue);
		visitor.process("shieldIndicator", CombatifyClient.shieldIndicator);
	}
}
