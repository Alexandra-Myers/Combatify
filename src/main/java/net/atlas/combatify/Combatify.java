package net.atlas.combatify;

import net.atlas.combatify.config.CombatifyConfig;
import net.atlas.combatify.config.ItemConfig;
import net.atlas.combatify.enchantment.DefendingEnchantment;
import net.atlas.combatify.enchantment.PiercingEnchantment;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.item.ItemRegistry;
import net.atlas.combatify.item.TieredShieldItem;
import net.atlas.combatify.networking.NetworkingHandler;
import net.atlas.combatify.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.minecraft.world.item.Items.NETHERITE_SWORD;

public class Combatify implements ModInitializer {
	public static final String MOD_ID = "combatify";
	public static final CombatifyConfig CONFIG = CombatifyConfig.createAndLoad();
	public static ItemConfig ITEMS;
	public static ResourceLocation modDetectionNetworkChannel = id("networking");
	public NetworkingHandler networkingHandler;
	public static final List<TieredShieldItem> shields = new ArrayListExtensions<>();
	public static final List<UUID> unmoddedPlayers = new ArrayListExtensions<>();
	public static final List<UUID> moddedPlayers = new ArrayListExtensions<>();
	public static final Map<UUID, Boolean> isPlayerAttacking = new HashMap<>();
	public static final Map<UUID, Boolean> finalizingAttack = new HashMap<>();
	public static final Map<UUID, Timer> scheduleHitResult = new HashMap<>();
	public static Map<String, BlockingType> registeredTypes = new HashMap<>();
	public static final PrefixLogger LOGGER = new PrefixLogger(LogManager.getLogger("Combatify"));
	public static final BlockingType SWORD = registerBlockingType(new SwordBlockingType("sword").setToolBlocker(true).setDisablement(false).setCrouchable(false).setBlockHit(true).setRequireFullCharge(false).setPercentage(true).setSwordBlocking(true));
	public static final BlockingType SHIELD = registerBlockingType(new ShieldBlockingType("shield"));
	public static final BlockingType NEW_SHIELD = registerBlockingType(new NewShieldBlockingType("new_shield").setKbMechanics(false).setPercentage(true));
	public static final BlockingType EMPTY = new EmptyBlockingType("empty").setDisablement(false).setCrouchable(false).setRequireFullCharge(false).setKbMechanics(false);

	@Override
	public void onInitialize() {
		networkingHandler = new NetworkingHandler();
		LOGGER.info("Init started.");
		DispenserBlock.registerBehavior(Items.TRIDENT, new AbstractProjectileDispenseBehavior() {
			@Override
			protected @NotNull Projectile getProjectile(Level world, Position position, ItemStack stack) {
				ThrownTrident trident = new ThrownTrident(EntityType.TRIDENT, world);
				trident.tridentItem = stack.copy();
				trident.setPosRaw(position.x(), position.y(), position.z());
				trident.pickup = AbstractArrow.Pickup.ALLOWED;
				return trident;
			}
		});
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
		if(CONFIG.piercer()) {
			PiercingEnchantment.registerEnchants();
		}
		if(CONFIG.defender()) {
			DefendingEnchantment.registerEnchants();
		}
		List<Item> items = BuiltInRegistries.ITEM.stream().toList();

		for(Item item : items) {
			((ItemExtensions) item).modifyAttributeModifiers();
		}
		MobEffects.DAMAGE_BOOST.addAttributeModifier(Attributes.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
		MobEffects.WEAKNESS.addAttributeModifier(Attributes.ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}
	public static <T extends BlockingType> T registerBlockingType(T blockingType) {
		Combatify.registeredTypes.put(blockingType.getName(), blockingType);
		return blockingType;
	}
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
