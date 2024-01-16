package net.atlas.combatify.networking;

import net.atlas.combatify.extensions.LivingEntityExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ClientOnly {
	public static void setUseRemainingTicks(RemainingUseSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
		Entity entity = Minecraft.getInstance().level.getEntity(packet.id());
		if (entity instanceof LivingEntityExtensions livingEntity)
			livingEntity.setUseItemRemaining(packet.ticks());
	}
}
