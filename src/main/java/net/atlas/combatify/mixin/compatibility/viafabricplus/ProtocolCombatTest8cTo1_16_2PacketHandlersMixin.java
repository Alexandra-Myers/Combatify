package net.atlas.combatify.mixin.compatibility.viafabricplus;

import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.annotation.mixin.ModSpecific;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = {"net.raphimc.viaaprilfools.protocol.scombattest8ctov1_16_2.ProtocolCombatTest8cTo1_16_2$1"})
@ModSpecific("viafabricplus")
public class ProtocolCombatTest8cTo1_16_2PacketHandlersMixin {
	@SuppressWarnings("unchecked")
	@ModifyArg(method = "register", at = @At(value = "INVOKE", target = "Lnet/raphimc/viaaprilfools/protocol/scombattest8ctov1_16_2/ProtocolCombatTest8cTo1_16_2$1;create(Lcom/viaversion/viaversion/api/type/Type;Ljava/lang/Object;)V"), index = 1, remap = false)
	public <T> T writeUseShieldOnCrouch(T value) {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
			return (T) CombatifyClient.shieldCrouch.get();
		return value;
	}

}
