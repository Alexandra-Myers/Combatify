package net.alexandra.atlas.atlas_combat.item;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;

public interface IAttribute {
	static Attribute getAttribute(String name){
		return Registry.ATTRIBUTE.get(new ResourceLocation(name));
	}
	static Attribute getAttribute(ResourceLocation id){
		return Registry.ATTRIBUTE.get(id);
	}
}
