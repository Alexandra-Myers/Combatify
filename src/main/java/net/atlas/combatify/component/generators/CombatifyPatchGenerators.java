package net.atlas.combatify.component.generators;

import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.init.ItemPatchGenerators;
import net.atlas.defaulted.init.registry.Bootstrapper;

public class CombatifyPatchGenerators extends Bootstrapper<MapCodec<? extends PatchGenerator>> {
	public static final CombatifyPatchGenerators INSTANCE = new CombatifyPatchGenerators();
	public CombatifyPatchGenerators() {
		super(Defaulted.PATCH_GENERATOR_TYPE, "defaulted", ItemPatchGenerators.INSTANCE.getRegistry());
	}

	@Override
	protected void bootstrap() {
		register("combat_test_weapon_stats", () -> WeaponStatsGenerator.CODEC);
		//? <=1.21.1 {
		/*register("modify_extended_blocking_data", () -> BlocksAttacksGenerator.CODEC);
		*///?}
	}
}
