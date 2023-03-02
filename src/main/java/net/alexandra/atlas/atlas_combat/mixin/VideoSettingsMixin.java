package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasClient;
import net.alexandra.atlas.atlas_combat.extensions.IOptions;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VideoSettingsScreen.class)
public class VideoSettingsMixin {
	@Shadow
	@Final
	@Mutable
	private static Option[] OPTIONS = new Option[]{Option.GRAPHICS, Option.RENDER_DISTANCE, Option.PRIORITIZE_CHUNK_UPDATES, Option.SIMULATION_DISTANCE, Option.AMBIENT_OCCLUSION, Option.FRAMERATE_LIMIT, Option.ENABLE_VSYNC, Option.VIEW_BOBBING, Option.GUI_SCALE, Option.ATTACK_INDICATOR, Option.GAMMA, Option.RENDER_CLOUDS, Option.USE_FULLSCREEN, Option.PARTICLES, Option.MIPMAP_LEVELS, Option.ENTITY_SHADOWS, Option.SCREEN_EFFECTS_SCALE, Option.ENTITY_DISTANCE_SCALING, Option.FOV_EFFECTS_SCALE, Option.AUTOSAVE_INDICATOR, AtlasClient.rhythmicAttacksOption, AtlasClient.lowShieldOption, AtlasClient.protectionIndicatorOption, AtlasClient.attackIndicatorValueOption, AtlasClient.fishingRodLegacyOption};;
}
