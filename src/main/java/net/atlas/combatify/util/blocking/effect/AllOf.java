package net.atlas.combatify.util.blocking.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public record AllOf(List<PostBlockEffect> effects) implements PostBlockEffect {
	public static final ResourceLocation ID = ResourceLocation.withDefaultNamespace("all_of");
	public static final MapCodec<AllOf> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(PostBlockEffects.MAP_CODEC.codec().listOf().fieldOf("conditions").forGetter(AllOf::effects)).apply(instance, AllOf::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, AllOf> STREAM_CODEC = StreamCodec.composite(PostBlockEffect.STREAM_CODEC.apply(ByteBufCodecs.list()), AllOf::effects, AllOf::new);
	@Override
	public void doEffect(ItemStack blockingItem, LivingEntity target, LivingEntity attacker, DamageSource damageSource) {
		effects.forEach(effect -> effect.doEffect(blockingItem, target, attacker, damageSource));
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
		map.put(ID, STREAM_CODEC.map(allOf -> allOf, blockingCondition -> (AllOf) blockingCondition));
	}
}
