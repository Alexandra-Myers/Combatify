package net.atlas.combatify.util.blocking.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record EnchantmentEffect(EnchantmentEntityEffect enchantmentEntityEffect) implements PostBlockEffect {
	public static final Identifier ID = Identifier.withDefaultNamespace("enchantment_effect");
	public static final MapCodec<EnchantmentEffect> MAP_CODEC = EnchantmentEntityEffect.CODEC.fieldOf("effect")
		.xmap(EnchantmentEffect::new, EnchantmentEffect::enchantmentEntityEffect);

	@Override
	public void doEffect(ServerLevel serverLevel, EnchantedItemInUse enchantedItemInUse, LivingEntity attacker, DamageSource damageSource, int enchantmentLevel, LivingEntity toApply, Vec3 position) {
		enchantmentEntityEffect.apply(serverLevel, enchantmentLevel, enchantedItemInUse, toApply, position);
	}

	@Override
	public MapCodec<? extends PostBlockEffect> type() {
		return MAP_CODEC;
	}

	@Override
	public Identifier id() {
		return ID;
	}
}
