package net.atlas.combatify.mixin.accessors;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Collection;

@Mixin(AttributeInstance.class)
public interface AttributeInstanceAccessor {

	@Invoker("getModifiersOrEmpty")
	Collection<AttributeModifier> combatify$getModifiersOrEmpty(AttributeModifier.Operation operation);

}
