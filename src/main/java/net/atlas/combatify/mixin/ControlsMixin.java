package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.extensions.IOptions;
import net.minecraft.client.Options;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ControlsScreen.class)
public abstract class ControlsMixin extends OptionsSubScreen {
	public ControlsMixin(Screen screen, Options options, Component component) {
		super(screen, options, component);
	}

	@Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 5))
	private void injectOptions(CallbackInfo ci, @Local(ordinal = 0) GridLayout.RowHelper rowHelper) {
		rowHelper.addChild(((IOptions)options).autoAttack().createButton(this.options));
		rowHelper.addChild(((IOptions)options).shieldCrouch().createButton(this.options));
	}
}
