package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.IUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(ClientboundUpdateAttributesPacket.class)
public class ClientboundUpdateAttributesPacketMixin implements IUpdateAttributesPacket {
	@Shadow
	@Final
	private List<ClientboundUpdateAttributesPacket.AttributeSnapshot> attributes;

	@Override
	public void changeAttributes(ServerPlayer reciever) {
		List<Integer> indexes = new ArrayList<>();
		for (ClientboundUpdateAttributesPacket.AttributeSnapshot attributeSnapshot : attributes) {
			if (attributeSnapshot.getAttribute() == Attributes.ATTACK_SPEED && Combatify.unmoddedPlayers.contains(reciever.getUUID())) {
				indexes.add(attributes.indexOf(attributeSnapshot));
			}
		}
		if (!indexes.isEmpty()) {
			for (Integer index : indexes) {
				ClientboundUpdateAttributesPacket.AttributeSnapshot attributeSnapshot = attributes.remove(index.intValue());
				attributes.add(index, new ClientboundUpdateAttributesPacket.AttributeSnapshot(attributeSnapshot.getAttribute(), attributeSnapshot.getBase() - 1.5, attributeSnapshot.getModifiers()));
			}
		}
	}
}
