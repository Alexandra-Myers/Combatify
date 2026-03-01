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

public record CombatifyCritImpl(boolean allowsSprint, float minimumBaseCharge, float minimumFullCharge, float critMult, float chargedCritMult) implements CritImpl {
	public static final Identifier ID = Combatify.id("charged_crits");
	public static final MapCodec<CombatifyCritImpl> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(Codec.BOOL.optionalFieldOf("allows_sprint", true).forGetter(CombatifyCritImpl::allowsSprint),
				ExtraCodecs.floatRange(-1, 2).optionalFieldOf("minimum_base_charge", 0.9F).forGetter(CombatifyCritImpl::minimumBaseCharge),
				ExtraCodecs.floatRange(-1, 2).optionalFieldOf("minimum_full_charge", 1.95F).forGetter(CombatifyCritImpl::minimumFullCharge),
				ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("crit_multiplier", 1.25F).forGetter(CombatifyCritImpl::critMult),
				ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("charged_crit_multiplier", 1.5F).forGetter(CombatifyCritImpl::chargedCritMult))
			.apply(instance, CombatifyCritImpl::new));

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
			&& attacker.getAttackStrengthScale(0.5F) > this.minimumBaseCharge;
		if (!this.allowsSprint) isCrit &= !attacker.isSprinting();
		var isChargedCrit = attacker.getAttackStrengthScale(0.5F) > this.minimumFullCharge;
		if (isCrit) damageRef.set(damageRef.get() * (isChargedCrit ? this.chargedCritMult : this.critMult));
		return isCrit;
	}

	@Override
	public MapCodec<? extends CritImpl> type() {
		return CODEC;
	}
}
