package net.atlas.combatify.mixin.accessor;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FleeSunGoal.class)
public interface FleeSunGoalAccessor {
	@Accessor
	PathfinderMob getMob();
}
