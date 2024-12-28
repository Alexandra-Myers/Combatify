package net.atlas.combatify.mixin;

import net.atlas.combatify.critereon.CustomLootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootContextParamSets.class)
public class LootContextParamSetsMixin {
	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void appendCustomParamSet(CallbackInfo ci) {
		CustomLootContextParamSets.init();
	}
}
