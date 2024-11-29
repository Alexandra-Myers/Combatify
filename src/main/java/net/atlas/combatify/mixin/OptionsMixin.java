package net.atlas.combatify.mixin;

import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.CookeyMod;
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

import java.io.IOException;

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
		visitor.process("attackIndicatorMaxValue", CombatifyClient.attackIndicatorMaxValue);
		visitor.process("attackIndicatorMinValue", CombatifyClient.attackIndicatorMinValue);
		visitor.process("shieldIndicator", CombatifyClient.shieldIndicator);
	}

	@Inject(method = "save", at = @At("TAIL"))
	public void saveModConfig(CallbackInfo ci) {
		try {
			CookeyMod.getConfig().saveConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
