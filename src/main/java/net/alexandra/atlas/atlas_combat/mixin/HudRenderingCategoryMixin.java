package net.alexandra.atlas.atlas_combat.mixin;

import net.rizecookey.cookeymod.config.ModConfig;
import net.rizecookey.cookeymod.config.category.Category;
import net.rizecookey.cookeymod.config.category.HudRenderingCategory;
import net.rizecookey.cookeymod.config.option.DoubleSliderOption;
import net.rizecookey.cookeymod.config.option.Option;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HudRenderingCategory.class)
public abstract class HudRenderingCategoryMixin {
	@Shadow
	public Option<Double> attackCooldownHandOffset = ((HudRenderingCategory)(Object)this).register(new DoubleSliderOption("attackCooldownHandOffset", ((HudRenderingCategory)(Object)this), 0.65, -1.0 + 0.65, 1.0 + 0.65));
}
