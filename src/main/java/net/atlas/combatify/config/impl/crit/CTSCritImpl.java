package net.atlas.combatify.config.impl.crit;

import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.Combatify;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record CTSCritImpl(boolean allowsSprint, float minimumCharge, float critMult) implements CritImpl {
	public static final Identifier ID = Identifier.withDefaultNamespace("combat_test_8c");
	public static final MapCodec<CTSCritImpl> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(Codec.BOOL.optionalFieldOf("allows_sprint", true).forGetter(CTSCritImpl::allowsSprint),
				ExtraCodecs.floatRange(-1, 2).optionalFieldOf("minimum_charge", -1F).forGetter(CTSCritImpl::minimumCharge),
				ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("crit_multiplier", 1.5F).forGetter(CTSCritImpl::critMult))
			.apply(instance, CTSCritImpl::new));

	@Override
	public boolean overrideCrit() {
		return Combatify.CONFIG.chargedAttacks();
	}

	@Override
	public boolean runCrit(Player attacker, Entity target, LocalFloatRef damageRef) {
		var isCrit = attacker.fallDistance > 0
			&& !attacker.onGround()
			&& !attacker.onClimbable()
			&& !attacker.isInWater()
			&& !attacker.hasEffect(MobEffects.BLINDNESS)
			&& !attacker.isPassenger()
			&& target instanceof LivingEntity
			&& attacker.getAttackStrengthScale(0.5F) > this.minimumCharge;
		if (!this.allowsSprint) isCrit &= !attacker.isSprinting();
		if (isCrit) damageRef.set(damageRef.get() * this.critMult);
		return isCrit;
	}

	@Override
	public MapCodec<? extends CritImpl> type() {
		return CODEC;
	}
}
