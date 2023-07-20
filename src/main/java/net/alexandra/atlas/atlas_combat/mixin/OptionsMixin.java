package net.alexandra.atlas.atlas_combat.mixin;

import com.mojang.serialization.Codec;
import net.alexandra.atlas.atlas_combat.AtlasClient;
import net.alexandra.atlas.atlas_combat.config.ShieldIndicatorStatus;
import net.alexandra.atlas.atlas_combat.extensions.IOptions;
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
		visitor.process("autoAttack", AtlasClient.autoAttack);
		visitor.process("shieldCrouch", AtlasClient.shieldCrouch);
		visitor.process("rhythmicAttacks", AtlasClient.rhythmicAttacks);
		visitor.process("protIndicator", AtlasClient.protectionIndicator);
		visitor.process("fishingRodLegacy", AtlasClient.fishingRodLegacy);
		visitor.process("attackIndicatorValue", attackIndicatorValue);
		visitor.process("shieldIndicator", AtlasClient.shieldIndicator);
	}

	@Override
	public OptionInstance<Boolean> autoAttack() {
		return AtlasClient.autoAttack;
	}
	@Override
	public OptionInstance<Boolean> shieldCrouch() {
		return AtlasClient.shieldCrouch;
	}
	@Override
	public OptionInstance<Boolean> rhythmicAttacks() {
		return AtlasClient.rhythmicAttacks;
	}
	@Override
	public OptionInstance<Boolean> protIndicator() {
		return AtlasClient.protectionIndicator;
	}
	@Override
	public OptionInstance<Boolean> fishingRodLegacy() {
		return AtlasClient.fishingRodLegacy;
	}
	@Override
	public OptionInstance<ShieldIndicatorStatus> shieldIndicator() {
		return AtlasClient.shieldIndicator;
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
