package net.atlas.combatify.mixin;

import net.atlas.combatify.enchantment.CleavingEnchantment;
import net.atlas.combatify.extensions.IEnchantments;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Enchantments.class)
public abstract class EnchantmentsMixin implements IEnchantments {
	@Shadow
	private static Enchantment register(String name, Enchantment enchantment) {
		return null;
	}

	@Unique
	private static final Enchantment CLEAVING_ENCHANTMENT = register("cleaving", new CleavingEnchantment());

	@Override
	public Enchantment getCleavingEnchantment() {
		return CLEAVING_ENCHANTMENT;
	}
}
