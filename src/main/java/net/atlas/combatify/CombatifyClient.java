package net.atlas.combatify;

import com.mojang.serialization.Codec;
import net.atlas.combatify.config.ShieldIndicatorStatus;
import net.atlas.combatify.extensions.IOptions;
import net.atlas.combatify.networking.ClientNetworkingHandler;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Arrays;
import java.util.Objects;

import static net.atlas.combatify.Combatify.CONFIG;
import static net.minecraft.client.Options.genericValueLabel;

@Mod.EventBusSubscriber(modid = Combatify.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CombatifyClient {
	public static final ModelLayerLocation WOODEN_SHIELD_MODEL_LAYER = new ModelLayerLocation(new ResourceLocation("combatify", "wooden_shield"),"main");
	public static final ModelLayerLocation IRON_SHIELD_MODEL_LAYER = new ModelLayerLocation(new ResourceLocation("combatify", "iron_shield"),"main");
	public static final ModelLayerLocation GOLDEN_SHIELD_MODEL_LAYER = new ModelLayerLocation(new ResourceLocation("combatify", "golden_shield"),"main");
	public static final ModelLayerLocation DIAMOND_SHIELD_MODEL_LAYER = new ModelLayerLocation(new ResourceLocation("combatify", "diamond_shield"),"main");
	public static final ModelLayerLocation NETHERITE_SHIELD_MODEL_LAYER = new ModelLayerLocation(new ResourceLocation("combatify", "netherite_shield"),"main");
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
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		Combatify.LOGGER.info("Client init started.");
		ClientNetworkingHandler.init();
		if (CONFIG.tieredShields.get()) {
			EntityModelLayerRegistry.registerModelLayer(WOODEN_SHIELD_MODEL_LAYER, ShieldModel::createLayer);
			EntityModelLayerRegistry.registerModelLayer(IRON_SHIELD_MODEL_LAYER, ShieldModel::createLayer);
			EntityModelLayerRegistry.registerModelLayer(GOLDEN_SHIELD_MODEL_LAYER, ShieldModel::createLayer);
			EntityModelLayerRegistry.registerModelLayer(DIAMOND_SHIELD_MODEL_LAYER, ShieldModel::createLayer);
			EntityModelLayerRegistry.registerModelLayer(NETHERITE_SHIELD_MODEL_LAYER, ShieldModel::createLayer);
		}
	}
}
