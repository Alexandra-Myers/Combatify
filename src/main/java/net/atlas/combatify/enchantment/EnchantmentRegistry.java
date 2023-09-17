package net.atlas.combatify.enchantment;

import net.atlas.combatify.Combatify;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EnchantmentRegistry {
	private static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Combatify.MOD_ID);
	public static RegistryObject<Enchantment> registerEnchant(ResourceLocation resourceLocation, Enchantment item) {
		return ENCHANTMENTS.register(resourceLocation.getPath(), () -> item);
	}
	public static void registerAllEnchants(IEventBus bus) {
		ENCHANTMENTS.register(bus);
	}
}
