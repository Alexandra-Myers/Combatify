package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.item.IAttribute;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Attributes.class)
public class EntityAttributesMixin implements IAttribute {
	@Shadow
	private static Attribute register(String var0, Attribute var1) {
		return null;
	}

	@Shadow
	@Final
	public static Attribute ATTACK_SPEED;

	private static Attribute registrySet(String var0, Attribute var1, Attribute var2){
		return Registry.registerMapping(Registry.ATTRIBUTE, Registry.ATTRIBUTE.getId(var2), var0, var1);
	}

	static final Attribute NEW_ATTACK_SPEED = registrySet("generic.attack_speed", (new RangedAttribute("attribute.name.generic.attack_speed", 4.0, 0.10000000149011612, 1024.0)).setSyncable(true), ATTACK_SPEED);

	static final Attribute ATTACK_REACH = register("generic.attack_reach", (new RangedAttribute("attribute.name.generic.attack_reach", 2.5, 0.0, 6.0)).setSyncable(true));

	@Override
	public Attribute getAttribute(String name) {
		return Registry.ATTRIBUTE.get(new ResourceLocation(name));
	}
}
