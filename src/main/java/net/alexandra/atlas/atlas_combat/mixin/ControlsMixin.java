package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.IOptions;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ControlsScreen.class)
public abstract class ControlsMixin extends OptionsSubScreen {
	public ControlsMixin(Screen screen, Options options, Component component) {
		super(screen, options, component);
	}

	@Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/controls/ControlsScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 4), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void injectOptions(CallbackInfo ci, int i, int j, int k) {
		this.addRenderableWidget(((IOptions)options).autoAttack().createButton(this.options, j, k, 150));
	}
}
