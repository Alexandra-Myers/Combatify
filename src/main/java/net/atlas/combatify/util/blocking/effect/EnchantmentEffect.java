package net.atlas.combatify.util.blocking.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public record EnchantmentEffect(EnchantmentEntityEffect enchantmentEntityEffect) implements PostBlockEffect {
	public static final ResourceLocation ID = ResourceLocation.withDefaultNamespace("enchantment_effect");
	public static final MapCodec<EnchantmentEffect> MAP_CODEC = EnchantmentEntityEffect.CODEC.fieldOf("effect")
		.xmap(EnchantmentEffect::new, EnchantmentEffect::enchantmentEntityEffect);
	public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentEffect> STREAM_CODEC = ByteBufCodecs.fromCodec(MAP_CODEC.codec()).mapStream(buf -> buf);

	@Override
	public void doEffect(ServerLevel serverLevel, EnchantedItemInUse enchantedItemInUse, LivingEntity attacker, DamageSource damageSource, int enchantmentLevel, LivingEntity toApply, Vec3 position) {
		enchantmentEntityEffect.apply(serverLevel, enchantmentLevel, enchantedItemInUse, toApply, position);
	}

	@Override
	public MapCodec<? extends PostBlockEffect> type() {
		return MAP_CODEC;
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

	public static void mapStreamCodec(Map<ResourceLocation, StreamCodec<RegistryFriendlyByteBuf, PostBlockEffect>> map) {
		map.put(ID, STREAM_CODEC.map(enchantmentEffect -> enchantmentEffect, postBlockEffect -> (EnchantmentEffect) postBlockEffect));
	}
}
