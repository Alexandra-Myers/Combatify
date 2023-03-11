package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.IAttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;

@Mixin(AttributeInstance.class)
public abstract class AttributeInstanceMixin implements IAttributeInstance {
	@Shadow
	protected abstract Collection<AttributeModifier> getModifiersOrEmpty(AttributeModifier.Operation operation);

	@Shadow
	@Final
	private Attribute attribute;

	AttributeInstance attributeInstance = ((AttributeInstance)(Object)this);

	@Override
	public final double calculateValue(float damageBonus) {
		double d = attributeInstance.getBaseValue();

		for(AttributeModifier attributeModifier : getModifiersOrEmpty(AttributeModifier.Operation.ADDITION)) {
			d += attributeModifier.getAmount();
		}

		double e = d;

		e += damageBonus;

		for(AttributeModifier attributeModifier2 : getModifiersOrEmpty(AttributeModifier.Operation.MULTIPLY_BASE)) {
			e += d * attributeModifier2.getAmount();
		}

		for(AttributeModifier attributeModifier2 : getModifiersOrEmpty(AttributeModifier.Operation.MULTIPLY_TOTAL)) {
			e *= 1.0 + attributeModifier2.getAmount();
		}

		return attribute.sanitizeValue(e);
	}
}
