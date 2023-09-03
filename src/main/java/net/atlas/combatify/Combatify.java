package net.atlas.combatify;

import net.atlas.combatify.config.CombatifyConfig;
import net.atlas.combatify.config.ItemConfig;
import net.atlas.combatify.enchantment.DefendingEnchantment;
import net.atlas.combatify.enchantment.PiercingEnchantment;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.item.ItemRegistry;
import net.atlas.combatify.networking.NetworkingHandler;
import net.atlas.combatify.util.ArrayListExtensions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.minecraft.world.item.Items.NETHERITE_SWORD;

public class Combatify implements ModInitializer {
	public static Player player;
	public static final String MOD_ID = "combatify";
	public static final CombatifyConfig CONFIG = CombatifyConfig.createAndLoad();
	public static ItemConfig ITEMS;
	public static ResourceLocation modDetectionNetworkChannel = id("networking");
	public NetworkingHandler networkingHandler;
	public static final List<UUID> unmoddedPlayers = new ArrayListExtensions<>();
	public static Map<UUID, Boolean> isPlayerAttacking = new HashMap<>();
	public static Map<UUID, Boolean> finalizingAttack = new HashMap<>();
	public static Map<UUID, Timer> scheduleHitResult = new HashMap<>();

	@Override
	public void onInitialize() {
		networkingHandler = new NetworkingHandler();
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
		if(CONFIG.piercer()) {
			PiercingEnchantment.registerEnchants();
		}
		if(CONFIG.defender()) {
			DefendingEnchantment.registerEnchants();
		}
		List<Item> items = BuiltInRegistries.ITEM.stream().toList();

		for(Item item : items) {
//			if(item == Items.SNOWBALL || item == Items.EGG) {
//				((ItemExtensions) item).setStackSize(64);
//			} else if(item == Items.POTION) {
//				((ItemExtensions) item).setStackSize(16);
//			}
			((ItemExtensions) item).modifyAttributeModifiers();
		}
	}
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
