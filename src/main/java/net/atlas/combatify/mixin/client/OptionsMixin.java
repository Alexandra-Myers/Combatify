package net.atlas.combatify.mixin.client;

import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.networking.PacketRegistration;
import net.atlas.combatify.networking.ServerboundClientInformationExtensionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
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
			ServerboundClientInformationExtensionPacket payload = new ServerboundClientInformationExtensionPacket(CombatifyClient.shieldCrouch.get());
			PacketRegistration.MAIN.sendToServer(payload);
		}
	}

	@Inject(method = "processOptions", at = @At(value = "HEAD"))
	public void injectOptions(Options.FieldAccess visitor, CallbackInfo ci) {
		visitor.process("autoAttack", CombatifyClient.autoAttack);
		visitor.process("shieldCrouch", CombatifyClient.shieldCrouch);
		visitor.process("rhythmicAttacks", CombatifyClient.rhythmicAttacks);
		visitor.process("protIndicator", CombatifyClient.protectionIndicator);
		visitor.process("attackIndicatorMaxValue", CombatifyClient.attackIndicatorMaxValue);
		visitor.process("attackIndicatorMinValue", CombatifyClient.attackIndicatorMinValue);
		visitor.process("shieldIndicator", CombatifyClient.shieldIndicator);
	}
}
