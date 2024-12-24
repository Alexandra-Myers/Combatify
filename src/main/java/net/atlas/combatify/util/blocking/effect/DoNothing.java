package net.atlas.combatify.util.blocking.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public record DoNothing() implements PostBlockEffect {
	public static final ResourceLocation ID = ResourceLocation.withDefaultNamespace("nothing");
	public static final MapCodec<DoNothing> MAP_CODEC = MapCodec.unit(new DoNothing());
	public static final StreamCodec<RegistryFriendlyByteBuf, PostBlockEffect> STREAM_CODEC = StreamCodec.unit(new DoNothing());
	@Override
	public void doEffect(ItemStack blockingItem, LivingEntity target, LivingEntity attacker, DamageSource damageSource) {

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
