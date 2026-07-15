package net.atlas.combatify.config;

import net.atlas.combatify.Combatify;
//? >=26.2 {
/*import net.atlas.combatify.util.HexaConsumer;
*///?}
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FishingHook;
//? <26.2 {
import org.apache.commons.lang3.function.TriConsumer;

import java.util.Map;
import java.util.WeakHashMap;
//?}

public enum KnockbackMode {
	VANILLA,
	OLD,
	CTS_8C,
	CTS_5,
	MIDAIR;
	//? <26.2 {
	private static final Map<LivingEntity, DamageSource> CONTEXTS = new WeakHashMap<>();
	//?}
	public void runKnockback(
		LivingEntity target,
		double strength,
		double x,
		double z,
		//? <26.2 {
		TriConsumer<Double, Double, Double> vanillaCall
		//?} >=26.2 {
		/*DamageSource source,
		float damage,
		boolean fromEffect,
		HexaConsumer<Double, Double, Double, DamageSource, Float, Boolean> vanillaCall
		*///?}
	) {
		//? <26.2 {
		DamageSource source = CONTEXTS.remove(target);
		//?}
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
			case VANILLA -> vanillaCall.accept(strength, x, z);
			//?} >=26.2 {
			/*case VANILLA -> vanillaCall.accept(strength, x, z, source, damage, fromEffect);
			*///?}
			case OLD -> MethodHandler.oldKnockback(target, strength, x, z);
		}
	}

	public boolean usesKnockback(boolean original, Entity entity) {
		return switch (this) {
			case OLD, MIDAIR -> false;
			default -> original;
		};
	}

	//? <26.2 {
	public static void extractContext(LivingEntity target, DamageSource source) {
		CONTEXTS.put(target, source);
	}
	//?}
}
