package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
	@Unique
	public Enchantment thisEnchantment = ((Enchantment)(Object)this);

	@Inject(method = "canEnchant", at = @At(value = "HEAD"), cancellable = true)
	public void canEnchant(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if(thisEnchantment instanceof LootBonusEnchantment
				|| thisEnchantment instanceof KnockbackEnchantment
				|| thisEnchantment instanceof FireAspectEnchantment
				|| thisEnchantment instanceof SweepingEdgeEnchantment) {
			cir.setReturnValue(stack.getItem() instanceof AxeItem || stack.getItem() instanceof SwordItem || stack.getItem() instanceof HoeItem);
			cir.cancel();
		}
	}
}
