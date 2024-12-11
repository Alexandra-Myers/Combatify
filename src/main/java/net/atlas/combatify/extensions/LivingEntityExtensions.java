package net.atlas.combatify.extensions;

import net.minecraft.world.item.ItemCooldowns;

public interface LivingEntityExtensions {
    double getPiercingNegation();

    boolean hasEnabledShieldOnCrouch();

    ItemCooldowns combatify$getFallbackCooldowns();

    void setPiercingNegation(double negation);

    void setUseItemRemaining(int useItemRemaining);
}
