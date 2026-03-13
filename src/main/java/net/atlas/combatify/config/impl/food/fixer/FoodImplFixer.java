package net.atlas.combatify.config.impl.food.fixer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.atlas.atlascore.config.AtlasConfig;
import net.atlas.atlascore.config.fixer.ConfigHolderFixer;
import net.atlas.combatify.config.impl.JSImpl;
import net.atlas.combatify.config.impl.food.CTSFoodImpl;
import net.atlas.combatify.config.impl.food.CombatifyFoodImpl;
import net.atlas.combatify.config.impl.food.FoodImpl;
import net.atlas.combatify.config.impl.food.JSFoodImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FoodImplFixer extends ConfigHolderFixer<FoodImpl> {
	public FoodImplFixer(AtlasConfig.ConfigHolder<FoodImpl> owner) {
		super(owner);
	}

	@Override
	public JsonElement fixData(@Nullable JsonElement value, @Nullable JsonObject holderRootObject, @NotNull JsonObject configRootObject) {
		value = super.fixData(value, holderRootObject, configRootObject);
		if (value.isJsonPrimitive()) {
			String asStr = value.getAsString();
			FoodImpl val = switch (asStr) {
				case "vanilla_food_impl" -> new CTSFoodImpl(false, false, 6, 18, 20, 0.5F, 4, 4);
				case "cts_food_impl" -> new CTSFoodImpl(true, true, 6, 7, 21, 0.5F, 2, 2);
				case "combatify_food_impl" -> new CombatifyFoodImpl(6, 9, 21, 0.5F, 2F, 2F);
				default -> new JSFoodImpl(new JSImpl(asStr));
			};
			value = FoodImpl.CODEC.encodeStart(JsonOps.INSTANCE, val).getOrThrow();
		}
		return value;
	}

	@Override
	protected boolean mustFix(@Nullable JsonElement value) {
		return value == null || value.isJsonPrimitive();
	}
}
