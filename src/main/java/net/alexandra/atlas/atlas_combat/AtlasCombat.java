package net.alexandra.atlas.atlas_combat;

import net.alexandra.atlas.atlas_combat.config.AtlasConfig;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.alexandra.atlas.atlas_combat.item.ItemRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.alexandra.atlas.atlas_combat.item.ItemRegistry.*;
import static net.minecraft.world.item.Items.NETHERITE_SWORD;

public class AtlasCombat implements ModInitializer {
	public static Player player;
	public static final String MOD_ID = "atlas_combat";
	public static final AtlasConfig CONFIG = AtlasConfig.createAndLoad();

	@Override
	public void onInitialize() {
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
			event.register(entries -> entries.addAfter(NETHERITE_SWORD, WOODEN_KNIFE, STONE_KNIFE, IRON_KNIFE, GOLD_KNIFE, DIAMOND_KNIFE, NETHERITE_KNIFE, WOODEN_LONGSWORD, STONE_LONGSWORD, IRON_LONGSWORD, GOLD_LONGSWORD, DIAMOND_LONGSWORD, NETHERITE_LONGSWORD));
		}
		List<Item> items = BuiltInRegistries.ITEM.stream().toList();

		for(Item item : items) {
			if(item == Items.SNOWBALL || item == Items.EGG) {
				((ItemExtensions) item).setStackSize(64);
			} else if(item == Items.POTION) {
				((ItemExtensions) item).setStackSize(16);
			}
		}
	}
}
