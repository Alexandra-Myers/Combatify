package net.atlas.combatify;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.atlas.atlaslib.util.ArrayListExtensions;
import net.atlas.atlaslib.util.PrefixLogger;
import net.atlas.combatify.config.CombatifyBetaConfig;
import net.atlas.combatify.config.ItemConfig;
import net.atlas.combatify.enchantment.DefendingEnchantment;
import net.atlas.combatify.extensions.ExtendedTier;
import net.atlas.combatify.item.CombatifyItemTags;
import net.atlas.combatify.item.ItemRegistry;
import net.atlas.combatify.item.TieredShieldItem;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.networking.NetworkingHandler;
import net.atlas.combatify.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.DispenserBlock;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.world.item.Items.NETHERITE_SWORD;

public class Combatify implements ModInitializer {
	public static final String MOD_ID = "combatify";
	public static CombatifyBetaConfig CONFIG = new CombatifyBetaConfig();
	public static ItemConfig ITEMS;
	public static ResourceLocation modDetectionNetworkChannel = id("networking");
	public NetworkingHandler networkingHandler;
	public static final List<TieredShieldItem> shields = new ArrayListExtensions<>();
	public static final List<UUID> unmoddedPlayers = new ArrayListExtensions<>();
	public static final List<UUID> moddedPlayers = new ArrayListExtensions<>();
	public static final Map<UUID, Boolean> isPlayerAttacking = new HashMap<>();
	public static final Map<String, WeaponType> defaultWeaponTypes = new HashMap<>();
	public static final Map<String, BlockingType> defaultTypes = new HashMap<>();
	public static final BiMap<String, Tier> defaultTiers = HashBiMap.create();
	public static Map<String, WeaponType> registeredWeaponTypes = new HashMap<>();
	public static Map<String, BlockingType> registeredTypes = new HashMap<>();
	public static BiMap<String, Tier> tiers = HashBiMap.create();
	public static final PrefixLogger LOGGER = new PrefixLogger(LogManager.getLogger("Combatify"));
	public static final BlockingType SWORD = defineDefaultBlockingType(new SwordBlockingType("sword").setToolBlocker(true).setDisablement(false).setCrouchable(false).setBlockHit(true).setRequireFullCharge(false).setSwordBlocking(true).setDelay(false));
	public static final BlockingType SHIELD = defineDefaultBlockingType(new ShieldBlockingType("shield"));
	public static final BlockingType CURRENT_SHIELD = defineDefaultBlockingType(new CurrentShieldBlockingType("current_shield"));
	public static final BlockingType NEW_SHIELD = defineDefaultBlockingType(new NewShieldBlockingType("new_shield").setKbMechanics(false));
	public static final BlockingType EMPTY = new EmptyBlockingType("empty").setDisablement(false).setCrouchable(false).setRequireFullCharge(false).setKbMechanics(false);

	@Override
	public void onInitialize() {
		WeaponType.init();
		networkingHandler = new NetworkingHandler();
		LOGGER.info("Init started.");
		CombatifyItemTags.init();
		if (CONFIG.dispensableTridents())
 			DispenserBlock.registerProjectileBehavior(Items.TRIDENT);
		if (CONFIG.configOnlyWeapons()) {
			ItemRegistry.registerWeapons();
			Event<ItemGroupEvents.ModifyEntries> event = ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT);
			event.register(entries -> entries.addAfter(NETHERITE_SWORD, ItemRegistry.WOODEN_KNIFE, ItemRegistry.STONE_KNIFE, ItemRegistry.IRON_KNIFE, ItemRegistry.GOLD_KNIFE, ItemRegistry.DIAMOND_KNIFE, ItemRegistry.NETHERITE_KNIFE, ItemRegistry.WOODEN_LONGSWORD, ItemRegistry.STONE_LONGSWORD, ItemRegistry.IRON_LONGSWORD, ItemRegistry.GOLD_LONGSWORD, ItemRegistry.DIAMOND_LONGSWORD, ItemRegistry.NETHERITE_LONGSWORD));
		}
		if (CONFIG.tieredShields()) {
			TieredShieldItem.init();
			Event<ItemGroupEvents.ModifyEntries> event = ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT);
			event.register(entries -> entries.addAfter(Items.SHIELD, TieredShieldItem.WOODEN_SHIELD, TieredShieldItem.IRON_SHIELD, TieredShieldItem.GOLD_SHIELD, TieredShieldItem.DIAMOND_SHIELD, TieredShieldItem.NETHERITE_SHIELD));
		}
		if (CONFIG.defender())
			DefendingEnchantment.registerEnchants();
		if (Combatify.CONFIG.percentageDamageEffects()) {
			MobEffects.DAMAGE_BOOST.value().addAttributeModifier(Attributes.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
			MobEffects.WEAKNESS.value().addAttributeModifier(Attributes.ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		}
	}
	public static void registerWeaponType(WeaponType weaponType) {
		Combatify.registeredWeaponTypes.put(weaponType.name, weaponType);
    }
	public static <T extends BlockingType> T registerBlockingType(T blockingType) {
		Combatify.registeredTypes.put(blockingType.getName(), blockingType);
		return blockingType;
	}
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
	public static void defineDefaultWeaponType(WeaponType type) {
		defaultWeaponTypes.put(type.name, type);
	}
	public static <T extends BlockingType> T defineDefaultBlockingType(T blockingType) {
		defaultTypes.put(blockingType.getName(), blockingType);
		return registerBlockingType(blockingType);
	}
	public static void defineDefaultTier(String name, ExtendedTier tier) {
		defaultTiers.put(name, tier);
		tiers.put(name, tier);
	}
}
