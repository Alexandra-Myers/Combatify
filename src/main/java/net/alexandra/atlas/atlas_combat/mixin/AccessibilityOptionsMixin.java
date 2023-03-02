package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasClient;
import net.alexandra.atlas.atlas_combat.extensions.IOptions;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(AccessibilityOptionsScreen.class)
public class AccessibilityOptionsMixin {
	@Shadow
	@Final
	@Mutable
	private static Option[] OPTIONS = new Option[]{Option.NARRATOR, Option.SHOW_SUBTITLES, Option.TEXT_BACKGROUND_OPACITY, Option.TEXT_BACKGROUND, Option.CHAT_OPACITY, Option.CHAT_LINE_SPACING, Option.CHAT_DELAY, Option.AUTO_JUMP, Option.TOGGLE_CROUCH, Option.TOGGLE_SPRINT, Option.SCREEN_EFFECTS_SCALE, Option.FOV_EFFECTS_SCALE, Option.DARK_MOJANG_STUDIOS_BACKGROUND_COLOR, Option.HIDE_LIGHTNING_FLASH, AtlasClient.autoAttackOption, AtlasClient.shieldCrouchOption, AtlasClient.lowShieldOption, AtlasClient.attackIndicatorValueOption, AtlasClient.fishingRodLegacyOption};
}
