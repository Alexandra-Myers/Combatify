package net.atlas.combatify.extensions;

import net.minecraft.world.damagesource.DamageSource;

public interface LivingEntityExtensions {
	boolean combatify$hasEnabledShieldOnCrouch();

	void combatify$setPiercingNegation(double negation);

	boolean combatify$getIsParry();

	void combatify$setIsParry(boolean isParry);

	int combatify$getIsParryTicker();

	void combatify$setIsParryTicker(int isParryTicker);

    float combatify$getNewDamageAfterArmorAbsorb(DamageSource source, float amount, double piercingLevel);

	float combatify$getNewDamageAfterMagicAbsorb(DamageSource source, float amount, double piercingLevel);
}
