package net.alexandra.atlas.atlas_combat;

import com.chocohead.mm.api.ClassTinkerers;
import net.alexandra.atlas.atlas_combat.networking.NewServerboundInteractPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.quiltmc.loader.api.MappingResolver;
import org.quiltmc.loader.api.QuiltLoader;

import java.util.function.Function;

public class EarlyRiser implements Runnable {
	@Override
	public void run() {
		Function<FriendlyByteBuf, ServerboundInteractPacket.Action> buf = ignored -> NewServerboundInteractPacket.MISS_ATTACK_ACTION;
		MappingResolver remapper = QuiltLoader.getMappingResolver();

		String actionType = remapper.mapClassName("intermediary", "net.minecraft.class_2824$class_5907");
		ClassTinkerers.enumBuilder(actionType, "Ljava/util/function/Function;").addEnum("MISS_ATTACK", () -> new Object[] {buf}/*, "net.alexandra.atlas.atlas_combat.util.NewActionTypeStruct"*/).build();
	}
}
