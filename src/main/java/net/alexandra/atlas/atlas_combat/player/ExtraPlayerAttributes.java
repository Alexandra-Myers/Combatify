package net.alexandra.atlas.atlas_combat.player;

import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class ExtraPlayerAttributes {

	public static final Attribute ATTACK_REACH = register("generic.attack_reach", (new RangedAttribute("attribute.name.generic.attack_reach", 2.5, 0.0, 6.0)).setSyncable(true));

	private static Attribute register(String var0, Attribute var1) {
		return Registry.register(Registry.ATTRIBUTE, var0, var1);
	}
}
