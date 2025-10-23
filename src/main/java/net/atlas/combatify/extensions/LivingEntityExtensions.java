package net.atlas.combatify.extensions;

public interface LivingEntityExtensions {
	boolean combatify$hasEnabledShieldOnCrouch();

	void setPiercingNegation(double negation);

    void setUseItemRemaining(int useItemRemaining);
}
