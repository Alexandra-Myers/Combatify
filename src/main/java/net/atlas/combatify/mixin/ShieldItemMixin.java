package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.util.BlockingType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ShieldItem.class)
public class ShieldItemMixin extends Item implements ItemExtensions {


    public ShieldItemMixin(Properties properties) {
        super(properties);
    }


    @Inject(method = "appendHoverText", at = @At("HEAD"),cancellable = true)
    public void appendText(ItemStack itemStack, Level level, List<Component> list, TooltipFlag tooltipFlag, CallbackInfo ci)
    {
        BannerItem.appendHoverTextFromBannerBlockEntityTag(itemStack, list);
        float f = getBlockingType().getShieldBlockDamageValue(itemStack);
        double g = getBlockingType().getShieldKnockbackResistanceValue(itemStack);
		if(!getBlockingType().isPercentage())
        	list.add((Component.literal("")).append(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADDITION.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(f), Component.translatable("attribute.name.generic.shield_strength"))).withStyle(ChatFormatting.DARK_GREEN));
		else
			list.add((Component.literal("")).append(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.MULTIPLY_TOTAL.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((double) f * 100), Component.translatable("attribute.name.generic.sword_block_strength"))).withStyle(ChatFormatting.DARK_GREEN));
        list.add((Component.literal("")).append(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADDITION.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(g * 10.0), Component.translatable("attribute.name.generic.knockback_resistance"))).withStyle(ChatFormatting.DARK_GREEN));
		ci.cancel();
    }

	@Override
	public void setStackSize(int stackSize) {
		maxStackSize = stackSize;
	}

	@Override
	public double getChargedAttackBonus() {
		double chargedBonus = 1.0;
		if(Combatify.ITEMS.configuredItems.containsKey(this)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(this);
			if (configurableItemData.type != null)
				if (Combatify.ITEMS.configuredWeapons.containsKey(configurableItemData.type))
					if (Combatify.ITEMS.configuredWeapons.get(configurableItemData.type).chargedReach != null)
						chargedBonus = Combatify.ITEMS.configuredWeapons.get(configurableItemData.type).chargedReach;
			if (configurableItemData.chargedReach != null)
				chargedBonus = configurableItemData.chargedReach;
		}
		return chargedBonus;
	}

	@Override
	public BlockingType getBlockingType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(this);
			if (configurableItemData.blockingType != null) {
				return configurableItemData.blockingType;
			}
		}
		return BlockingType.SHIELD;
	}
}
