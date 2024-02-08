package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.extensions.CustomEnchantment;
import net.atlas.combatify.extensions.WeaponWithType;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin implements CustomEnchantment {
	@Shadow
	public abstract boolean canEnchant(ItemStack stack);

	@Shadow
	@Final
	private TagKey<Item> match;

	@Inject(method = "canEnchant", at = @At(value = "HEAD"), cancellable = true)
	public void canEnchant(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		boolean hasSwordEnchants = false;
		if (stack.getItem() instanceof WeaponWithType weaponWithType)
			hasSwordEnchants = weaponWithType.getWeaponType().hasSwordEnchants();
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(stack.getItem())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(stack.getItem());
			if (configurableItemData.hasSwordEnchants != null)
				hasSwordEnchants = configurableItemData.hasSwordEnchants;
		}
		if (match.equals(ItemTags.WEAPON_ENCHANTABLE) && hasSwordEnchants)
			cir.setReturnValue(true);
	}

	@Override
	public boolean isAcceptibleConditions(ItemStack stack) {
		boolean hasSwordEnchants = false;
		if (stack.getItem() instanceof WeaponWithType weaponWithType)
			hasSwordEnchants = weaponWithType.getWeaponType().hasSwordEnchants();
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(stack.getItem())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(stack.getItem());
			if (configurableItemData.hasSwordEnchants != null)
				hasSwordEnchants = configurableItemData.hasSwordEnchants;
		}
		return stack.is(match) || (match.equals(ItemTags.WEAPON_ENCHANTABLE) && hasSwordEnchants);
	}

	@Override
	public boolean isAcceptibleAnvil(ItemStack stack) {
		boolean hasSwordEnchants = false;
		if (stack.getItem() instanceof WeaponWithType weaponWithType)
			hasSwordEnchants = weaponWithType.getWeaponType().hasSwordEnchants();
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(stack.getItem())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(stack.getItem());
			if (configurableItemData.hasSwordEnchants != null)
				hasSwordEnchants = configurableItemData.hasSwordEnchants;
		}
		return canEnchant(stack) || (match.equals(ItemTags.WEAPON_ENCHANTABLE) && hasSwordEnchants);
	}
}
