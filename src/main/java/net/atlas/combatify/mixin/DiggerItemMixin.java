package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.atlas.combatify.Combatify;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DiggerItem.class)
public abstract class DiggerItemMixin extends Item {
	public DiggerItemMixin(Properties properties) {
		super(properties);
	}

	@WrapOperation(method = "postHurtEnemy",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"))
	public void damage(ItemStack instance, int amount, LivingEntity livingEntity, EquipmentSlot equipmentSlot, Operation<Void> original) {
		boolean bl = instance.getItem() instanceof AxeItem;
		if (bl && Combatify.CONFIG.axesAreWeapons())
			amount -= 1;
		original.call(instance, amount, livingEntity, equipmentSlot);
	}
}
