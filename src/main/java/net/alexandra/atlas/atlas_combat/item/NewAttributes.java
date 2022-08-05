package net.alexandra.atlas.atlas_combat.item;

import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class NewAttributes extends Attributes{
	private static Attribute registrySet(String var0, Attribute var1, Attribute var2){
		return Registry.registerMapping(Registry.ATTRIBUTE, Registry.ATTRIBUTE.getId(var2), var0, var1);
	}

	private static Attribute register(String id, Attribute attribute) {
		return Registry.register(Registry.ATTRIBUTE, id, attribute);
	}

	public static final Attribute NEW_ATTACK_SPEED = registrySet("generic.attack_speed", (new RangedAttribute("attribute.name.generic.attack_speed", 4.0, 0.10000000149011612, 1024.0)).setSyncable(true), ATTACK_SPEED);

	public static final Attribute ATTACK_REACH = register("generic.attack_reach", (new RangedAttribute("attribute.name.generic.attack_reach", 2.5, 0.0, 6.0)).setSyncable(true));
}
