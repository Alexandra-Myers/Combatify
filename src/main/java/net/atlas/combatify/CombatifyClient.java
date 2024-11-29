package net.atlas.combatify;

import com.mojang.serialization.Codec;
import net.atlas.combatify.config.ShieldIndicatorStatus;
import net.atlas.combatify.networking.ClientNetworkingHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Objects;

import static net.minecraft.client.Options.genericValueLabel;
import static net.minecraft.client.Options.percentValueLabel;

public class CombatifyClient implements ClientModInitializer {
	public static final ModelLayerLocation WOODEN_SHIELD_MODEL_LAYER = new ModelLayerLocation(Combatify.id("wooden_shield"),"main");
	public static final ModelLayerLocation IRON_SHIELD_MODEL_LAYER = new ModelLayerLocation(Combatify.id("iron_shield"),"main");
	public static final ModelLayerLocation GOLDEN_SHIELD_MODEL_LAYER = new ModelLayerLocation(Combatify.id("golden_shield"),"main");
	public static final ModelLayerLocation DIAMOND_SHIELD_MODEL_LAYER = new ModelLayerLocation(Combatify.id("diamond_shield"),"main");
	public static final ModelLayerLocation NETHERITE_SHIELD_MODEL_LAYER = new ModelLayerLocation(Combatify.id("netherite_shield"),"main");
	public static final OptionInstance<Boolean> autoAttack = OptionInstance.createBoolean("options.autoAttack", true);
	public static final OptionInstance<Boolean> shieldCrouch = OptionInstance.createBoolean("options.shieldCrouch", true);
	public static final OptionInstance<Boolean> rhythmicAttacks = OptionInstance.createBoolean("options.rhythmicAttack",true);
	public static final OptionInstance<Boolean> augmentedArmHeight = OptionInstance.createBoolean("options.augmentedArmHeight",true);
	public static final OptionInstance<Double> attackIndicatorMaxValue = new OptionInstance<>(
		"options.attackIndicatorMaxValue",
		OptionInstance.cachedConstantTooltip(Component.translatable("options.attackIndicatorMaxValue.tooltip")),
		(optionText, value) -> value == 2.0 ? Objects.requireNonNull(genericValueLabel(optionText, Component.translatable("options.attackIndicatorMaxValue.default"))) : percentValueLabel(optionText, value),
		new OptionInstance.IntRange(1, 200).xmap(sliderValue -> (double)sliderValue / 100.0, value -> (int)(value * 100.0)),
		Codec.doubleRange(0.1, 2.0),
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
		if (Combatify.CONFIG.tieredShields()) {
			EntityModelLayerRegistry.registerModelLayer(WOODEN_SHIELD_MODEL_LAYER, ShieldModel::createLayer);
			EntityModelLayerRegistry.registerModelLayer(IRON_SHIELD_MODEL_LAYER, ShieldModel::createLayer);
			EntityModelLayerRegistry.registerModelLayer(GOLDEN_SHIELD_MODEL_LAYER, ShieldModel::createLayer);
			EntityModelLayerRegistry.registerModelLayer(DIAMOND_SHIELD_MODEL_LAYER, ShieldModel::createLayer);
			EntityModelLayerRegistry.registerModelLayer(NETHERITE_SHIELD_MODEL_LAYER, ShieldModel::createLayer);
		}
	}
}
