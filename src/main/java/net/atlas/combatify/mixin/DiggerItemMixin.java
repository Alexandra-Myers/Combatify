package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ConfigurableWeaponData;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.extensions.WeaponWithType;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DiggerItem.class)
public abstract class DiggerItemMixin extends TieredItem implements ItemExtensions, WeaponWithType {
	public DiggerItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}
	@Override
	public ItemAttributeModifiers modifyAttributeModifiers() {
		if (getWeaponType().isEmpty() || !Combatify.CONFIG.weaponTypesEnabled())
			return null;
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		getWeaponType().addCombatAttributes(getTier(), builder);
		return builder.build();
	}
	@Redirect(method = "hurtEnemy",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"))
	public <T extends LivingEntity> void damage(ItemStack instance, int amount, LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
		boolean bl = instance.getItem() instanceof AxeItem || instance.getItem() instanceof HoeItem;
		if (bl)
			amount -= 1;
		instance.hurtAndBreak(amount, livingEntity, equipmentSlot);
	}

	@Override
	public void setStackSize(int stackSize) {
		this.maxStackSize = stackSize;
	}

	@Override
	public WeaponType getWeaponType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			WeaponType type = Combatify.ITEMS.configuredItems.get(this).type;
			if (type != null)
				return type;
		}
		return WeaponType.EMPTY;
	}
	@Override
	public double getChargedAttackBonus() {
		Item item = this;
		double chargedBonus = getWeaponType().getChargedReach();
		if(Combatify.ITEMS.configuredItems.containsKey(item)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
			if (configurableItemData.chargedReach != null)
				chargedBonus = configurableItemData.chargedReach;
		}
		return chargedBonus;
	}

	@Override
	public boolean canSweep() {
		Item item = this;
		boolean canSweep = getWeaponType().canSweep();
		if(Combatify.ITEMS.configuredItems.containsKey(item)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
			if (configurableItemData.canSweep != null)
				canSweep = configurableItemData.canSweep;
		}
		return canSweep;
	}

	@Override
	public BlockingType getBlockingType() {
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(this);
			if (configurableItemData.blockingType != null)
				return configurableItemData.blockingType;
		}
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(getWeaponType())) {
			ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(getWeaponType());
			if (configurableWeaponData.blockingType != null)
				return configurableWeaponData.blockingType;
		}
		return Combatify.EMPTY;
	}

	@Override
	public double getPiercingLevel() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(this);
			if (configurableItemData.piercingLevel != null)
				return configurableItemData.piercingLevel;
		}
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(getWeaponType())) {
			ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(getWeaponType());
			if (configurableWeaponData.piercingLevel != null)
				return configurableWeaponData.piercingLevel;
		}
		return 0;
	}
}
