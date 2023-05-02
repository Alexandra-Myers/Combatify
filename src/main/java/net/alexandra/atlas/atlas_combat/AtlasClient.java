package net.alexandra.atlas.atlas_combat;

import com.mojang.serialization.Codec;
import net.alexandra.atlas.atlas_combat.config.ShieldIndicatorStatus;
import net.alexandra.atlas.atlas_combat.extensions.IOptions;
import net.alexandra.atlas.atlas_combat.networking.ClientNetworkingHandler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.CycleOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ProgressOption;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.Arrays;

public class AtlasClient implements ClientModInitializer {
	private static final Component ACCESSIBILITY_TOOLTIP_LOW_SHIELD = new TranslatableComponent("options.lowShield.tooltip");
	public static Boolean autoAttack = true;
	public static Boolean shieldCrouch = true;
	public static Boolean lowShield = false;
	public static Boolean rhythmicAttacks = true;
	public static Boolean protectionIndicator = false;
	public static Boolean fishingRodLegacy = false;
	public static Double attackIndicatorValue = 2.0;
	public static ShieldIndicatorStatus shieldIndicator = ShieldIndicatorStatus.CROSSHAIR;
	public static final CycleOption<Boolean> autoAttackOption = CycleOption.createOnOff("options.autoAttack", (options) -> autoAttack, (options, option, boolean_) -> autoAttack = boolean_);
	public static final CycleOption<Boolean> shieldCrouchOption = CycleOption.createOnOff("options.shieldCrouch", (options) -> shieldCrouch, (options, option, boolean_) -> shieldCrouch = boolean_);
	public static final CycleOption<Boolean> lowShieldOption = CycleOption.createOnOff("options.lowShield", ACCESSIBILITY_TOOLTIP_LOW_SHIELD, (options) -> lowShield, (options, option, boolean_) -> lowShield = boolean_);
	public static final CycleOption<Boolean> rhythmicAttacksOption = CycleOption.createOnOff("options.rhythmicAttacks", (options) -> rhythmicAttacks, (options, option, boolean_) -> rhythmicAttacks = boolean_);
	public static final CycleOption<Boolean> protectionIndicatorOption = CycleOption.createOnOff("options.protectionIndicator", (options) -> protectionIndicator, (options, option, boolean_) -> protectionIndicator = boolean_);
	public static final CycleOption<Boolean> fishingRodLegacyOption = CycleOption.createOnOff("options.fishingRodLegacy", (options) -> fishingRodLegacy, (options, option, boolean_) -> fishingRodLegacy = boolean_);
	public static final ProgressOption attackIndicatorValueOption = new ProgressOption("options.attackIndicatorValue",
			0.1, 2.0, 0.0F, (options) -> attackIndicatorValue, (options, double_) -> attackIndicatorValue = double_, (options, progressOption) -> {
		double d = progressOption.get(options);
		return d == 2.0 ? new TranslatableComponent("options.attackIndicatorValue.default") : IOptions.doubleValueLabel(new TranslatableComponent("options.attackIndicatorValue"),d);
	}, (minecraft) -> minecraft.font.split(new TranslatableComponent("options.attackIndicatorValue.tooltip"), 200));
	public static final CycleOption<ShieldIndicatorStatus> shieldIndicatorOption = CycleOption.create("options.shieldIndicator", ShieldIndicatorStatus.values(),
			(shieldIndicatorStatus) -> new TranslatableComponent(shieldIndicatorStatus.getKey()),
			(options) -> AtlasClient.shieldIndicator,
			(options, option, shieldIndicatorStatus) -> AtlasClient.shieldIndicator = shieldIndicatorStatus);

	@Override
	public void onInitializeClient() {
		// Commented out until in use
		//ClientNetworkingHandler networkingHandler = new ClientNetworkingHandler();
	}
}
