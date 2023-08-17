package net.atlas.combatify;

import com.chocohead.mm.api.ClassTinkerers;
import net.atlas.combatify.networking.NewServerboundInteractPacket;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import java.util.function.Function;

public class EarlyRiser implements Runnable {
	@Override
	public void run() {
		Function<FriendlyByteBuf, ServerboundInteractPacket.Action> buf = ignored -> NewServerboundInteractPacket.MISS_ATTACK_ACTION;
		MappingResolver remapper = FabricLoader.getInstance().getMappingResolver();

		String actionType = remapper.mapClassName("intermediary", "net.minecraft.class_2824$class_5907");
		ClassTinkerers.enumBuilder(actionType, "Ljava/util/function/Function;").addEnum("MISS_ATTACK", () -> new Object[] {buf}).build();
	}
}
