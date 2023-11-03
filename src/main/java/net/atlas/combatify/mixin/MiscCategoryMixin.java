package net.atlas.combatify.mixin;

import net.atlas.combatify.extensions.IMiscCategory;
import net.rizecookey.cookeymod.config.ModConfig;
import net.rizecookey.cookeymod.config.category.Category;
import net.rizecookey.cookeymod.config.category.MiscCategory;
import net.rizecookey.cookeymod.config.option.BooleanOption;
import net.rizecookey.cookeymod.config.option.Option;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MiscCategory.class)
public abstract class MiscCategoryMixin extends Category implements IMiscCategory {
	public Option<Boolean> force100PercentRecharge;

	public MiscCategoryMixin(ModConfig modConfig) {
		super(modConfig);
	}

	@Inject(method = "<init>", at = @At(value = "RETURN"), remap = false)
	public void addOption(ModConfig modConfig, CallbackInfo ci) {
		this.force100PercentRecharge = register(new BooleanOption("force100PercentRecharge", this, false));
	}

	@Override
	public Option<Boolean> combatify$getForce100PercentRecharge() {
		return force100PercentRecharge;
	}
}
