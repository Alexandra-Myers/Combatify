package net.atlas.combatify.compat;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.config.DualAttackIndicatorStatus;
import net.atlas.combatify.config.ShieldIndicatorStatus;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatterImpls;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;

public class CombatifySodiumConfig implements ConfigEntryPoint {
	private static final Identifier COMBATIFY_ICON = Identifier.fromNamespaceAndPath("combatify", "textures/gui/config_icon.png");
	private final Options vanillaOpts;
	private final StorageEventHandler vanillaStorage;

	public CombatifySodiumConfig() {
		Minecraft minecraft = Minecraft.getInstance();
		this.vanillaOpts = minecraft.options;
		this.vanillaStorage = () -> {
			this.vanillaOpts.save();
			SodiumClientMod.logger().info("Flushed changes to Minecraft configuration");
		};
	}
	@Override
	public void registerConfigLate(ConfigBuilder configBuilder) {
		buildFullConfig(configBuilder);
	}
	private void buildFullConfig(ConfigBuilder builder) {
		builder.registerOwnModOptions().setName("Combatify").setIcon(COMBATIFY_ICON)
			.setColorTheme(builder.createColorTheme().setFullThemeRGB(0xFF175672, 0xFF238692, 0xFF0F3258)).addPage(this.buildGeneralPage(builder));
	}

	private OptionPageBuilder buildGeneralPage(ConfigBuilder builder) {
		var generalPage = builder.createOptionPage().setName(Component.literal("Combatify General"));
		generalPage.addOptionGroup(builder.createOptionGroup()
			.addOption(builder.createEnumOption(Combatify.id("general.dual_attack_indicator"), DualAttackIndicatorStatus.class)
				.setStorageHandler(this.vanillaStorage)
				.setName(Component.translatable("options.dualAttackIndicator"))
				.setTooltip(Component.translatable("sodium.options.dual_attack_indicator.tooltip"))
				.setDefaultValue(DualAttackIndicatorStatus.OFF)
				.setElementNameProvider(DualAttackIndicatorStatus::caption)
				.setBinding(CombatifyClient.dualAttackIndicator::set, CombatifyClient.dualAttackIndicator::get))
			.addOption(builder.createEnumOption(Combatify.id("general.shield_indicator"), ShieldIndicatorStatus.class)
				.setStorageHandler(this.vanillaStorage)
				.setName(Component.translatable("options.shieldIndicator"))
				.setTooltip(Component.translatable("sodium.options.shield_indicator.tooltip"))
				.setDefaultValue(ShieldIndicatorStatus.OFF)
				.setElementNameProvider(ShieldIndicatorStatus::caption)
				.setBinding(CombatifyClient.shieldIndicator::set, CombatifyClient.shieldIndicator::get))
			.addOption(builder.createEnumOption(Combatify.id("general.projectile_charge_indicator"), AttackIndicatorStatus.class)
				.setStorageHandler(this.vanillaStorage)
				.setName(Component.translatable("options.projectileChargeIndicator"))
				.setTooltip(Component.translatable("sodium.options.projectile_charge_indicator.tooltip"))
				.setDefaultValue(AttackIndicatorStatus.OFF)
				.setElementNameProvider(AttackIndicatorStatus::caption)
				.setBinding(CombatifyClient.projectileChargeIndicator::set, CombatifyClient.projectileChargeIndicator::get))
			.addOption(builder.createEnumOption(Combatify.id("general.spear_charge_indicator"), AttackIndicatorStatus.class)
				.setStorageHandler(this.vanillaStorage)
				.setName(Component.translatable("options.spearChargeIndicator"))
				.setTooltip(Component.translatable("sodium.options.spear_charge_indicator.tooltip"))
				.setDefaultValue(AttackIndicatorStatus.OFF)
				.setElementNameProvider(AttackIndicatorStatus::caption)
				.setBinding(CombatifyClient.spearChargeIndicator::set, CombatifyClient.spearChargeIndicator::get))
			.addOption(builder.createIntegerOption(Combatify.id("general.attack_indicator_max_value"))
				.setStorageHandler(this.vanillaStorage)
				.setName(Component.translatable("options.attackIndicatorMaxValue"))
				.setTooltip(Component.translatable("sodium.options.attack_indicator_max_value.tooltip"))
				.setDefaultValue(200)
				.setValueFormatter(ControlValueFormatterImpls.percentage())
				.setRange(1, 200, 1)
				.setBinding((value) -> {
					double newValue = value / 100.0;
					CombatifyClient.attackIndicatorMaxValue.set(newValue);
				}, () -> (int) (CombatifyClient.attackIndicatorMaxValue.get() * 100)))
			.addOption(builder.createIntegerOption(Combatify.id("general.attack_indicator_min_value"))
				.setStorageHandler(this.vanillaStorage)
				.setName(Component.translatable("options.attackIndicatorMinValue"))
				.setTooltip(Component.translatable("sodium.options.attack_indicator_min_value.tooltip"))
				.setDefaultValue(130)
				.setValueFormatter(ControlValueFormatterImpls.percentage())
				.setRange(0, 200, 1)
				.setBinding((value) -> {
					double newValue = value / 100.0;
					CombatifyClient.attackIndicatorMinValue.set(newValue);
				}, () -> (int) (CombatifyClient.attackIndicatorMinValue.get() * 100)))
			.addOption(builder.createEnumOption(Combatify.id("general.rhythmic_attacks"), TriState.class)
				.setStorageHandler(this.vanillaStorage)
				.setName(Component.translatable("options.rhythmicAttack"))
				.setTooltip(Component.translatable("sodium.options.rhythmic_attacks.tooltip"))
				.setDefaultValue(TriState.DEFAULT)
				.setElementNameProvider(triState -> switch (triState) {
					case TRUE -> CommonComponents.OPTION_ON;
					case FALSE -> CommonComponents.OPTION_OFF;
					case DEFAULT -> Component.translatable("options.context_decided");
				})
				.setBinding(CombatifyClient.rhythmicAttacks::set, CombatifyClient.rhythmicAttacks::get))
			.addOption(builder.createEnumOption(Combatify.id("general.augmented_arm_height"), TriState.class)
				.setStorageHandler(this.vanillaStorage)
				.setName(Component.translatable("options.augmentedArmHeight"))
				.setTooltip(Component.translatable("sodium.options.augmented_arm_height.tooltip"))
				.setDefaultValue(TriState.DEFAULT)
				.setElementNameProvider(triState -> switch (triState) {
					case TRUE -> CommonComponents.OPTION_ON;
					case FALSE -> CommonComponents.OPTION_OFF;
					case DEFAULT -> Component.translatable("options.context_decided");
				})
				.setBinding(CombatifyClient.augmentedArmHeight::set, CombatifyClient.augmentedArmHeight::get)));
		return generalPage;
	}
}
