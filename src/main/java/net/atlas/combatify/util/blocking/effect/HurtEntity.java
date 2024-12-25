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
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public record HurtEntity(Holder<DamageType> damageType, float amount) implements PostBlockEffect {
	public static final ResourceLocation ID = ResourceLocation.withDefaultNamespace("hurt_entity");

	public static final MapCodec<HurtEntity> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(DamageType.CODEC.fieldOf("damage_type").forGetter(HurtEntity::damageType),
				Codec.FLOAT.fieldOf("damage").forGetter(HurtEntity::amount))
			.apply(instance, HurtEntity::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, HurtEntity> STREAM_CODEC = StreamCodec.composite(DamageType.STREAM_CODEC, HurtEntity::damageType,
		ByteBufCodecs.FLOAT, HurtEntity::amount,
		HurtEntity::new);

	@Override
	public void doEffect(ServerLevel serverLevel, EnchantedItemInUse enchantedItemInUse, LivingEntity attacker, DamageSource damageSource, int enchantmentLevel, LivingEntity toApply, Vec3 position) {
		if (!damageSource.combatify$originatedFromBlockedAttack()) toApply.hurtServer(serverLevel, new DamageSource(damageType, enchantedItemInUse.owner()).combatify$originatesFromBlockedAttack(true), amount);
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
		map.put(ID, STREAM_CODEC.map(hurtAttacker -> hurtAttacker, postBlockEffect -> (HurtEntity) postBlockEffect));
	}
}
