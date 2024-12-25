package net.atlas.combatify.util.blocking.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3;

public interface PostBlockEffect {
	StreamCodec<RegistryFriendlyByteBuf, PostBlockEffect> STREAM_CODEC = StreamCodec.of((buf, postBlockEffect) -> {
		buf.writeResourceLocation(postBlockEffect.id());
		PostBlockEffects.STREAM_CODEC_MAP.get(postBlockEffect.id()).encode(buf, postBlockEffect);
	}, buf -> PostBlockEffects.STREAM_CODEC_MAP.get(buf.readResourceLocation()).decode(buf));
	void doEffect(ServerLevel serverLevel, EnchantedItemInUse enchantedItemInUse, LivingEntity attacker, DamageSource damageSource, int enchantmentLevel, LivingEntity toApply, Vec3 position);

	MapCodec<? extends PostBlockEffect> type();

	ResourceLocation id();
}
