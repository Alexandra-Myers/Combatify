package net.atlas.combatify.mixin.compatibility.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.annotation.mixin.ModSpecific;
import net.atlas.combatify.config.DualAttackIndicatorStatus;
import net.atlas.combatify.config.ShieldIndicatorStatus;
import net.caffeinemc.mods.sodium.client.gui.SodiumGameOptionPages;
import net.caffeinemc.mods.sodium.client.gui.options.Option;
import net.caffeinemc.mods.sodium.client.gui.options.OptionGroup;
import net.caffeinemc.mods.sodium.client.gui.options.OptionImpl;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatter;
import net.caffeinemc.mods.sodium.client.gui.options.control.CyclingControl;
import net.caffeinemc.mods.sodium.client.gui.options.control.SliderControl;
import net.caffeinemc.mods.sodium.client.gui.options.storage.MinecraftOptionsStorage;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@ModSpecific("sodium")
@Mixin(SodiumGameOptionPages.class)
public class SodiumGameOptionPagesMixin {
	@Shadow(remap = false)
	@Final
	private static MinecraftOptionsStorage vanillaOpts;

	@WrapOperation(method = "general", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/gui/options/OptionGroup$Builder;add(Lnet/caffeinemc/mods/sodium/client/gui/options/Option;)Lnet/caffeinemc/mods/sodium/client/gui/options/OptionGroup$Builder;", ordinal = 9), remap = false)
	private static OptionGroup.Builder appendMore(OptionGroup.Builder instance, Option<?> option, Operation<OptionGroup.Builder> original) {
		return original.call(instance, option)
			.add(OptionImpl.createBuilder(DualAttackIndicatorStatus.class, vanillaOpts)
				.setName(Component.translatable("options.dualAttackIndicator"))
				.setTooltip(Component.translatable("sodium.options.dual_attack_indicator.tooltip"))
				.setControl(opts -> new CyclingControl<>(opts, DualAttackIndicatorStatus.class, DualAttackIndicatorStatus.AS_COMPONENTS))
				.setBinding((opts, value) -> CombatifyClient.dualAttackIndicator.set(value), (opts) -> CombatifyClient.dualAttackIndicator.get())
				.build())
			.add(OptionImpl.createBuilder(ShieldIndicatorStatus.class, vanillaOpts)
				.setName(Component.translatable("options.shieldIndicator"))
				.setTooltip(Component.translatable("sodium.options.shield_indicator.tooltip"))
				.setControl(opts -> new CyclingControl<>(opts, ShieldIndicatorStatus.class, ShieldIndicatorStatus.AS_COMPONENTS))
				.setBinding((opts, value) -> CombatifyClient.shieldIndicator.set(value), (opts) -> CombatifyClient.shieldIndicator.get())
				.build())
			.add(OptionImpl.createBuilder(AttackIndicatorStatus.class, vanillaOpts)
				.setName(Component.translatable("options.projectileChargeIndicator"))
				.setTooltip(Component.translatable("sodium.options.projectile_charge_indicator.tooltip"))
				.setControl(opts -> new CyclingControl<>(opts, AttackIndicatorStatus.class, ShieldIndicatorStatus.AS_COMPONENTS))
				.setBinding((opts, value) -> CombatifyClient.projectileChargeIndicator.set(value), (opts) -> CombatifyClient.projectileChargeIndicator.get())
				.build())
			.add(OptionImpl.createBuilder(int.class, vanillaOpts)
				.setName(Component.translatable("options.attackIndicatorMaxValue"))
				.setTooltip(Component.translatable("sodium.options.attack_indicator_max_value.tooltip"))
				.setControl(opts -> new SliderControl(opts, 1, 200, 1, ControlValueFormatter.percentage()))
				.setBinding((opts, value) -> {
					double newValue = value / 100.0;
					CombatifyClient.attackIndicatorMaxValue.set(newValue);
				}, (opts) -> (int) (CombatifyClient.attackIndicatorMaxValue.get() * 100))
				.build())
			.add(OptionImpl.createBuilder(int.class, vanillaOpts)
				.setName(Component.translatable("options.attackIndicatorMinValue"))
				.setTooltip(Component.translatable("sodium.options.attack_indicator_min_value.tooltip"))
				.setControl(opts -> new SliderControl(opts, 0, 200, 1, ControlValueFormatter.percentage()))
				.setBinding((opts, value) -> {
					double newValue = value / 100.0;
					CombatifyClient.attackIndicatorMinValue.set(newValue);
				}, (opts) -> (int) (CombatifyClient.attackIndicatorMinValue.get() * 100))
				.build())
			.add(OptionImpl.createBuilder(TriState.class, vanillaOpts)
				.setName(Component.translatable("options.rhythmicAttack"))
				.setTooltip(Component.translatable("sodium.options.rhythmic_attacks.tooltip"))
				.setControl(opts -> new CyclingControl<>(opts, TriState.class, new Component[] { CommonComponents.OPTION_OFF, Component.translatable("options.context_decided"), CommonComponents.OPTION_ON }))
				.setBinding((opts, value) -> CombatifyClient.rhythmicAttacks.set(value), (opts) -> CombatifyClient.rhythmicAttacks.get())
				.build())
			.add(OptionImpl.createBuilder(TriState.class, vanillaOpts)
				.setName(Component.translatable("options.augmentedArmHeight"))
				.setTooltip(Component.translatable("sodium.options.augmented_arm_height.tooltip"))
				.setControl(opts -> new CyclingControl<>(opts, TriState.class, new Component[] { CommonComponents.OPTION_OFF, Component.translatable("options.context_decided"), CommonComponents.OPTION_ON }))
				.setBinding((opts, value) -> CombatifyClient.augmentedArmHeight.set(value), (opts) -> CombatifyClient.augmentedArmHeight.get())
				.build());
	}
}
