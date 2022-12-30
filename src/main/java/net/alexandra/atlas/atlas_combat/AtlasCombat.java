package net.alexandra.atlas.atlas_combat;

import eu.midnightdust.lib.config.MidnightConfig;
import net.alexandra.atlas.atlas_combat.config.AtlasConfig;
import net.alexandra.atlas.atlas_combat.networking.NetworkingHandler;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class AtlasCombat implements ModInitializer {
	public static Player player;
	public static final AtlasConfig CONFIG = AtlasConfig.createAndLoad();

	@Override
	public void onInitialize() {
		new NetworkingHandler();

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
	}
}
