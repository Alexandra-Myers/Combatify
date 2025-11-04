package net.atlas.combatify.config;

import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;

import java.util.Arrays;
import java.util.function.IntFunction;

@Environment(EnvType.CLIENT)
public enum DualAttackIndicatorStatus {
	OFF(0, "options.off"),
	BOTTOM(1, "options.dualAttackIndicator.bottom"),
	SIDE(2, "options.dualAttackIndicator.side");

	private static final IntFunction<DualAttackIndicatorStatus> BY_ID = ByIdMap.continuous((dualAttackIndicatorStatus) -> dualAttackIndicatorStatus.id, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
	public static final Codec<DualAttackIndicatorStatus> LEGACY_CODEC = Codec.INT.xmap(BY_ID::apply, dualAttackIndicatorStatus -> dualAttackIndicatorStatus.id);
	public static final Component[] AS_COMPONENTS = Arrays.stream(DualAttackIndicatorStatus.values()).map(DualAttackIndicatorStatus::caption).toArray(Component[]::new);
	private final int id;
	private final Component caption;

	DualAttackIndicatorStatus(int id, String translationKey) {
		this.id = id;
		this.caption = Component.translatable(translationKey);
	}

	public Component caption() {
		return caption;
	}

	public boolean isOn() {
		return this != OFF;
	}
}
