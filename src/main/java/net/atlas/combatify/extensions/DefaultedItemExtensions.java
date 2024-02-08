package net.atlas.combatify.extensions;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public interface DefaultedItemExtensions {
	void setDefaultModifiers(ImmutableMultimap<Holder<Attribute>, AttributeModifier> modifiers);
}
