package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasClient;
import net.alexandra.atlas.atlas_combat.extensions.IOptions;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import net.minecraft.client.gui.components.Button;

@Mixin(ControlsScreen.class)
public abstract class ControlsMixin extends OptionsSubScreen {
	public ControlsMixin(Screen screen, Options options, Component component) {
		super(screen, options, component);
	}

	@Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/controls/ControlsScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 4), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void injectOptions(CallbackInfo ci, int i, int j, int k) {
		addRenderableWidget(AtlasClient.autoAttackOption.createButton(this.options, j, k, 150));
		k+=24;
		addRenderableWidget(AtlasClient.shieldCrouchOption.createButton(this.options, i, k, 150));
	}
	@Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/controls/ControlsScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 5))
	private GuiEventListener redirectDoneButton(ControlsScreen instance, GuiEventListener guiEventListener) {
		return this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, (button) -> {
			this.minecraft.setScreen(this.lastScreen);
		}));

		//return addRenderableWidget(this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen))));
	}
}
