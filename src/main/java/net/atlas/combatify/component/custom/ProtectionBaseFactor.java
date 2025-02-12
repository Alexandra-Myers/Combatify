package net.atlas.combatify.component.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;

public record ProtectionBaseFactor(EnchantmentValueEffect base, EnchantmentValueEffect factor) {
	public static Codec<ProtectionBaseFactor> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(EnchantmentValueEffect.CODEC.fieldOf("base").forGetter(ProtectionBaseFactor::base),
			EnchantmentValueEffect.CODEC.fieldOf("factor").forGetter(ProtectionBaseFactor::factor))
		.apply(instance, ProtectionBaseFactor::new));
}
