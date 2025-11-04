package net.atlas.combatify.config;

import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;

import java.util.Arrays;
import java.util.function.IntFunction;

@Environment(EnvType.CLIENT)
public enum ShieldIndicatorStatus {
	OFF(0, "options.off"),
	CROSSHAIR(1, "options.attack.crosshair"),
	HOTBAR(2, "options.attack.hotbar");

	private static final IntFunction<ShieldIndicatorStatus> BY_ID = ByIdMap.continuous((shieldIndicatorStatus) -> shieldIndicatorStatus.id, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
	public static final Codec<ShieldIndicatorStatus> LEGACY_CODEC = Codec.INT.xmap(BY_ID::apply, shieldIndicatorStatus -> shieldIndicatorStatus.id);
	public static final Component[] AS_COMPONENTS = Arrays.stream(ShieldIndicatorStatus.values()).map(ShieldIndicatorStatus::caption).toArray(Component[]::new);
	private final int id;
	private final Component caption;

	ShieldIndicatorStatus(int id, String translationKey) {
		this.id = id;
		this.caption = Component.translatable(translationKey);
	}

	public Component caption() {
		return caption;
	}
}
