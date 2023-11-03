package net.atlas.combatify.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ConfigurableWeaponData;
import net.atlas.combatify.extensions.DefaultedItemExtensions;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.extensions.WeaponWithType;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.mixin.accessors.ItemAccessor;
import net.atlas.combatify.util.BlockingType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(DiggerItem.class)
public abstract class DiggerItemMixin extends TieredItem implements Vanishable, ItemExtensions, DefaultedItemExtensions, WeaponWithType {
	@Shadow
	@Mutable
	private Multimap<Attribute, AttributeModifier> defaultModifiers;
	public DiggerItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}
	@Override
	public void modifyAttributeModifiers() {
		ImmutableMultimap.Builder<Attribute, AttributeModifier> var3 = ImmutableMultimap.builder();
		combatify$getWeaponType().addCombatAttributes(getTier(), var3);
		ImmutableMultimap<Attribute, AttributeModifier> output = var3.build();
		((DefaultedItemExtensions)this).combatify$setDefaultModifiers(output);
	}
	@Redirect(method = "hurtEnemy",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
	public <T extends LivingEntity> void damage(ItemStack instance, int amount, T entity, Consumer<T> breakCallback) {
		boolean bl = instance.getItem() instanceof AxeItem || instance.getItem() instanceof HoeItem;
		if (Combatify.CONFIG.toolsAreWeapons() || bl) {
			amount -= 1;
		}
		instance.hurtAndBreak(amount, entity, breakCallback);
	}

	@Override
	public void combatify$setDefaultModifiers(ImmutableMultimap<Attribute, AttributeModifier> modifiers) {
		defaultModifiers = modifiers;
	}

	@Override
	public void combatify$setStackSize(int stackSize) {
		((ItemAccessor) this).combatify$setMaxStackSize(stackSize);
	}

	@Override
	public WeaponType combatify$getWeaponType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			WeaponType type = Combatify.ITEMS.configuredItems.get(this).type;
			if (type != null)
				return type;
		}
		return WeaponType.PICKAXE;
	}
	@Override
	public double combatify$getChargedAttackBonus() {
		Item item = this;
		double chargedBonus = combatify$getWeaponType().getChargedReach();
		if(Combatify.ITEMS.configuredItems.containsKey(item)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
			if (configurableItemData.chargedReach != null)
				chargedBonus = configurableItemData.chargedReach;
		}
		return chargedBonus;
	}

	@Override
	public BlockingType combatify$getBlockingType() {
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(this);
			if (configurableItemData.blockingType != null) {
				return configurableItemData.blockingType;
			}
		}
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(combatify$getWeaponType())) {
			ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(combatify$getWeaponType());
			if (configurableWeaponData.blockingType != null) {
				return configurableWeaponData.blockingType;
			}
		}
		return Combatify.EMPTY;
	}
}
