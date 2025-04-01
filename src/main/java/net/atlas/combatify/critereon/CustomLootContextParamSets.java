package net.atlas.combatify.critereon;

import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class CustomLootContextParamSets {
	public static final LootContextParamSet BLOCKED_DAMAGE = LootContextParamSets.register("blocked_damage", builder -> builder.required(LootContextParams.THIS_ENTITY)
		.required(LootContextParams.ENCHANTMENT_LEVEL)
		.required(LootContextParams.TOOL)
		.required(LootContextParams.ORIGIN)
		.required(LootContextParams.DAMAGE_SOURCE)
		.optional(LootContextParams.DIRECT_ATTACKING_ENTITY)
		.optional(LootContextParams.ATTACKING_ENTITY));

	public static void init() {

	}
}
