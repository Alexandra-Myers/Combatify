package net.atlas.combatify.extensions;

public interface LivingEntityExtensions {

	boolean hasEnabledShieldOnCrouch();

	void setPiercingNegation(double negation);

    void setUseItemRemaining(int ticks);
}
