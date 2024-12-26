package net.atlas.combatify.util.blocking.effect;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record HurtEntity(Holder<DamageType> damageType, LevelBasedValue minDamage, LevelBasedValue maxDamage) implements PostBlockEffect {
	public static final ResourceLocation ID = ResourceLocation.withDefaultNamespace("hurt_entity");
	public HurtEntity(Holder<DamageType> damageType, LevelBasedValue damage) {
		this(damageType, damage, damage);
	}
	public static final MapCodec<HurtEntity> PARTIAL_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(DamageType.CODEC.fieldOf("damage_type").forGetter(HurtEntity::damageType),
				PostBlockEffects.LEVEL_BASED_VALUE_OR_CONSTANT_CODEC.fieldOf("duration").forGetter(HurtEntity::maxDamage))
			.apply(instance, HurtEntity::new)
	);
	public static final MapCodec<HurtEntity> FULL_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(DamageType.CODEC.fieldOf("damage_type").forGetter(HurtEntity::damageType),
				PostBlockEffects.LEVEL_BASED_VALUE_OR_CONSTANT_CODEC.fieldOf("min_damage").forGetter(HurtEntity::minDamage),
				PostBlockEffects.LEVEL_BASED_VALUE_OR_CONSTANT_CODEC.fieldOf("max_damage").forGetter(HurtEntity::maxDamage))
			.apply(instance, HurtEntity::new)
	);
	public static final MapCodec<HurtEntity> MAP_CODEC = Codec.mapEither(FULL_CODEC, PARTIAL_CODEC).xmap(
		Either::unwrap,
		Either::left
	);

	@Override
	public void doEffect(ServerLevel serverLevel, EnchantedItemInUse enchantedItemInUse, LivingEntity attacker, DamageSource damageSource, int enchantmentLevel, LivingEntity toApply, Vec3 position) {
        assert enchantedItemInUse.owner() != null;
		float minDamage = this.minDamage.calculate(enchantmentLevel);
		float maxDamage = this.maxDamage.calculate(enchantmentLevel);
        float damage = minDamage == maxDamage ? minDamage : Mth.randomBetween(enchantedItemInUse.owner().getRandom(), minDamage, maxDamage);
		if (!damageSource.combatify$originatedFromBlockedAttack()) toApply.hurtServer(serverLevel, new DamageSource(damageType, enchantedItemInUse.owner()).combatify$originatesFromBlockedAttack(true), damage);
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
