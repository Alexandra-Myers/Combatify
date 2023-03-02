package net.alexandra.atlas.atlas_combat.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

import java.util.Arrays;
import java.util.Comparator;

@Environment(EnvType.CLIENT)
public enum ShieldIndicatorStatus {
	OFF(0, "options.off"),
	CROSSHAIR(1, "options.attack.crosshair"),
	HOTBAR(2, "options.attack.hotbar");

	private static final ShieldIndicatorStatus[] BY_ID = Arrays.stream(values())
		.sorted(Comparator.comparingInt(ShieldIndicatorStatus::getId))
		.toArray(i -> new ShieldIndicatorStatus[i]);
	private final int id;
	private final String key;

	ShieldIndicatorStatus(int j, String string2) {
		this.id = j;
		this.key = string2;
	}

	public int getId() {
		return this.id;
	}

	public String getKey() {
		return this.key;
	}

	public static ShieldIndicatorStatus byId(int id) {
		return BY_ID[Mth.positiveModulo(id, BY_ID.length)];
	}
}
