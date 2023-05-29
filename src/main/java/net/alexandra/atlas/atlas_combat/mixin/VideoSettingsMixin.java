package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.IOptions;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VideoSettingsScreen.class)
public class VideoSettingsMixin {
	@Inject(method = "options", at = @At(value = "HEAD"), cancellable = true)
	private static void injectOptions(Options options, CallbackInfoReturnable<OptionInstance<?>[]> cir) {
		OptionInstance<?>[] optionInstances = new OptionInstance[]{
				options.graphicsMode(),
				options.renderDistance(),
				options.prioritizeChunkUpdates(),
				options.simulationDistance(),
				options.ambientOcclusion(),
				options.framerateLimit(),
				options.enableVsync(),
				options.bobView(),
				options.guiScale(),
				options.attackIndicator(),
				((IOptions)options).shieldIndicator(),
				options.gamma(),
				options.cloudStatus(),
				options.fullscreen(),
				options.particles(),
				options.mipmapLevels(),
				options.entityShadows(),
				options.screenEffectScale(),
				options.entityDistanceScaling(),
				options.fovEffectScale(),
				options.showAutosaveIndicator(),
				((IOptions)options).lowShield(),
				((IOptions)options).attackIndicatorValue(),
				((IOptions)options).rhythmicAttacks(),
				((IOptions)options).protIndicator(),
				((IOptions)options).fishingRodLegacy()
		};
		cir.setReturnValue(optionInstances);
		cir.cancel();
	}
}
