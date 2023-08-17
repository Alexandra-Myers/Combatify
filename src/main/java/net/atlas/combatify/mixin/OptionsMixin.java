package net.atlas.combatify.mixin;

import com.mojang.serialization.Codec;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.config.ShieldIndicatorStatus;
import net.atlas.combatify.extensions.IOptions;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(Options.class)
public abstract class OptionsMixin implements IOptions {

	@Shadow
	public static Component genericValueLabel(Component optionText, Component value) {
		return null;
	}

	@Inject(method = "processOptions", at = @At(value = "HEAD"))
	public void injectOptions(Options.FieldAccess visitor, CallbackInfo ci) {
		visitor.process("autoAttack", CombatifyClient.autoAttack);
		visitor.process("shieldCrouch", CombatifyClient.shieldCrouch);
		visitor.process("rhythmicAttacks", CombatifyClient.rhythmicAttacks);
		visitor.process("protIndicator", CombatifyClient.protectionIndicator);
		visitor.process("fishingRodLegacy", CombatifyClient.fishingRodLegacy);
		visitor.process("attackIndicatorValue", attackIndicatorValue);
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
		return attackIndicatorValue;
	}
	private final OptionInstance<Double> attackIndicatorValue = new OptionInstance<>(
			"options.attackIndicatorValue",
			OptionInstance.cachedConstantTooltip(Component.translatable("options.attackIndicatorValue.tooltip")),
			(optionText, value) -> value == 2.0 ? Objects.requireNonNull(genericValueLabel(optionText, Component.translatable("options.attackIndicatorValue.default"))) : IOptions.doubleValueLabel(optionText, value),
			new OptionInstance.IntRange(1, 20).xmap(sliderValue -> (double)sliderValue / 10.0, value -> (int)(value * 10.0)),
			Codec.doubleRange(0.1, 2.0),
			2.0,
			value -> {

			}
	);
}
