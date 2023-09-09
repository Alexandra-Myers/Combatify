package net.atlas.combatify;

import com.mojang.serialization.Codec;
import net.atlas.combatify.config.ShieldIndicatorStatus;
import net.atlas.combatify.extensions.IOptions;
import net.atlas.combatify.networking.ClientNetworkingHandler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Objects;

import static net.minecraft.client.Options.genericValueLabel;

public class CombatifyClient implements ClientModInitializer {
	public static final OptionInstance<Boolean> autoAttack = OptionInstance.createBoolean("options.autoAttack", true);
	public static final OptionInstance<Boolean> shieldCrouch = OptionInstance.createBoolean("options.shieldCrouch", true);
	public static final OptionInstance<Boolean> rhythmicAttacks = OptionInstance.createBoolean("options.rhythmicAttack",true);
	public static final OptionInstance<Boolean> protectionIndicator = OptionInstance.createBoolean("options.protIndicator",false);
	public static final OptionInstance<Boolean> fishingRodLegacy = OptionInstance.createBoolean("options.fishingRodLegacy",false);
	public static final OptionInstance<Double> attackIndicatorValue = new OptionInstance<>(
		"options.attackIndicatorValue",
		OptionInstance.cachedConstantTooltip(Component.translatable("options.attackIndicatorValue.tooltip")),
		(optionText, value) -> value == 2.0 ? Objects.requireNonNull(genericValueLabel(optionText, Component.translatable("options.attackIndicatorValue.default"))) : IOptions.doubleValueLabel(optionText, value),
		new OptionInstance.IntRange(1, 20).xmap(sliderValue -> (double)sliderValue / 10.0, value -> (int)(value * 10.0)),
		Codec.doubleRange(0.1, 2.0),
		2.0,
		value -> {

		}
	);
	public static final OptionInstance<ShieldIndicatorStatus> shieldIndicator = new OptionInstance<>(
			"options.shieldIndicator",
			OptionInstance.noTooltip(),
			OptionInstance.forOptionEnum(),
			new OptionInstance.Enum<>(Arrays.asList(ShieldIndicatorStatus.values()), Codec.INT.xmap(ShieldIndicatorStatus::byId, ShieldIndicatorStatus::getId)),
			ShieldIndicatorStatus.CROSSHAIR,
			value -> {
			}
	);
	public ClientNetworkingHandler clientNetworkingHandler;

	@Override
	public void onInitializeClient() {
		clientNetworkingHandler = new ClientNetworkingHandler();
	}
}
