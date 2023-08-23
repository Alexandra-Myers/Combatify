package net.atlas.combatify.extensions;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;

public interface LivingEntityExtensions {

	void projectileKnockback(double strength, double x, double z);

	ItemStack getBlockingItem();

	boolean isItemOnCooldown(ItemStack var1);

	boolean hasEnabledShieldOnCrouch();

	void setPiercingNegation(double negation);

	void newKnockback(double strength, double x, double z);

	boolean getIsParry();

	void setIsParry(boolean isParry);

	int getIsParryTicker();

	void setIsParryTicker(int isParryTicker);

    float getNewDamageAfterArmorAbsorb(DamageSource source, float amount, double piercingLevel);

	float getNewDamageAfterMagicAbsorb(DamageSource source, float amount, double piercingLevel);
}
