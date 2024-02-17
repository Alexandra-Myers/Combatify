package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.extensions.WeaponWithType;
import net.atlas.combatify.item.WeaponType;
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
public abstract class EnchantmentMixin {
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
			WeaponType type;
			if ((type = configurableItemData.type) != null)
				hasSwordEnchants = type.hasSwordEnchants();
			if (configurableItemData.hasSwordEnchants != null)
				hasSwordEnchants = configurableItemData.hasSwordEnchants;
		}
		if ((match.equals(ItemTags.SWORD_ENCHANTABLE) || match.equals(ItemTags.WEAPON_ENCHANTABLE)) && hasSwordEnchants)
			cir.setReturnValue(true);
	}
}
