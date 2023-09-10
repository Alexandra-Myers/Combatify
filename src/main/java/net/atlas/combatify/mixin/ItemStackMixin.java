package net.atlas.combatify.mixin;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.extensions.PiercingItem;
import net.atlas.combatify.item.NewAttributes;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.enchantment.PiercingEnchantment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DecimalFormat;
import java.util.*;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Shadow
	public abstract Item getItem();
	@Shadow
	@Final
	public static DecimalFormat ATTRIBUTE_MODIFIER_FORMAT;

	@ModifyExpressionValue(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;isEmpty()Z"))
	public boolean preventOutcome(boolean original, @Local(ordinal = 0) Player player, @Local(ordinal = 0) List<Component> list, @Local(ordinal = 0) Multimap<Attribute, AttributeModifier> multimap, @Local(ordinal = 0) EquipmentSlot equipmentSlot) {
		if (!original) {
			boolean attackReach = Combatify.CONFIG.attackReach();
			list.add(CommonComponents.EMPTY);
			list.add(Component.translatable("item.modifiers." + equipmentSlot.getName()).withStyle(ChatFormatting.GRAY));

			for (Map.Entry<Attribute, AttributeModifier> entry : multimap.entries()) {
				AttributeModifier attributeModifier = entry.getValue();
				double d = attributeModifier.getAmount();
				boolean bl = false;
				if (player != null) {
					if (attributeModifier.getId() == WeaponType.BASE_ATTACK_DAMAGE_UUID || attributeModifier.getId() == Item.BASE_ATTACK_DAMAGE_UUID) {
						d += Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE)).getBaseValue();
						d += EnchantmentHelper.getDamageBonus((ItemStack) (Object) this, player.getMobType());
						bl = true;
					} else if (attributeModifier.getId() == WeaponType.BASE_ATTACK_SPEED_UUID || attributeModifier.getId() == Item.BASE_ATTACK_SPEED_UUID) {
						d += Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_SPEED)).getBaseValue() - 1.5;
						bl = true;
					} else if (attributeModifier.getId() == WeaponType.BASE_ATTACK_REACH_UUID) {
						d += Objects.requireNonNull(player.getAttribute(NewAttributes.ATTACK_REACH)).getBaseValue() + (attackReach ? 0 : 0.5);
						bl = true;
					} else if (entry.getKey().equals(Attributes.KNOCKBACK_RESISTANCE)) {
						d += Objects.requireNonNull(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE)).getBaseValue();
					}
				}

				double e;
				if (attributeModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE
						|| attributeModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
					e = d * 100.0;
				} else if (entry.getKey().equals(Attributes.KNOCKBACK_RESISTANCE)) {
					e = d * 10.0;
				} else {
					e = d;
				}

				if (attributeModifier.getId() == WeaponType.BASE_ATTACK_REACH_UUID || attributeModifier.getId() == WeaponType.BASE_ATTACK_SPEED_UUID || attributeModifier.getId() == Item.BASE_ATTACK_SPEED_UUID || attributeModifier.getId() == WeaponType.BASE_ATTACK_DAMAGE_UUID || attributeModifier.getId() == Item.BASE_ATTACK_DAMAGE_UUID || bl) {
					list.add(
							Component.literal(" ")
									.append(
											Component.translatable(
													"attribute.modifier.equals." + attributeModifier.getOperation().toValue(),
													ATTRIBUTE_MODIFIER_FORMAT.format(e),
													Component.translatable(entry.getKey().getDescriptionId())
											)
									)
									.withStyle(ChatFormatting.DARK_GREEN)
					);
				} else if (d > 0.0) {
					list.add(
							Component.translatable(
											"attribute.modifier.plus." + attributeModifier.getOperation().toValue(),
											ATTRIBUTE_MODIFIER_FORMAT.format(e),
											Component.translatable(entry.getKey().getDescriptionId())
									)
									.withStyle(ChatFormatting.BLUE)
					);
				} else if (d < 0.0) {
					e *= -1.0;
					list.add(
							Component.translatable(
											"attribute.modifier.take." + attributeModifier.getOperation().toValue(),
											ATTRIBUTE_MODIFIER_FORMAT.format(e),
											Component.translatable(entry.getKey().getDescriptionId())
									)
									.withStyle(ChatFormatting.RED)
					);
				}
			}
			double piercingLevel = 0;
			if(Combatify.CONFIG.piercer()) {
				piercingLevel = EnchantmentHelper.getItemEnchantmentLevel(PiercingEnchantment.PIERCER, (ItemStack) (Object) this) * 0.1;
			}
			if(getItem() instanceof PiercingItem item) {
				piercingLevel += item.getPiercingLevel();
			}
			if (piercingLevel > 0) {
				list.add(
					Component.literal(" ")
						.append(
							Component.translatable(
								"attribute.modifier.equals." + AttributeModifier.Operation.MULTIPLY_TOTAL.toValue(),
								ATTRIBUTE_MODIFIER_FORMAT.format(piercingLevel * 100),
								Component.translatable("attribute.name.generic.longsword_piercing")
							)
						)
						.withStyle(ChatFormatting.DARK_GREEN)
				);
			}
		}
		return true;
	}
	@ModifyReturnValue(method = "useOn", at = @At(value = "RETURN"))
	public InteractionResult addBlockAbility(InteractionResult original, @Local(ordinal = 0) UseOnContext useOnContext) {
		InteractionResultHolder<ItemStack> holder = null;
		Item item = Objects.requireNonNull(useOnContext.getPlayer()).getItemInHand(useOnContext.getHand()).getItem();
		if (!((ItemExtensions)item).getBlockingType().isEmpty() && original == InteractionResult.PASS) {
			holder = ((ItemExtensions)item).getBlockingType().use(useOnContext.getLevel(), useOnContext.getPlayer(), useOnContext.getHand());
		}
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(item)) {
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
			holder = ((ItemExtensions)item).getBlockingType().use(world, player, hand);
		}
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(item)) {
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
		if (original == UseAnim.NONE) {
			if (!((ItemExtensions)getItem()).getBlockingType().isEmpty()) {
				return UseAnim.BLOCK;
			}
		}
		return original;
	}
}

