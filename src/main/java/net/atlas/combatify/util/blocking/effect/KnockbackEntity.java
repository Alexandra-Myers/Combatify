package net.atlas.combatify.util.blocking.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record KnockbackEntity(LevelBasedValue strength, boolean force) implements PostBlockEffect {
	public static final ResourceLocation ID = ResourceLocation.withDefaultNamespace("knockback_entity");
	public KnockbackEntity() {
		this(LevelBasedValue.constant(0.5F), false);
	}
	public static final MapCodec<KnockbackEntity> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(LevelBasedValue.CODEC.optionalFieldOf("strength", LevelBasedValue.constant(0.5F)).forGetter(KnockbackEntity::strength),
				Codec.BOOL.optionalFieldOf("force", false).forGetter(KnockbackEntity::force))
			.apply(instance, KnockbackEntity::new));
	@Override
	public void doEffect(ServerLevel serverLevel, EnchantedItemInUse enchantedItemInUse, LivingEntity attacker, DamageSource damageSource, int enchantmentLevel, LivingEntity toApply, Vec3 position) {
        assert enchantedItemInUse.owner() != null;
		Vec3 targetPosition = toApply.equals(attacker) ? enchantedItemInUse.owner().position() : position;
		Vec3 attackerPosition = toApply.equals(attacker) ? position : enchantedItemInUse.owner().position();
        double x = targetPosition.x() - attackerPosition.x();
		double z = targetPosition.z() - attackerPosition.z();
		enchantedItemInUse.owner().blockUsingShield(attacker);
		if (force) toApply.hurtMarked = true;
		MethodHandler.knockback(toApply, strength.calculate(enchantmentLevel), x, z);
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
