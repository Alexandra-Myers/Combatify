package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.item.IAttribute;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.Desc;

@Mixin(Attributes.class)
public class AttributesMixin implements IAttribute {
}
