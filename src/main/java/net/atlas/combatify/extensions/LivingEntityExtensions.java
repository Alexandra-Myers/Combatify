package net.atlas.combatify.extensions;

public interface LivingEntityExtensions {
    double getPiercingNegation();

    boolean hasEnabledShieldOnCrouch();

	void setPiercingNegation(double negation);

    void setUseItemRemaining(int useItemRemaining);
}
