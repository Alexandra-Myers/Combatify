package net.alexandra.atlas.atlas_combat.mixin;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.alexandra.atlas.atlas_combat.extensions.IShieldItem;
import net.alexandra.atlas.atlas_combat.util.BlockingType;
import net.alexandra.atlas.atlas_combat.util.ShieldUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
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
        list.add((Component.literal("")).append(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADDITION.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(f), Component.translatable("attribute.name.generic.shield_strength"))).withStyle(ChatFormatting.DARK_GREEN));
        list.add((Component.literal("")).append(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADDITION.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(g * 10.0F), Component.translatable("attribute.name.generic.knockback_resistance"))).withStyle(ChatFormatting.DARK_GREEN));
		ci.cancel();
    }

	@Override
    public float getShieldKnockbackResistanceValue(ItemStack itemStack) {
        return itemStack.getTagElement("BlockEntityTag") != null ? 0.8F : 0.5F;
    }

	@Override
    public float getShieldBlockDamageValue(ItemStack itemStack) {
        return itemStack.getTagElement("BlockEntityTag") != null ? 10.0F : 5.0F;
    }

	@Override
	public void block(LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl) {
		if (instance instanceof Player player && player.getCooldowns().isOnCooldown(blockingItem.getItem()))
			return;
		float blockStrength = ShieldUtils.getShieldBlockDamageValue(blockingItem);
		g.set(Math.min(blockStrength, amount.get()));
		if (!source.isProjectile() && !source.isExplosion()) {
			entity = source.getDirectEntity();
			if (entity instanceof LivingEntity) {
				instance.blockUsingShield((LivingEntity) entity);
			}
		} else {
			g.set(amount.get());
		}

		instance.hurtCurrentlyUsedShield(g.get());
		amount.set(amount.get() - g.get());
		bl.set(true);
	}

	@Override
	public BlockingType getBlockingType() {
		return BlockingType.SHIELD;
	}
}
