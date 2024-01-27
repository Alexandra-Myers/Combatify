package net.atlas.combatify.extensions;

public interface LivingEntityExtensions {
	boolean hasEnabledShieldOnCrouch();

	void setPiercingNegation(double negation);

	boolean getIsParry();

	void setIsParry(boolean isParry);

	int getIsParryTicker();

	void setIsParryTicker(int isParryTicker);

    void setUseItemRemaining(int useItemRemaining);
}
