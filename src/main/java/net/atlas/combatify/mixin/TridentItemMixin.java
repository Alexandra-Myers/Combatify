package net.atlas.combatify.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.atlas.combatify.extensions.DefaultedItemExtensions;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.extensions.WeaponWithType;
import net.atlas.combatify.item.WeaponType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TridentItem.class)
public class TridentItemMixin extends Item implements Vanishable, ItemExtensions, DefaultedItemExtensions, WeaponWithType {
	@Mutable
	@Shadow
	@Final
	private Multimap<Attribute, AttributeModifier> defaultModifiers;

	public TridentItemMixin(Item.Properties properties) {
		super(properties);
	}
	@Override
	public void modifyAttributeModifiers() {
		ImmutableMultimap.Builder<Attribute, AttributeModifier> var3 = ImmutableMultimap.builder();
		getWeaponType().addCombatAttributes(Tiers.NETHERITE, var3);
		ImmutableMultimap<Attribute, AttributeModifier> output = var3.build();
		((DefaultedItemExtensions)this).setDefaultModifiers(output);
	}

	@Override
	public void setStackSize(int stackSize) {
		this.maxStackSize = stackSize;
	}

	@Override
	public void setDefaultModifiers(ImmutableMultimap<Attribute, AttributeModifier> modifiers) {
		defaultModifiers = modifiers;
	}

	@Override
	public WeaponType getWeaponType() {
		return WeaponType.TRIDENT;
	}

	@Override
	public double getChargedAttackBonus() {
		return getWeaponType().getChargedReach();
	}
}
