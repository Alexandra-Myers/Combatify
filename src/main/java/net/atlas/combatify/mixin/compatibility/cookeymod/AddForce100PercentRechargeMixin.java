package net.atlas.combatify.mixin.compatibility.cookeymod;

import net.atlas.combatify.annotation.mixin.ModSpecific;
import net.atlas.combatify.extensions.MiscCategoryExtensions;
import net.rizecookey.cookeymod.config.ModConfig;
import net.rizecookey.cookeymod.config.category.Category;
import net.rizecookey.cookeymod.config.category.MiscCategory;
import net.rizecookey.cookeymod.config.option.BooleanOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ModSpecific("cookeymod")
@Mixin(MiscCategory.class)
public abstract class AddForce100PercentRechargeMixin extends Category implements MiscCategoryExtensions {
	@Unique
	public BooleanOption force100PercentRecharge;

	protected AddForce100PercentRechargeMixin(ModConfig modConfig) {
		super(modConfig);
	}

	@Inject(method = "<init>", at = @At("TAIL"), remap = false)
	public void addForce100PercentRecharge(ModConfig modConfig, CallbackInfo ci) {
		force100PercentRecharge = register(new BooleanOption("force100PercentRecharge", this, false));
	}

	@Override
	public BooleanOption combatify$force100PercentRecharge() {
		return force100PercentRecharge;
	}
}
