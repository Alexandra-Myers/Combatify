package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.config.ConfigHelper;
import net.alexandra.atlas.atlas_combat.item.KnifeItem;
import net.alexandra.atlas.atlas_combat.item.LongSwordItem;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;

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
			cir.setReturnValue(stack.getItem() instanceof AxeItem || stack.getItem() instanceof SwordItem || stack.getItem() instanceof KnifeItem);
			cir.cancel();
		}
		if(thisEnchantment instanceof LootBonusEnchantment
				|| thisEnchantment instanceof FireAspectEnchantment) {
			cir.setReturnValue(stack.getItem() instanceof TieredItem);
			cir.cancel();
		}
		if((thisEnchantment instanceof KnockbackEnchantment
				|| thisEnchantment instanceof SweepingEdgeEnchantment) && ConfigHelper.toolsAreWeapons) {
			cir.setReturnValue(stack.getItem() instanceof TieredItem);
			cir.cancel();
		}
		if(thisEnchantment instanceof DamageEnchantment) {
			cir.setReturnValue(stack.getItem() instanceof SwordItem || stack.getItem() instanceof KnifeItem || stack.getItem() instanceof LongSwordItem);
			cir.cancel();
		}
	}
}
