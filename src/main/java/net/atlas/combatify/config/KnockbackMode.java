package net.atlas.combatify.config;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.util.SeptaConsumer;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import org.jetbrains.annotations.Nullable;

public enum KnockbackMode {
	VANILLA,
	OLD,
	CTS_8C,
	CTS_5,
	MIDAIR;
	public void runKnockback(
		LivingEntity target,
		@Nullable DamageSource source,
		double strength,
		double x,
		double z
		//? <26.2 {
		/*, QuadConsumer<LivingEntity, Double, Double, Double> vanillaCall
		*///?} >=26.2 {
		, SeptaConsumer<LivingEntity, Double, Double, Double, DamageSource, Float, Boolean> vanillaCall,
		float damage,
		boolean fromEffect
		//?}
	) {
		boolean applyNonProjectileKB = false;
		if (this == MIDAIR && source == null) applyNonProjectileKB = true;
		else if (source != null) applyNonProjectileKB = (Combatify.CONFIG.fishingHookKB() && source.getDirectEntity() instanceof FishingHook);
		switch (this) {
			case MIDAIR -> {
				if (applyNonProjectileKB || !source.is(DamageTypeTags.IS_PROJECTILE)) MethodHandler.midairKnockback(target, strength, x, z);
				else MethodHandler.knockback(target, strength, x, z);
			}
			case CTS_8C -> {
				if (applyNonProjectileKB) MethodHandler.midairKnockback(target, strength, x, z);
				else MethodHandler.knockback(target, strength, x, z);
			}
			case CTS_5 -> MethodHandler.combatTest5Knockback(target, strength, x, z);
			//? <26.2 {
			/*case VANILLA -> vanillaCall.accept(target, strength, x, z);
			*///?} >=26.2 {
			case VANILLA -> vanillaCall.accept(target, strength, x, z, source, damage, fromEffect);
			//?}
			case OLD -> MethodHandler.oldKnockback(target, strength, x, z);
		}
	}

	//? >=26.2 {
	public void runKnockback(
		LivingEntity target,
		@Nullable DamageSource source,
		double strength,
		double x,
		double z
		//? <26.2 {
		/*, QuadConsumer<LivingEntity, Double, Double, Double> vanillaCall
		 *///?} >=26.2 {
		, SeptaConsumer<LivingEntity, Double, Double, Double, DamageSource, Float, Boolean> vanillaCall,
		float damage
		//?}
	) {
		runKnockback(target, source, strength, x, z, vanillaCall, damage, false);
	}
	//?}

	public boolean usesKnockback(boolean original, Entity entity) {
		return switch (this) {
			case OLD, MIDAIR -> false;
			default -> original;
		};
	}
}
