package net.atlas.combatify.component.generators;

import net.atlas.defaulted.fabric.component.DefaultedRegistries;

public class CombatifyPatchGenerators {
	public static void init() {
		DefaultedRegistries.registerPatchGenerator("combat_test_weapon_stats", WeaponStatsGenerator.CODEC);
		//? <=1.21.1 {
		DefaultedRegistries.registerPatchGenerator("modify_extended_blocking_data", BlocksAttacksGenerator.CODEC);
		//?}
	}
}
