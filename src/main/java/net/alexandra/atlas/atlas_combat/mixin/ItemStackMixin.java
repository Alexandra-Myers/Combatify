package net.alexandra.atlas.atlas_combat.mixin;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.IItemStack;
import net.alexandra.atlas.atlas_combat.item.NewAttributes;
import net.alexandra.atlas.atlas_combat.item.WeaponType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.text.DecimalFormat;
import java.util.*;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements IItemStack {

	@Unique
	List<Component> list;
	@Unique
	Player player;
	@Unique
	Multimap<Attribute, AttributeModifier> multimap;
	@Unique
	EquipmentSlot equipmentSlot;
	@Shadow
	public abstract Item getItem();
	@Shadow
	@Final
	public static DecimalFormat ATTRIBUTE_MODIFIER_FORMAT;
	@Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;isEmpty()Z"), locals = LocalCapture.CAPTURE_FAILHARD)
	public void extractLines(Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir, List<Component> list, MutableComponent mutablecomponent, int j, EquipmentSlot[] var6, int var7, int var8, EquipmentSlot equipmentslot, Multimap<Attribute, AttributeModifier> multimap) {
		this.list = list;
		this.player = player;
		this.multimap = multimap;
		this.equipmentSlot = equipmentslot;
	}

	@ModifyExpressionValue(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;isEmpty()Z"))
	public boolean preventOutcome(boolean original) {
		if (!original) {
			boolean attackReach = AtlasCombat.CONFIG.attackReach();
			list.add(CommonComponents.EMPTY);
			list.add(Component.translatable("item.modifiers." + equipmentSlot.getName()).withStyle(ChatFormatting.GRAY));

			for (Map.Entry<Attribute, AttributeModifier> entry : multimap.entries()) {
				AttributeModifier attributeModifier = entry.getValue();
				double d = attributeModifier.getAmount();
				boolean bl = false;
				if (player != null) {
					if (attributeModifier.getId() == WeaponType.BASE_ATTACK_DAMAGE_UUID || attributeModifier.getId() == Item.BASE_ATTACK_DAMAGE_UUID) {
						d += player.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
						d += EnchantmentHelper.getDamageBonus((ItemStack) (Object) this, player.getMobType());
						bl = true;
					} else if (attributeModifier.getId() == WeaponType.BASE_ATTACK_SPEED_UUID || attributeModifier.getId() == Item.BASE_ATTACK_SPEED_UUID) {
						d += player.getAttribute(Attributes.ATTACK_SPEED).getBaseValue() - 1.5;
						bl = true;
					} else if (attributeModifier.getId() == WeaponType.BASE_ATTACK_REACH_UUID) {
						d += player.getAttribute(NewAttributes.ATTACK_REACH).getBaseValue() + (attackReach ? 2.5 : 3);
						bl = true;
					} else if (attributeModifier.getId() == WeaponType.BASE_BLOCK_REACH_UUID) {
						d += player.getAttribute(NewAttributes.BLOCK_REACH).getBaseValue() + 6.0;
						bl = true;
					} else if (entry.getKey().equals(Attributes.KNOCKBACK_RESISTANCE)) {
						d += player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getBaseValue();
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

				if (attributeModifier.getId() == WeaponType.BASE_BLOCK_REACH_UUID || attributeModifier.getId() == WeaponType.BASE_ATTACK_REACH_UUID || attributeModifier.getId() == WeaponType.BASE_ATTACK_SPEED_UUID || attributeModifier.getId() == Item.BASE_ATTACK_SPEED_UUID || attributeModifier.getId() == WeaponType.BASE_ATTACK_DAMAGE_UUID || attributeModifier.getId() == Item.BASE_ATTACK_DAMAGE_UUID || bl) {
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
		}
		return true;
	}
	@Override
	public int getEnchantmentLevel(Enchantment enchantment) {
		Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments((ItemStack) (Object) this);
		return map.get(enchantment);
	}
}

