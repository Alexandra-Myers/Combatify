package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.extensions.CustomEnchantment;
import net.minecraft.server.commands.EnchantCommand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantCommand.class)
public class EnchantCommandMixin {
	@ModifyExpressionValue(method = "enchant", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"))
	private static boolean redirectCanEnchant(boolean original, @Local(ordinal = 0) Enchantment currentEnchantment, @Local(ordinal = 0) ItemStack itemStack) {
		return currentEnchantment instanceof CustomEnchantment customEnchantment && itemStack != null ? customEnchantment.isAcceptibleConditions(itemStack) : original;
	}
}
