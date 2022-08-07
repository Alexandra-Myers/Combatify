package net.alexandra.atlas.atlas_combat;

import net.alexandra.atlas.atlas_combat.enchantment.CleavingEnchantment;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AtlasCombat implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Atlas Combat");

	public static final CleavingEnchantment CLEAVING_ENCHANTMENT = new CleavingEnchantment();

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registry.ENCHANTMENT, new ResourceLocation("atlas_combat","cleaving"),CLEAVING_ENCHANTMENT);
	}
}
