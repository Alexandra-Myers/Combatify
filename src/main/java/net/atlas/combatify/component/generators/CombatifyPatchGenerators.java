package net.atlas.combatify.component.generators;

import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.PatchGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CombatifyPatchGenerators {
	private static final DeferredRegister<MapCodec<? extends PatchGenerator>> PATCH_GENERATOR_TYPES = DeferredRegister.create(Defaulted.PATCH_GENERATOR_TYPE, "defaulted");
	public static void init(IEventBus eventBus) {
		PATCH_GENERATOR_TYPES.register("combat_test_weapon_stats", () -> WeaponStatsGenerator.CODEC);
		PATCH_GENERATOR_TYPES.register(eventBus);
	}
}
