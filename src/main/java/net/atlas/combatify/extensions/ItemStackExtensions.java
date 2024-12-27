package net.atlas.combatify.extensions;

import net.atlas.combatify.networking.NetworkingHandler;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface ItemStackExtensions {
	default void combatify$setBlockerInformation(List<Component> components, NetworkingHandler.ClientboundTooltipUpdatePacket.DataType type) {
		throw new IllegalStateException("Extension has not been applied");
	}
}
