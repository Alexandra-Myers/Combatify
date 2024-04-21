package net.atlas.combatify.mixin.cookey;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("SPEED_MODIFIER_SPRINTING")
    static AttributeModifier SPEED_MODIFIER_SPRINTING() {
        return null;
    }
}
