package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Tier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AxeItem.class)
public class AxeItemMixin extends DiggerItemMixin {
	public AxeItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}
}
