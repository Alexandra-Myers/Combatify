package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.IOptions;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AccessibilityOptionsScreen.class)
public class AccessibilityOptionsMixin {
	@Inject(method = "options", at = @At(value = "HEAD"))
	private static void injectOptions(Options options, CallbackInfoReturnable<OptionInstance<?>[]> cir) {
		((IOptions)options).autoAttack();
	}
}
