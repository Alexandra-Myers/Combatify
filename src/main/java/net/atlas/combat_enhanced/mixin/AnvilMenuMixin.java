package net.atlas.combat_enhanced.mixin;

import net.atlas.combat_enhanced.extensions.CustomEnchantment;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {
	@Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"))
	public boolean redirectCheck(Enchantment instance, ItemStack stack) {
		return instance instanceof CustomEnchantment customEnchantment ? customEnchantment.isAcceptibleAnvil(stack) : instance.canEnchant(stack);
	}
}
