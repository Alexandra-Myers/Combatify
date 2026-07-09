package net.atlas.combatify.util.blocking.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
//? <26.2 {
/*import net.atlas.combatify.config.KnockbackMode;
*///?}
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record KnockbackEntity(LevelBasedValue strength,
							  //? >=26.2 {
							  LevelBasedValue imaginedDamage,
							  //?}
							  boolean force,
							  boolean inverseDirection) implements PostBlockEffect {
	public static final Identifier ID = Identifier.withDefaultNamespace("knockback_entity");
	//? >=26.2 {
	public KnockbackEntity(LevelBasedValue strength, boolean force, boolean inverseDirection) {
		this(strength, LevelBasedValue.constant(0.5F), force, inverseDirection);
	}
	//?}
	public KnockbackEntity() {
		this(LevelBasedValue.constant(0.5F), false, false);
	}
	public static final MapCodec<KnockbackEntity> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(LevelBasedValue.CODEC.optionalFieldOf("strength", LevelBasedValue.constant(0.5F)).forGetter(KnockbackEntity::strength),
				//? >=26.2 {
				LevelBasedValue.CODEC.optionalFieldOf("imagined_damage", LevelBasedValue.constant(0.5F)).forGetter(KnockbackEntity::imaginedDamage),
				//?}
				Codec.BOOL.optionalFieldOf("force", false).forGetter(KnockbackEntity::force),
				Codec.BOOL.optionalFieldOf("inverse_direction", false).forGetter(KnockbackEntity::inverseDirection))
			.apply(instance, KnockbackEntity::new));
	@Override
	public void doEffect(ServerLevel serverLevel, EnchantedItemInUse enchantedItemInUse, LivingEntity attacker, DamageSource damageSource, int enchantmentLevel, LivingEntity toApply, Vec3 position) {
        assert enchantedItemInUse.owner() != null;
		Vec3 targetPosition = getPosition(enchantedItemInUse, attacker, toApply, position, inverseDirection);
		Vec3 attackerPosition = getPosition(enchantedItemInUse, attacker, toApply, position, !inverseDirection);
        double x = targetPosition.x() - attackerPosition.x();
		double z = targetPosition.z() - attackerPosition.z();
		if (force) toApply.hurtMarked = true;
		//? <26.2 {
		/*KnockbackMode.extractContext(toApply, damageSource);
		toApply.knockback(this.strength().calculate(enchantmentLevel), x, z);
		*///?} >=26.2 {
		toApply.knockback(this.strength().calculate(enchantmentLevel), x, z, damageSource, this.imaginedDamage().calculate(enchantmentLevel));
		//?}
	}

	private Vec3 getPosition(EnchantedItemInUse enchantedItemInUse, LivingEntity attacker, LivingEntity toApply, Vec3 position, boolean inverse) {
		if (!inverse) return toApply.equals(attacker) ? enchantedItemInUse.owner().position() : position;
		else return toApply.equals(attacker) ? position : enchantedItemInUse.owner().position();
	}

	@Override
	public MapCodec<? extends PostBlockEffect> type() {
		return MAP_CODEC;
	}

	@Override
	public Identifier id() {
		return ID;
	}
}
