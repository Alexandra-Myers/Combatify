package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.extensions.WeaponWithType;
import net.atlas.combatify.item.WeaponType;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {

	@Shadow
	@Final
	private Enchantment.EnchantmentDefinition definition;

	@Inject(method = "canEnchant", at = @At(value = "HEAD"), cancellable = true)
	public void canEnchant(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (hasSwordEnchants(stack))
			cir.setReturnValue(true);
	}
	@Inject(method = "isPrimaryItem", at = @At(value = "HEAD"), cancellable = true)
	public void isPrimaryItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		boolean isPrimaryForSwordEnchants = false;
		if (stack.getItem() instanceof WeaponWithType weaponWithType)
			isPrimaryForSwordEnchants = weaponWithType.getWeaponType().isPrimaryForSwordEnchants();
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(stack.getItem())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(stack.getItem());
			WeaponType type;
			if ((type = configurableItemData.type) != null)
				isPrimaryForSwordEnchants = type.isPrimaryForSwordEnchants();
			if (configurableItemData.isPrimaryForSwordEnchants != null)
				isPrimaryForSwordEnchants = configurableItemData.isPrimaryForSwordEnchants;
		}
		if (hasSwordEnchants(stack)) {
			if (isPrimaryForSwordEnchants)
				cir.setReturnValue(true);
			else if (definition.primaryItems().isEmpty())
				cir.setReturnValue(false);
		}
	}

	@Unique
	public boolean hasSwordEnchants(ItemStack stack) {
		boolean hasSwordEnchants = false;
		if (stack.getItem() instanceof WeaponWithType weaponWithType)
			hasSwordEnchants = weaponWithType.getWeaponType().hasSwordEnchants();
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(stack.getItem())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(stack.getItem());
			WeaponType type;
			if ((type = configurableItemData.type) != null)
				hasSwordEnchants = type.hasSwordEnchants();
			if (configurableItemData.hasSwordEnchants != null)
				hasSwordEnchants = configurableItemData.hasSwordEnchants;
		}
		return (definition.supportedItems().equals(ItemTags.SWORD_ENCHANTABLE) || definition.supportedItems().equals(ItemTags.WEAPON_ENCHANTABLE) || definition.supportedItems().equals(ItemTags.SHARP_WEAPON_ENCHANTABLE) || definition.supportedItems().equals(ItemTags.FIRE_ASPECT_ENCHANTABLE)) && hasSwordEnchants;
	}
}
