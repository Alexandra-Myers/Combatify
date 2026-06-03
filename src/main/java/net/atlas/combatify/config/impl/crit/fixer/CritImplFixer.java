package net.atlas.combatify.config.impl.crit.fixer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.atlas.atlascore.config.AtlasConfig;
import net.atlas.atlascore.config.fixer.ConfigHolderFixer;
import net.atlas.combatify.config.impl.JSImpl;
import net.atlas.combatify.config.impl.crit.CTSCritImpl;
import net.atlas.combatify.config.impl.crit.CombatifyCritImpl;
import net.atlas.combatify.config.impl.crit.CritImpl;
import net.atlas.combatify.config.impl.crit.JSCritImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CritImplFixer extends ConfigHolderFixer<CritImpl> {
	public CritImplFixer(AtlasConfig.ConfigHolder<CritImpl> owner) {
		super(owner);
	}

	@Override
	public JsonElement fixData(@Nullable JsonElement value, @Nullable JsonObject holderRootObject, @NotNull JsonObject configRootObject) {
		value = super.fixData(value, holderRootObject, configRootObject);
		if (value.isJsonPrimitive()) {
			String asStr = value.getAsString();
			CritImpl val = switch (asStr) {
				case "vanilla_crit_impl" -> new CTSCritImpl(false, 0.9F, 1.5F);
				case "cts_crit_impl" -> new CTSCritImpl(true, -1F, 1.5F);
				case "combatify_crit_impl" -> new CombatifyCritImpl(true, 0.9F, 1.95F, 1.25F, 1.5F);
				default -> new JSCritImpl(new JSImpl(asStr));
			};
			value = CritImpl.CODEC.encodeStart(JsonOps.INSTANCE, val).getOrThrow();
		}
		return value;
	}

	@Override
	protected boolean mustFix(@Nullable JsonElement value) {
		return value == null || value.isJsonPrimitive();
	}
}
