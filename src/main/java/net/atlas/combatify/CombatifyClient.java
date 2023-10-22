package net.atlas.combatify;

import com.mojang.serialization.Codec;
import eu.midnightdust.lib.config.MidnightConfig;
import net.atlas.combatify.config.ShieldIndicatorStatus;
import net.atlas.combatify.extensions.IOptions;
import net.atlas.combatify.networking.ClientPacketInfo;
import net.atlas.combatify.util.ArrayListExtensions;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.NetworkDirection;

import java.util.Arrays;
import java.util.Objects;

import static net.atlas.combatify.Combatify.*;
import static net.atlas.combatify.item.ItemRegistry.*;
import static net.atlas.combatify.item.TieredShieldItem.*;
import static net.minecraft.client.Options.genericValueLabel;
import static net.minecraft.world.item.Items.NETHERITE_SWORD;
import static net.minecraft.world.item.Items.SHIELD;

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
	public static void modelLayerLocationInit(EntityRenderersEvent.RegisterLayerDefinitions event) {
		if (CONFIG.tieredShields.get()) {
			event.registerLayerDefinition(WOODEN_SHIELD_MODEL_LAYER, ShieldModel::createLayer);
			event.registerLayerDefinition(IRON_SHIELD_MODEL_LAYER, ShieldModel::createLayer);
			event.registerLayerDefinition(GOLDEN_SHIELD_MODEL_LAYER, ShieldModel::createLayer);
			event.registerLayerDefinition(DIAMOND_SHIELD_MODEL_LAYER, ShieldModel::createLayer);
			event.registerLayerDefinition(NETHERITE_SHIELD_MODEL_LAYER, ShieldModel::createLayer);
		}
	}
	@SubscribeEvent
	public static void clientInit(FMLClientSetupEvent event) {
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> MidnightConfig.getScreen(parent, "combatify")));
	}
	@SubscribeEvent
	public static void onCreativeTabBuild(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.COMBAT && CONFIG.configOnlyWeapons.get()) {
			ArrayListExtensions<ItemLike> arrayListExtensions = new ArrayListExtensions<>();
			arrayListExtensions.addAll(NETHERITE_SWORD, WOODEN_KNIFE.get(), STONE_KNIFE.get(), IRON_KNIFE.get(), GOLD_KNIFE.get(), DIAMOND_KNIFE.get(), NETHERITE_KNIFE.get(), WOODEN_LONGSWORD.get(), STONE_LONGSWORD.get(), IRON_LONGSWORD.get(), GOLD_LONGSWORD.get(), DIAMOND_LONGSWORD.get(), NETHERITE_LONGSWORD.get());
			for (int i = 1; i < arrayListExtensions.size(); i++) {
				event.getEntries().putAfter(new ItemStack(arrayListExtensions.get(i - 1)), new ItemStack(arrayListExtensions.get(i)), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			}
		}
		if (event.getTabKey() == CreativeModeTabs.COMBAT && CONFIG.tieredShields.get()) {
			ArrayListExtensions<ItemLike> arrayListExtensions = new ArrayListExtensions<>();
			arrayListExtensions.addAll(SHIELD, WOODEN_SHIELD.get(), IRON_SHIELD.get(), GOLD_SHIELD.get(), DIAMOND_SHIELD.get(), NETHERITE_SHIELD.get());
			for (int i = 1; i < arrayListExtensions.size(); i++) {
				event.getEntries().putAfter(new ItemStack(arrayListExtensions.get(i - 1)), new ItemStack(arrayListExtensions.get(i)), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			}
		}
	}
}
