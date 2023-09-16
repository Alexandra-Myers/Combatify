package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.CustomEnchantment;
import net.atlas.combatify.item.KnifeItem;
import net.atlas.combatify.item.LongSwordItem;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin implements CustomEnchantment {
	@Shadow
	public abstract boolean canEnchant(ItemStack stack);

	@Shadow
	@Final
	public EnchantmentCategory category;

	@Shadow
	public abstract boolean canApplyAtEnchantingTable(ItemStack stack);

	@Unique
	public Enchantment thisEnchantment = Enchantment.class.cast(this);

	@Inject(method = "canEnchant", at = @At(value = "HEAD"), cancellable = true)
	public void canEnchant(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if(thisEnchantment instanceof SweepingEdgeEnchantment) {
			cir.setReturnValue(stack.getItem() instanceof TieredItem);
		}
	}

	@Override
	public boolean isAcceptibleConditions(ItemStack stack) {
		if(thisEnchantment instanceof SweepingEdgeEnchantment && !Combatify.CONFIG.toolsAreWeapons.get()) {
			return stack.getItem() instanceof AxeItem || stack.getItem() instanceof KnifeItem || stack.getItem() instanceof LongSwordItem || category.canEnchant(stack.getItem()) || canApplyAtEnchantingTable(stack);
		} else if(thisEnchantment instanceof SweepingEdgeEnchantment) {
			return canEnchant(stack);
		}
		if(thisEnchantment instanceof DamageEnchantment) {
			return stack.getItem() instanceof SwordItem || stack.getItem() instanceof KnifeItem || stack.getItem() instanceof LongSwordItem || category.canEnchant(stack.getItem()) || canApplyAtEnchantingTable(stack);
		}
		return category.canEnchant(stack.getItem());
	}

	@Override
	public boolean isAcceptibleAnvil(ItemStack stack) {
		if(thisEnchantment instanceof SweepingEdgeEnchantment && Combatify.CONFIG.toolsAreWeapons.get()) {
			return canEnchant(stack);
		} else if(thisEnchantment instanceof SweepingEdgeEnchantment) {
			return stack.getItem() instanceof AxeItem || stack.getItem() instanceof KnifeItem || stack.getItem() instanceof LongSwordItem || category.canEnchant(stack.getItem()) || canApplyAtEnchantingTable(stack);
		}
		if(thisEnchantment instanceof DamageEnchantment) {
			return stack.getItem() instanceof SwordItem || stack.getItem() instanceof KnifeItem || stack.getItem() instanceof LongSwordItem || canEnchant(stack);
		}
		return canEnchant(stack);
	}
}
