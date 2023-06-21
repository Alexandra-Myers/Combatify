package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Attributes.class)
public class AttributesMixin {

	@Shadow
	@Mutable
	@Final
	public static Attribute ATTACK_SPEED = registrySet(7,"generic.attack_speed", (new RangedAttribute("attribute.name.generic.attack_speed", 4.0, 0.10000000149011612, 1024.0)).setSyncable(true));

	private static Attribute registrySet(int rawId, String id, Attribute attribute) {
		return Registry.registerMapping(BuiltInRegistries.ATTRIBUTE, rawId,id, attribute);
	}
}
