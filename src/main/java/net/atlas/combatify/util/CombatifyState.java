package net.atlas.combatify.util;

import com.mojang.serialization.Codec;
import net.atlas.combatify.Combatify;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public enum CombatifyState {
	VANILLA(0, "Vanilla", "vanilla"),
	CTS_8C(1, "CTS 8C", "combat_test"),
	COMBATIFY(2, "Combatify", "combatify");

	public static final Codec<CombatifyState> CODEC = Codec.INT.xmap(id -> switch (Mth.positiveModulo(id, 3)) {
		case 0 -> CombatifyState.VANILLA;
		case 1 -> CombatifyState.CTS_8C;
		default -> CombatifyState.COMBATIFY;
	}, CombatifyState::id);

	public final int id;
	public final String name;
	public final Component caption;

	CombatifyState(int id, String name, String key) {
		this.id = id;
		this.name = name;
		this.caption = Component.translatableWithFallback("options.combatify_state." + key, name);
	}

	public int id() {
		return id;
	}

	public Component caption() {
		return caption;
	}

	@Override
	public String toString() {
		return "CombatifyState{" +
			"id=" + id + '\n' +
			"name='" + name + '\'' +
			'}';
	}
}
