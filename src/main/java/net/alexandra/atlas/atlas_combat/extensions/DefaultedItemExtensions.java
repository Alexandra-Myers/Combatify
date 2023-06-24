package net.alexandra.atlas.atlas_combat.extensions;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public interface DefaultedItemExtensions {
	Multimap<Attribute, AttributeModifier> getDefaultModifiers();

	void setDefaultModifiers(ImmutableMultimap<Attribute, AttributeModifier> modifiers);
}
