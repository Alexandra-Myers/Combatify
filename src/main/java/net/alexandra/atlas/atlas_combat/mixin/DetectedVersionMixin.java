package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.DetectedVersion;
import net.minecraft.WorldVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DetectedVersion.class)
public abstract class DetectedVersionMixin implements WorldVersion {
	@Inject(method = "getName", at = @At(value = "RETURN"), cancellable = true)
	public void changeName(CallbackInfoReturnable<String> cir) {
		cir.setReturnValue("Combat Test 8c");
		cir.cancel();
	}
	@Inject(method = "getProtocolVersion", at = @At(value = "RETURN"), cancellable = true)
	public void changeProtocol(CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue(800);
		cir.cancel();
	}
}
