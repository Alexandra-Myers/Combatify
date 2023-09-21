package net.atlas.combatify.enchantment;

import net.atlas.combatify.Combatify;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class EnchantmentRegistry {
	private static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Combatify.MOD_ID);
	public static RegistryObject<Enchantment> registerEnchant(ResourceLocation resourceLocation, Supplier<Enchantment> enchantment) {
		return ENCHANTMENTS.register(resourceLocation.getPath(), enchantment);
	}
	public static void registerAllEnchants(IEventBus bus) {
		ENCHANTMENTS.register(bus);
	}
}
