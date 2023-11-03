package net.atlas.combatify.extensions;

import com.google.common.collect.ImmutableMultimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public interface DefaultedItemExtensions {
	void combatify$setDefaultModifiers(ImmutableMultimap<Attribute, AttributeModifier> modifiers);
}
