package net.alexandra.atlas.atlas_combat.extensions;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IBowItem {
    void stopUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks);

    float getFatigueForTime(int f);
}
