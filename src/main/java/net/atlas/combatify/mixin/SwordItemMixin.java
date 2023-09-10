package net.atlas.combatify.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.atlas.combatify.extensions.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SwordItem.class)
public class SwordItemMixin extends TieredItem implements ItemExtensions, DefaultedItemExtensions, WeaponWithType {
	@Shadow
	private Multimap<Attribute, AttributeModifier> defaultModifiers;

	public SwordItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
		if(Combatify.CONFIG.swordBlocking()) {
			float f = getBlockingType().getShieldBlockDamageValue(stack);
			double g = getBlockingType().getShieldKnockbackResistanceValue(stack);
			if(!getBlockingType().isPercentage())
				tooltip.add((Component.literal("")).append(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADDITION.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(f), Component.translatable("attribute.name.generic.shield_strength"))).withStyle(ChatFormatting.DARK_GREEN));
			else
				tooltip.add((Component.literal("")).append(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.MULTIPLY_TOTAL.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((double) f * 100), Component.translatable("attribute.name.generic.sword_block_strength"))).withStyle(ChatFormatting.DARK_GREEN));
			if (g > 0.0) {
				tooltip.add((Component.literal("")).append(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADDITION.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(g * 10.0), Component.translatable("attribute.name.generic.knockback_resistance"))).withStyle(ChatFormatting.DARK_GREEN));
			}
		}
		super.appendHoverText(stack, world, tooltip, context);
	}
	@Override
	public void modifyAttributeModifiers() {
		ImmutableMultimap.Builder<Attribute, AttributeModifier> var3 = ImmutableMultimap.builder();
		getWeaponType().addCombatAttributes(getTier(), var3);
		ImmutableMultimap<Attribute, AttributeModifier> output = var3.build();
		((DefaultedItemExtensions)this).setDefaultModifiers(output);
	}

	@Inject(method = "getDamage", at = @At(value = "RETURN"), cancellable = true)
	public void getDamage(CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue((float) getWeaponType().getDamage(getTier()));
	}

	@Override
	public void setStackSize(int stackSize) {
		this.maxStackSize = stackSize;
	}

	@Override
	public BlockingType getBlockingType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(this);
			if (configurableItemData.blockingType != null) {
				return configurableItemData.blockingType;
			}
		}
		return BlockingType.SWORD;
	}

	@Override
	public void setDefaultModifiers(ImmutableMultimap<Attribute, AttributeModifier> modifiers) {
		defaultModifiers = modifiers;
	}

	@Override
	public WeaponType getWeaponType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			WeaponType type = Combatify.ITEMS.configuredItems.get(this).type;
			if (type != null)
				return type;
		}
		return WeaponType.SWORD;
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
}
