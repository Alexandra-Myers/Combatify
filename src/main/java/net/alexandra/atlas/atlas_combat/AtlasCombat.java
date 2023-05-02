package net.alexandra.atlas.atlas_combat;

import eu.midnightdust.lib.config.MidnightConfig;
import net.alexandra.atlas.atlas_combat.config.AtlasConfig;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.alexandra.atlas.atlas_combat.networking.NetworkingHandler;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Position;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.List;

public class AtlasCombat implements ModInitializer {
	public static Player player;
	public static final String MOD_ID = "atlas_combat";
	public static ResourceLocation modDetectionNetworkChannel = id("networking");

	@Override
	public void onInitialize() {
		new NetworkingHandler();
		MidnightConfig.init("atlas-combat", AtlasConfig.class);

		DispenserBlock.registerBehavior(Items.TRIDENT, new AbstractProjectileDispenseBehavior() {
			@Override
			protected Projectile getProjectile(Level world, Position position, ItemStack stack) {
				ThrownTrident trident = new ThrownTrident(EntityType.TRIDENT, world);
				trident.tridentItem = stack.copy();
				trident.setPosRaw(position.x(), position.y(), position.z());
				trident.pickup = AbstractArrow.Pickup.ALLOWED;
				return trident;
			}
		});
		List<Item> items = Registry.ITEM.stream().toList();

		for(Item item : items) {
			if(item == Items.SNOWBALL || item == Items.EGG) {
				((ItemExtensions) item).setStackSize(64);
			} else if(item == Items.POTION) {
				((ItemExtensions) item).setStackSize(16);
			}
		}
	}
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
