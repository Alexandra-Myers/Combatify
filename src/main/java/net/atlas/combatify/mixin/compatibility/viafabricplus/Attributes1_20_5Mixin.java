package net.atlas.combatify.mixin.compatibility.viafabricplus;

import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Attributes1_20_5;
import com.viaversion.viaversion.util.Key;
import net.atlas.combatify.annotation.mixin.ModSpecific;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Attributes1_20_5.class)
@ModSpecific("viafabricplus")
public class Attributes1_20_5Mixin {
	@Inject(method = "keyToId", at = @At(value = "HEAD"), remap = false, cancellable = true)
	private static void insertMappingForCTS(String attribute, CallbackInfoReturnable<Integer> cir) {
		if (Key.stripMinecraftNamespace(attribute).equals("generic.attack_reach"))
			cir.setReturnValue(7);
	}
}
