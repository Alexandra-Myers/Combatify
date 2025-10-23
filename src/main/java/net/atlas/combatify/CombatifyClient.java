package net.atlas.combatify;

import com.mojang.serialization.Codec;
import net.atlas.combatify.config.ShieldIndicatorStatus;
import net.atlas.combatify.networking.ClientNetworkingHandler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Arrays;
import java.util.Objects;

import static net.minecraft.client.Options.genericValueLabel;
import static net.minecraft.client.Options.percentValueLabel;

public class CombatifyClient implements ClientModInitializer {
	public static final OptionInstance<Boolean> autoAttack = OptionInstance.createBoolean("options.autoAttack", true);
	public static final OptionInstance<Boolean> shieldCrouch = OptionInstance.createBoolean("options.shieldCrouch", true);
	public static final OptionInstance<Boolean> rhythmicAttacks = OptionInstance.createBoolean("options.rhythmicAttack",true);
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
	public static final OptionInstance<Combatify.CombatifyState> combatifyState = new OptionInstance<>(
		"options.combatifyState",
		OptionInstance.noTooltip(),
		(component, state) -> state.getComponent(),
		new OptionInstance.Enum<>(Arrays.asList(Combatify.CombatifyState.values()), Codec.INT.xmap(ordinal -> switch (Mth.positiveModulo(ordinal, 2)) {
			case 0 -> Combatify.CombatifyState.CTS_8C;
			default -> Combatify.CombatifyState.COMBATIFY; // TODO - Vanilla support for 1.20.1
		}, Combatify.CombatifyState::ordinal)),
		Combatify.CombatifyState.COMBATIFY,
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

	public static final Material IRON_SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/iron_shield_base"));
	public static final Material IRON_SHIELD_BASE_NO_PATTERN = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/iron_shield_base_nopattern"));
	public static final Material GOLDEN_SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/golden_shield_base"));
	public static final Material GOLDEN_SHIELD_BASE_NO_PATTERN = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/golden_shield_base_nopattern"));
	public static final Material DIAMOND_SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/diamond_shield_base"));
	public static final Material DIAMOND_SHIELD_BASE_NO_PATTERN = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/diamond_shield_base_nopattern"));
	public static final Material NETHERITE_SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/netherite_shield_base"));
	public static final Material NETHERITE_SHIELD_BASE_NO_PATTERN = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/netherite_shield_base_nopattern"));


	@Override
	public void onInitializeClient() {
		Combatify.LOGGER.info("Client init started.");
		ClientNetworkingHandler.init();
		Combatify.markState(combatifyState::get);
	}
}
