package net.atlas.combatify.extensions;

import net.minecraft.world.damagesource.DamageSource;

public interface LivingEntityExtensions {

	boolean hasEnabledShieldOnCrouch();

	void setPiercingNegation(double negation);

	boolean getIsParry();

	void setIsParry(boolean isParry);

	int getIsParryTicker();

	void setIsParryTicker(int isParryTicker);

    float getNewDamageAfterArmorAbsorb(DamageSource source, float amount, double piercingLevel);

	float getNewDamageAfterMagicAbsorb(DamageSource source, float amount, double piercingLevel);
}
