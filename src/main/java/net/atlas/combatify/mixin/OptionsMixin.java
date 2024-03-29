package net.atlas.combatify.mixin;

import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.config.ShieldIndicatorStatus;
import net.atlas.combatify.extensions.IOptions;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public abstract class OptionsMixin implements IOptions {

	@Inject(method = "processOptions", at = @At(value = "HEAD"))
	public void injectOptions(Options.FieldAccess visitor, CallbackInfo ci) {
		visitor.process("autoAttack", CombatifyClient.autoAttack);
		visitor.process("shieldCrouch", CombatifyClient.shieldCrouch);
		visitor.process("rhythmicAttacks", CombatifyClient.rhythmicAttacks);
		visitor.process("protIndicator", CombatifyClient.protectionIndicator);
		visitor.process("fishingRodLegacy", CombatifyClient.fishingRodLegacy);
		visitor.process("attackIndicatorValue", CombatifyClient.attackIndicatorValue);
		visitor.process("shieldIndicator", CombatifyClient.shieldIndicator);
	}

	@Override
	public OptionInstance<Boolean> autoAttack() {
		return CombatifyClient.autoAttack;
	}
	@Override
	public OptionInstance<Boolean> shieldCrouch() {
		return CombatifyClient.shieldCrouch;
	}
	@Override
	public OptionInstance<Boolean> rhythmicAttacks() {
		return CombatifyClient.rhythmicAttacks;
	}
	@Override
	public OptionInstance<Boolean> protIndicator() {
		return CombatifyClient.protectionIndicator;
	}
	@Override
	public OptionInstance<Boolean> fishingRodLegacy() {
		return CombatifyClient.fishingRodLegacy;
	}
	@Override
	public OptionInstance<ShieldIndicatorStatus> shieldIndicator() {
		return CombatifyClient.shieldIndicator;
	}
	@Override
	public OptionInstance<Double> attackIndicatorValue() {
		return CombatifyClient.attackIndicatorValue;
	}
}
