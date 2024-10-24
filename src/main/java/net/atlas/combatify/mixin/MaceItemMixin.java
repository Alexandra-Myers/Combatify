package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.WeaponWithType;
import net.atlas.combatify.item.WeaponType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MaceItem.class)
public class MaceItemMixin extends Item implements WeaponWithType {

	public MaceItemMixin(Properties properties) {
		super(properties);
	}

	@Override
	public WeaponType getWeaponType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			WeaponType type = Combatify.ITEMS.configuredItems.get(this).type;
			if (type != null)
				return type;
		}
		return WeaponType.MACE;
	}

	@ModifyReturnValue(method = "isValidRepairItem", at = @At(value = "RETURN"))
	public boolean canRepair(boolean original, @Local(ordinal = 1, argsOnly = true) ItemStack stack) {
		return original || canRepairThroughConfig(stack);
	}

	@Override
	public Item self() {
		return this;
	}
}
