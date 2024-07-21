package net.atlas.combatify.mixin;

import net.atlas.combatify.extensions.WeaponWithType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DiggerItem.class)
public abstract class DiggerItemMixin extends TieredItem implements WeaponWithType {
	public DiggerItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}
	@Redirect(method = "postHurtEnemy",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"))
	public void damage(ItemStack instance, int amount, LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
		boolean bl = instance.getItem() instanceof AxeItem || instance.getItem() instanceof HoeItem;
		if (bl)
			amount -= 1;
		instance.hurtAndBreak(amount, livingEntity, equipmentSlot);
	}

	@Override
	public Item self() {
		return this;
	}
}
