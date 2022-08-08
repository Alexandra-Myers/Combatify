package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.world.item.BowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BowItem.class)
public class BowItemMixin {

	@ModifyConstant(method = "releaseUsing",constant = @Constant(floatValue = 1.0F))
	public float modifyUncertaintyConstant(float value) {
		return 0.25F;
	}
}
