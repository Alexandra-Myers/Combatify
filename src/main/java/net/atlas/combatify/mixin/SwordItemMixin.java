package net.atlas.combatify.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ConfigurableWeaponData;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.mixin.accessors.ItemAccessor;
import net.atlas.combatify.util.BlockingType;
import net.atlas.combatify.extensions.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SwordItem.class)
public class SwordItemMixin extends TieredItem implements ItemExtensions, DefaultedItemExtensions, WeaponWithType {
	@Shadow
	@Final
	@Mutable
	private Multimap<Attribute, AttributeModifier> defaultModifiers;

	public SwordItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}

	@Override
	public void modifyAttributeModifiers() {
		ImmutableMultimap.Builder<Attribute, AttributeModifier> var3 = ImmutableMultimap.builder();
		combatify$getWeaponType().addCombatAttributes(getTier(), var3);
		ImmutableMultimap<Attribute, AttributeModifier> output = var3.build();
		((DefaultedItemExtensions)this).combatify$setDefaultModifiers(output);
	}

	@Inject(method = "getDamage", at = @At(value = "RETURN"), cancellable = true)
	public void getDamage(CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue((float) combatify$getWeaponType().getDamage(getTier()));
	}

	@Override
	public void combatify$setStackSize(int stackSize) {
		((ItemAccessor) this).combatify$setMaxStackSize(stackSize);
	}

	@Override
	public BlockingType combatify$getBlockingType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(this);
			if (configurableItemData.blockingType != null) {
				return configurableItemData.blockingType;
			}
		}
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(combatify$getWeaponType())) {
			ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(combatify$getWeaponType());
			if (configurableWeaponData.blockingType != null) {
				return configurableWeaponData.blockingType;
			}
		}
		return Combatify.registeredTypes.get("sword");
	}

	@Override
	public void combatify$setDefaultModifiers(ImmutableMultimap<Attribute, AttributeModifier> modifiers) {
		defaultModifiers = modifiers;
	}

	@Override
	public WeaponType combatify$getWeaponType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			WeaponType type = Combatify.ITEMS.configuredItems.get(this).type;
			if (type != null)
				return type;
		}
		return WeaponType.SWORD;
	}
	@Override
	public double combatify$getChargedAttackBonus() {
		Item item = this;
		double chargedBonus = combatify$getWeaponType().getChargedReach();
		if(Combatify.ITEMS.configuredItems.containsKey(item)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
			if (configurableItemData.chargedReach != null)
				chargedBonus = configurableItemData.chargedReach;
		}
		return chargedBonus;
	}
}
