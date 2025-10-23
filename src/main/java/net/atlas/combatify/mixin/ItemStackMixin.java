package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.item.NewAttributes;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.enchantment.PiercingEnchantment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.DecimalFormat;
import java.util.*;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Shadow
	public abstract Item getItem();
	@Shadow
	@Final
	public static DecimalFormat ATTRIBUTE_MODIFIER_FORMAT;

	@Shadow
	public abstract boolean isEnchanted();

	@Shadow
	protected abstract int getHideFlags();

	@Shadow
	private static boolean shouldShowInTooltip(int i, ItemStack.TooltipPart tooltipPart) {
		return false;
	}

	@Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hasTag()Z", ordinal = 0))
	public void addHoverText(@Nullable Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir, @Local(ordinal = 0) List<Component> tooltip) {
		ItemExtensions item = (ItemExtensions) getItem();
		if (!item.getBlockingType().isEmpty()) {
			if (!item.getBlockingType().requiresSwordBlocking() || Combatify.CONFIG.swordBlocking()) {
				item.getBlockingType().appendTooltips(ItemStack.class.cast(this), tooltip::add);
			}
		}
	}

	@Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;getOperation()Lnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;", ordinal = 0))
	public void addAttackReach(Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir, @Local(ordinal = 0) AttributeModifier attributeModifier, @Local(ordinal = 0) LocalDoubleRef d, @Local(ordinal = 0) LocalBooleanRef bl) {
		if (player != null) {
			if (attributeModifier.getId().equals(WeaponType.BASE_ATTACK_SPEED_CTS_UUID)) {
				d.set(d.get() + player.getAttributeBaseValue(Attributes.ATTACK_SPEED) - 1.5);
				bl.set(true);
			}
			if (attributeModifier.getId().equals(WeaponType.BASE_ATTACK_REACH_UUID)) {
				d.set(d.get() + player.getAttributeBaseValue(NewAttributes.ATTACK_REACH) + (Combatify.CONFIG.attackReach() ? 0 : 0.5));
				bl.set(true);
			}
		}
	}
	@Inject(method = "getTooltipLines", slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/item/ItemStack$TooltipPart;MODIFIERS : Lnet/minecraft/world/item/ItemStack$TooltipPart;")), at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shouldShowInTooltip(ILnet/minecraft/world/item/ItemStack$TooltipPart;)Z", ordinal = 0))
	public void addPiercing(Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir, @Local(ordinal = 0) List<Component> list) {
		if (shouldShowInTooltip(getHideFlags(), ItemStack.TooltipPart.MODIFIERS)) {
			double piercingLevel = 0;
			if (Combatify.CONFIG.piercer())
				piercingLevel = EnchantmentHelper.getItemEnchantmentLevel(PiercingEnchantment.PIERCER, (ItemStack) (Object) this) * 0.1;
			piercingLevel += ((ItemExtensions) getItem()).getPiercingLevel();
			if (piercingLevel > 0) {
				piercingLevel = Math.min(piercingLevel, 1);
				list.add(CommonComponents.EMPTY);
				list.add(Component.translatable("item.modifiers.mainhand").withStyle(ChatFormatting.GRAY));
				list.add(
					Component.literal(" ")
						.append(
							Component.translatable(
								"attribute.modifier.equals." + AttributeModifier.Operation.MULTIPLY_TOTAL.toValue(),
								ATTRIBUTE_MODIFIER_FORMAT.format(piercingLevel * 100),
								Component.translatable("attribute.name.generic.armor_piercing")
							)
						)
						.withStyle(ChatFormatting.DARK_GREEN)
				);
			}
		}
	}
	@ModifyReturnValue(method = "getUseDuration", at = @At(value = "RETURN"))
	public int getUseDuration(int original) {
		Item item = getItem();
		if (Combatify.ITEMS.configuredItems.containsKey(item)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
			if (configurableItemData.useDuration != null)
				return configurableItemData.useDuration;
		} else if (!((ItemExtensions) item).getBlockingType().isEmpty() && (!((ItemExtensions) item).getBlockingType().requiresSwordBlocking() || Combatify.CONFIG.swordBlocking()))
			return 72000;
		return original;
	}

	@ModifyReturnValue(method = "isEnchantable", at = @At(value = "RETURN"))
	public boolean addEnchantability(boolean original) {
		boolean enchantable = false;
		Item item = getItem();
		if (Combatify.ITEMS.configuredItems.containsKey(item)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
			if (configurableItemData.isEnchantable != null)
				enchantable = configurableItemData.isEnchantable && !isEnchanted();
		}
		return enchantable || original;
	}
	@ModifyReturnValue(method = "useOn", at = @At(value = "RETURN"))
	public InteractionResult addBlockAbility(InteractionResult original, @Local(ordinal = 0) UseOnContext useOnContext) {
		InteractionResultHolder<ItemStack> holder = null;
		Item item = Objects.requireNonNull(useOnContext.getPlayer()).getItemInHand(useOnContext.getHand()).getItem();
		if (!((ItemExtensions)item).getBlockingType().isEmpty() && original == InteractionResult.PASS) {
			if(((ItemExtensions) item).getBlockingType().canUse(useOnContext.getLevel(), useOnContext.getPlayer(), useOnContext.getHand())) {
				holder = ((ItemExtensions)item).getBlockingType().use(useOnContext.getLevel(), useOnContext.getPlayer(), useOnContext.getHand());
			}
		}
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(item) && original != InteractionResult.PASS && original != InteractionResult.FAIL) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
			if (configurableItemData.cooldown != null && !configurableItemData.cooldownAfter) {
				Objects.requireNonNull(useOnContext.getPlayer()).getCooldowns().addCooldown(item, configurableItemData.cooldown);
			}
		}
		if (holder != null) {
			return holder.getResult();
		}
		return original;
	}

	@ModifyReturnValue(method = "use", at = @At(value = "RETURN"))
	public InteractionResultHolder<ItemStack> addBlockAbility(InteractionResultHolder<ItemStack> original, @Local(ordinal = 0) Level world, @Local(ordinal = 0) Player player, @Local(ordinal = 0) InteractionHand hand) {
		InteractionResultHolder<ItemStack> holder = null;
		Item item = player.getItemInHand(hand).getItem();
		if (!((ItemExtensions)item).getBlockingType().isEmpty() && original.getResult() == InteractionResult.PASS) {
			if(((ItemExtensions) item).getBlockingType().canUse(world, player, hand)) {
				holder = ((ItemExtensions)item).getBlockingType().use(world, player, hand);
			}
		}
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(item) && original.getResult() != InteractionResult.PASS && original.getResult() != InteractionResult.FAIL) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
			if (configurableItemData.cooldown != null && !configurableItemData.cooldownAfter) {
				player.getCooldowns().addCooldown(item, configurableItemData.cooldown);
			}
		}
		if (holder != null) {
			return holder;
		}
		return original;
	}
	@Inject(method = "releaseUsing", at = @At(value = "TAIL"))
	public void addCooldown(Level level, LivingEntity livingEntity, int i, CallbackInfo ci) {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(getItem())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(getItem());
			if (configurableItemData.cooldown != null && configurableItemData.cooldownAfter && livingEntity instanceof Player player) {
				player.getCooldowns().addCooldown(getItem(), configurableItemData.cooldown);
			}
		}
	}
	@ModifyReturnValue(method = "getUseAnimation", at = @At(value = "RETURN"))
	public UseAnim addBlockAnim(UseAnim original) {
		if (original == UseAnim.NONE)
			if (!((ItemExtensions)getItem()).getBlockingType().isEmpty() && (!((ItemExtensions)getItem()).getBlockingType().requiresSwordBlocking() || Combatify.CONFIG.swordBlocking()))
				return UseAnim.BLOCK;
		return original;
	}
}

