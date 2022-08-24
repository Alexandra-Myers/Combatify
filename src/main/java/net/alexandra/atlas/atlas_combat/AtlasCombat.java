package net.alexandra.atlas.atlas_combat;

import net.alexandra.atlas.atlas_combat.config.ConfigHelper;
import net.alexandra.atlas.atlas_combat.config.QuiltConfigs;
import net.alexandra.atlas.atlas_combat.enchantment.CleavingEnchantment;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.alexandra.atlas.atlas_combat.networking.NetworkingHandler;
import net.minecraft.core.Position;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.config.QuiltConfig;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AtlasCombat implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Atlas Combat");
	public static final QuiltConfigs QUILT_CONFIGS = QuiltConfig.create(
			"atlas_combat",   // The family id, this should usually just be your mod ID
			"config",           // The id for this particular config, since your mod might have multiple
			QuiltConfigs.class      // The config class you created earlier
	);

	public static ConfigHelper helper = new ConfigHelper();

	public static final CleavingEnchantment CLEAVING_ENCHANTMENT = register();
	public static CleavingEnchantment register() {
		return Registry.register(Registry.ENCHANTMENT, new ResourceLocation("atlas_combat","cleaving"),new CleavingEnchantment());
	}

	@Override
	public void onInitialize(ModContainer mod) {

		NetworkingHandler networkingHandler = new NetworkingHandler();

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
				int newStackSize = helper.getInt(helper.itemsJsonObject,Registry.ITEM.getKey(item).getPath());

			if(item.maxStackSize == newStackSize) continue;

			((ItemExtensions)item).setStackSize(newStackSize);
		}

	}
}
