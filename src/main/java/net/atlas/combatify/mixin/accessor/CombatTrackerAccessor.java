package net.atlas.combatify.mixin.accessor;

import net.minecraft.world.damagesource.CombatTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CombatTracker.class)
public interface CombatTrackerAccessor {
	@Accessor
	boolean isInCombat();
}
