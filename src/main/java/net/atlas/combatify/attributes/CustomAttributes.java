package net.atlas.combatify.attributes;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class CustomAttributes {
	public static final Holder<Attribute> SHIELD_DISABLE_TIME = register(
		"generic.shield_disable_time", new RangedAttribute("attribute.name.generic.shield_disable_time", 0.0, -1024.0, 2048.0)
	);
	public static final Holder<Attribute> SHIELD_DISABLE_REDUCTION = register(
		"generic.shield_disable_reduction", new RangedAttribute("attribute.name.generic.shield_disable_reduction", 0.0, -1024.0, 2048.0)
	);

	private static Holder<Attribute> register(String string, Attribute attribute) {
		return Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, ResourceLocation.withDefaultNamespace(string), attribute);
	}

	public static void registerAttributes() {

	}
}
