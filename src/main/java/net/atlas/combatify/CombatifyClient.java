package net.atlas.combatify;

import com.mojang.serialization.Codec;
import net.atlas.combatify.config.DualAttackIndicatorStatus;
import net.atlas.combatify.config.ShieldIndicatorStatus;
import net.atlas.combatify.networking.ClientNetworkingHandler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.TriState;

import java.util.Arrays;
import java.util.Objects;

import static net.minecraft.client.Options.genericValueLabel;
import static net.minecraft.client.Options.percentValueLabel;

public class CombatifyClient implements ClientModInitializer {
	public static final OptionInstance<Boolean> autoAttack = OptionInstance.createBoolean("options.autoAttack", true);
	public static final OptionInstance<Boolean> shieldCrouch = OptionInstance.createBoolean("options.shieldCrouch", true);
	public static final OptionInstance<TriState> rhythmicAttacks = new OptionInstance<>(
		"options.rhythmicAttack",
		OptionInstance.noTooltip(),
		(component, object) -> switch (object) {
			case TRUE -> CommonComponents.OPTION_ON;
			case FALSE -> CommonComponents.OPTION_OFF;
			case DEFAULT -> Component.translatable("options.context_decided");
		},
		new OptionInstance.Enum<>(Arrays.asList(TriState.values()), Codec.INT.xmap(ordinal -> switch (Mth.positiveModulo(ordinal, 3)) {
			case 0 -> TriState.FALSE;
			case 1 -> TriState.TRUE;
			default -> TriState.DEFAULT;
		}, TriState::ordinal)),
		TriState.DEFAULT,
		value -> {

		}
	);
	public static final OptionInstance<TriState> augmentedArmHeight = new OptionInstance<>(
		"options.augmentedArmHeight",
		OptionInstance.noTooltip(),
		(component, object) -> switch (object) {
			case TRUE -> CommonComponents.OPTION_ON;
			case FALSE -> CommonComponents.OPTION_OFF;
			case DEFAULT -> Component.translatable("options.context_decided");
		},
		new OptionInstance.Enum<>(Arrays.asList(TriState.values()), Codec.INT.xmap(ordinal -> switch (Mth.positiveModulo(ordinal, 3)) {
			case 0 -> TriState.FALSE;
			case 1 -> TriState.TRUE;
			default -> TriState.DEFAULT;
		}, TriState::ordinal)),
		TriState.DEFAULT,
		value -> {

		}
	);
	public static final OptionInstance<Combatify.CombatifyState> combatifyState = new OptionInstance<>(
		"options.combatifyState",
		OptionInstance.noTooltip(),
		(component, state) -> state.getComponent(),
		new OptionInstance.Enum<>(Arrays.asList(Combatify.CombatifyState.values()), Codec.INT.xmap(ordinal -> switch (Mth.positiveModulo(ordinal, 3)) {
			case 0 -> Combatify.CombatifyState.VANILLA;
			case 1 -> Combatify.CombatifyState.CTS_8C;
			default -> Combatify.CombatifyState.COMBATIFY;
		}, Combatify.CombatifyState::ordinal)),
		Combatify.CombatifyState.COMBATIFY,
		value -> {}
	);
	public static final OptionInstance<AttackIndicatorStatus> projectileChargeIndicator = new OptionInstance<>(
		"options.projectileChargeIndicator",
		OptionInstance.noTooltip(),
		OptionInstance.forOptionEnum(),
		new OptionInstance.Enum<>(Arrays.asList(AttackIndicatorStatus.values()), Codec.INT.xmap(AttackIndicatorStatus::byId, AttackIndicatorStatus::getId)),
		AttackIndicatorStatus.OFF,
		value -> {
		}
	);
	public static final OptionInstance<DualAttackIndicatorStatus> dualAttackIndicator = new OptionInstance<>(
		"options.dualAttackIndicator",
		OptionInstance.noTooltip(),
		OptionInstance.forOptionEnum(),
		new OptionInstance.Enum<>(Arrays.asList(DualAttackIndicatorStatus.values()), Codec.INT.xmap(DualAttackIndicatorStatus::byId, DualAttackIndicatorStatus::getId)),
		DualAttackIndicatorStatus.OFF,
		value -> {
		}
	);
	public static final OptionInstance<Double> attackIndicatorMaxValue = new OptionInstance<>(
		"options.attackIndicatorMaxValue",
		OptionInstance.cachedConstantTooltip(Component.translatable("options.attackIndicatorMaxValue.tooltip")),
		(optionText, value) -> value == 2.0 ? Objects.requireNonNull(genericValueLabel(optionText, Component.translatable("options.attackIndicatorMaxValue.default"))) : percentValueLabel(optionText, value),
		new OptionInstance.IntRange(1, 200).xmap(sliderValue -> (double)sliderValue / 100.0, value -> (int)(value * 100.0)),
		Codec.doubleRange(0.01, 2.0),
		2.0,
		value -> {

		}
	);
	public static final OptionInstance<Double> attackIndicatorMinValue = new OptionInstance<>(
		"options.attackIndicatorMinValue",
		OptionInstance.cachedConstantTooltip(Component.translatable("options.attackIndicatorMinValue.tooltip")),
		(optionText, value) -> value == 1.3 ? Objects.requireNonNull(genericValueLabel(optionText, Component.translatable("options.attackIndicatorMinValue.default"))) : percentValueLabel(optionText, value),
		new OptionInstance.IntRange(0, 200).xmap(sliderValue -> (double)sliderValue / 100.0, value -> (int)(value * 100.0)),
		Codec.doubleRange(0.0, 2.0),
		1.3,
		value -> {

		}
	);
	public static final OptionInstance<ShieldIndicatorStatus> shieldIndicator = new OptionInstance<>(
			"options.shieldIndicator",
			OptionInstance.noTooltip(),
			OptionInstance.forOptionEnum(),
			new OptionInstance.Enum<>(Arrays.asList(ShieldIndicatorStatus.values()), Codec.INT.xmap(ShieldIndicatorStatus::byId, ShieldIndicatorStatus::getId)),
			ShieldIndicatorStatus.OFF,
			value -> {
			}
	);

	@Override
	public void onInitializeClient() {
		Combatify.LOGGER.info("Client init started.");
		ClientNetworkingHandler.init();
		Combatify.markState(combatifyState::get);
	}
}
