package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.extensions.ItemExtensions;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ElytraItem.class)
public class ElytraItemMixin extends Item implements ItemExtensions {
	public ElytraItemMixin(Properties properties) {
		super(properties);
	}

	@ModifyReturnValue(method = "isValidRepairItem", at = @At(value = "RETURN"))
	public boolean canRepair(boolean original, @Local(ordinal = 1, argsOnly = true) ItemStack stack) {
		return original || canRepairThroughConfig(stack);
	}

	@Override
	public Item combatify$self() {
		return this;
	}
}
