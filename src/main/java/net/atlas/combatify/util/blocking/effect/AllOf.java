package net.atlas.combatify.util.blocking.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record AllOf(List<PostBlockEffect> effects) implements PostBlockEffect {
	public static final ResourceLocation ID = ResourceLocation.withDefaultNamespace("all_of");
	public static final MapCodec<AllOf> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(PostBlockEffects.MAP_CODEC.codec().listOf().fieldOf("effects").forGetter(AllOf::effects)).apply(instance, AllOf::new));
	@Override
	public void doEffect(ServerLevel serverLevel, EnchantedItemInUse enchantedItemInUse, LivingEntity attacker, DamageSource damageSource, int enchantmentLevel, LivingEntity toApply, Vec3 position) {
		effects.forEach(effect -> effect.doEffect(serverLevel, enchantedItemInUse, attacker, damageSource, enchantmentLevel, toApply, position));
	}

	@Override
	public MapCodec<? extends PostBlockEffect> type() {
		return MAP_CODEC;
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
