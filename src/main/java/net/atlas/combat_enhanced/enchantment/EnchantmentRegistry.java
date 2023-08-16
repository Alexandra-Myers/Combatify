package net.atlas.combat_enhanced.enchantment;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentRegistry {
	public static Enchantment registerEnchant(ResourceLocation resourceLocation, Enchantment item) {
		return Registry.register(BuiltInRegistries.ENCHANTMENT, resourceLocation, item);
	}
}
