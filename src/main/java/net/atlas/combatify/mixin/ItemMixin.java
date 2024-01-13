package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ConfigurableWeaponData;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.util.BlockingType;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SuspiciousStewItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemExtensions {

	@Override
	public void setStackSize(int stackSize) {
		((Item) (Object)this).maxStackSize = stackSize;
	}

	@ModifyReturnValue(method = "getUseDuration", at = @At(value = "RETURN"))
	public int getUseDuration(int original, @Local(ordinal = 0) ItemStack stack) {
		if (stack.getItem() instanceof BowlFoodItem || stack.getItem() instanceof SuspiciousStewItem) {
			return Combatify.CONFIG.stewUseDuration();
		} else {
			if (!getBlockingType().isEmpty()) {
				if (!getBlockingType().requiresSwordBlocking() || Combatify.CONFIG.swordBlocking()) {
					return 72000;
                }
			}
		}
		return original;
	}

	@ModifyReturnValue(method = "isEnchantable", at = @At(value = "RETURN"))
	public boolean addEnchantability(boolean original) {
		boolean enchantable = false;
		Item item = Item.class.cast(this);
		if (Combatify.ITEMS.configuredItems.containsKey(item)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
			if (configurableItemData.isEnchantable != null)
				enchantable = configurableItemData.isEnchantable;
		}
		return enchantable || original;
	}

	@Override
	public double getChargedAttackBonus() {
		Item item = Item.class.cast(this);
		double chargedBonus = 1.0;
		if(Combatify.ITEMS.configuredItems.containsKey(item)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
			if (configurableItemData.type != null)
				if (Combatify.ITEMS.configuredWeapons.containsKey(configurableItemData.type))
					if (Combatify.ITEMS.configuredWeapons.get(configurableItemData.type).chargedReach != null)
						chargedBonus = Combatify.ITEMS.configuredWeapons.get(configurableItemData.type).chargedReach;
			if (configurableItemData.chargedReach != null)
				chargedBonus = configurableItemData.chargedReach;
		}
		return chargedBonus;
	}

	@Override
	public BlockingType getBlockingType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(Item.class.cast(this))) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(Item.class.cast(this));
			if (configurableItemData.blockingType != null) {
				return configurableItemData.blockingType;
			}
			if (configurableItemData.type != null && Combatify.ITEMS.configuredWeapons.containsKey(configurableItemData.type)) {
				ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(configurableItemData.type);
				if (configurableWeaponData.blockingType != null) {
					return configurableWeaponData.blockingType;
				}
			}
		}
		return Combatify.EMPTY;
	}
}
