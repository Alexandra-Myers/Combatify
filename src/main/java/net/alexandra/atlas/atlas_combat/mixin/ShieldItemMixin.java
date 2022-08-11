package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.IShieldItem;
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
public class ShieldItemMixin extends Item implements IShieldItem {


    public ShieldItemMixin(Properties properties) {
        super(properties);
    }


    @Inject(method = "appendHoverText", at = @At("HEAD"),cancellable = true)
    public void appendText(ItemStack itemStack, Level level, List<Component> list, TooltipFlag tooltipFlag, CallbackInfo ci)
    {
        BannerItem.appendHoverTextFromBannerBlockEntityTag(itemStack, list);
        float f = getShieldBlockDamageValue(itemStack);
        float g = getShieldKnockbackResistanceValue(itemStack);
        list.add((Component.literal("")).append(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADDITION.toValue(), new Object[]{ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((double)f), Component.translatable("attribute.name.generic.shield_strength")})).withStyle(ChatFormatting.DARK_GREEN));
        list.add((Component.literal("")).append(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADDITION.toValue(), new Object[]{ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((double)(g * 10.0F)), Component.translatable("attribute.name.generic.knockback_resistance")})).withStyle(ChatFormatting.DARK_GREEN));
    }

	@Override
    public float getShieldKnockbackResistanceValue(ItemStack itemStack) {
        return itemStack.getTagElement("BlockEntityTag") != null ? 0.8F : 0.5F;
    }

	@Override
    public float getShieldBlockDamageValue(ItemStack itemStack) {
        return itemStack.getTagElement("BlockEntityTag") != null ? 10.0F : 5.0F;
    }
}
