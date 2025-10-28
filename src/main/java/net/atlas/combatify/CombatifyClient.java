package net.atlas.combatify;

import com.mojang.serialization.Codec;
import eu.midnightdust.lib.config.MidnightConfig;
import net.atlas.combatify.config.ShieldIndicatorStatus;
import net.atlas.combatify.util.ArrayListExtensions;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

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
	public static final OptionInstance<Boolean> autoAttack = OptionInstance.createBoolean("options.autoAttack", true);
	public static final OptionInstance<Boolean> shieldCrouch = OptionInstance.createBoolean("options.shieldCrouch", true);
	public static final OptionInstance<Boolean> rhythmicAttacks = OptionInstance.createBoolean("options.rhythmicAttack",true);
	public static final OptionInstance<Boolean> protectionIndicator = OptionInstance.createBoolean("options.protIndicator",false);
	public static final OptionInstance<Boolean> fishingRodLegacy = OptionInstance.createBoolean("options.fishingRodLegacy",false);
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
	private static Component percentValueLabel(Component arg, double d) {
		return Component.translatable("options.percent_value", arg, (int)(d * (double)100.0F));
	}
	@SubscribeEvent
	public static void clientInit(FMLClientSetupEvent event) {
		//noinspection removal
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> MidnightConfig.getScreen(parent, "combatify")));
		Combatify.markState(combatifyState::get);
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
			arrayListExtensions.addAll(SHIELD, IRON_SHIELD.get(), GOLD_SHIELD.get(), DIAMOND_SHIELD.get(), NETHERITE_SHIELD.get());
			for (int i = 1; i < arrayListExtensions.size(); i++) {
				event.getEntries().putAfter(new ItemStack(arrayListExtensions.get(i - 1)), new ItemStack(arrayListExtensions.get(i)), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			}
		}
	}
}
