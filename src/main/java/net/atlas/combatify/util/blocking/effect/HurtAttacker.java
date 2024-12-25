package net.atlas.combatify.util.blocking.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public record HurtAttacker(Holder<DamageType> damageType, float amount) implements PostBlockEffect {
	public static final ResourceLocation ID = ResourceLocation.withDefaultNamespace("hurt_attacker");

	public static final MapCodec<HurtAttacker> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(DamageType.CODEC.fieldOf("damage_type").forGetter(HurtAttacker::damageType),
				Codec.FLOAT.fieldOf("damage").forGetter(HurtAttacker::amount))
			.apply(instance, HurtAttacker::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, HurtAttacker> STREAM_CODEC = StreamCodec.composite(DamageType.STREAM_CODEC, HurtAttacker::damageType,
		ByteBufCodecs.FLOAT, HurtAttacker::amount,
		HurtAttacker::new);

	@Override
	public void doEffect(ServerLevel serverLevel, ItemStack blockingItem, LivingEntity target, LivingEntity attacker, DamageSource damageSource) {
		attacker.hurtServer(serverLevel, new DamageSource(damageType, target), amount);
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
		map.put(ID, STREAM_CODEC.map(hurtAttacker -> hurtAttacker, postBlockEffect -> (HurtAttacker) postBlockEffect));
	}
}
