package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Attributes.class)
public class AttributesMixin {

	@ModifyVariable(target = "ATTACK_SPEED")

}
