package net.atlas.combatify.mixin.compatibility.viafabricplus;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.rewriter.EntityPacketRewriter1_20_5;
import net.atlas.combatify.annotation.mixin.ModSpecific;
import net.raphimc.viaaprilfools.api.AprilFoolsProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

@Mixin(EntityPacketRewriter1_20_5.class)
@ModSpecific("viafabricplus")
public class EntityPacketRewriter1_20_5Mixin {
	@WrapWithCondition(method = "sendRangeAttributes", at = @At(value = "INVOKE", target = "Lcom/viaversion/viaversion/protocols/v1_20_3to1_20_5/rewriter/EntityPacketRewriter1_20_5;writeAttribute(Lcom/viaversion/viaversion/api/protocol/packet/PacketWrapper;Ljava/lang/String;DLjava/util/UUID;D)V", ordinal = 1), remap = false)
	public boolean disableOn8c(EntityPacketRewriter1_20_5 instance, PacketWrapper wrapper, String attributeId, double base, UUID modifierId, double amount, @Local(ordinal = 0, argsOnly = true) UserConnection connection) {
		return !connection.getProtocolInfo().serverProtocolVersion().equals(AprilFoolsProtocolVersion.sCombatTest8c);
	}
}
