package net.alexandra.atlas.atlas_combat;

import net.alexandra.atlas.atlas_combat.enchantment.CleavingEnchantment;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AtlasCombat implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Atlas Combat");

	public static final CleavingEnchantment CLEAVING_ENCHANTMENT = register();
	public static CleavingEnchantment register() {
		return Registry.register(Registry.ENCHANTMENT, new ResourceLocation("atlas_combat","cleaving"),new CleavingEnchantment());
	}

	@Override
	public void onInitialize(ModContainer mod) {

		((ItemExtensions) Items.SNOWBALL).setStackSize(64);
		((ItemExtensions) Items.EGG).setStackSize(16);
	}
}
