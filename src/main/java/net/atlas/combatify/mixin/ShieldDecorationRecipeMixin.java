package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.crafting.ShieldDecorationRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShieldDecorationRecipe.class)
public class ShieldDecorationRecipeMixin {
	@ModifyExpressionValue(method = "matches", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
	public boolean makeAllShieldsWork(boolean original, @Local(ordinal = 0) ItemStack shield) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original;
		return shield.getItem() instanceof ShieldItem || original;
	}

	@ModifyExpressionValue(method = "assemble", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
	public boolean makeAllShieldsWork1(boolean original, @Local(ordinal = 2) ItemStack shield) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original;
		return shield.getItem() instanceof ShieldItem || original;
	}
}
