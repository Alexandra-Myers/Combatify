package net.atlas.combatify.util.blocking.effect;

import com.mojang.serialization.MapCodec;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public record KnockbackAttacker() implements PostBlockEffect {
	public static final ResourceLocation ID = ResourceLocation.withDefaultNamespace("knockback_attacker");
	public static final MapCodec<KnockbackAttacker> MAP_CODEC = MapCodec.unit(new KnockbackAttacker());
	public static final StreamCodec<RegistryFriendlyByteBuf, PostBlockEffect> STREAM_CODEC = StreamCodec.unit(new KnockbackAttacker());
	@Override
	public void doEffect(ServerLevel serverLevel, ItemStack blockingItem, LivingEntity target, LivingEntity attacker, DamageSource damageSource) {
		double x = target.getX() - attacker.getX();
		double z = target.getZ() - attacker.getZ();
		target.blockUsingShield(attacker);
		MethodHandler.knockback(attacker, 0.5, x, z);
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
		map.put(ID, STREAM_CODEC);
	}
}
