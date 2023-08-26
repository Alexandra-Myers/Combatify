package net.atlas.combatify.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.atlas.combatify.enchantment.DefendingEnchantment;
import net.atlas.combatify.extensions.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SwordItem.class)
public class SwordItemMixin extends TieredItem implements ItemExtensions, IShieldItem, ISwordItem, DefaultedItemExtensions, WeaponWithType {
	@Shadow
	private Multimap<Attribute, AttributeModifier> defaultModifiers;
	public int strengthTimer = 0;

	public SwordItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
		if(Combatify.CONFIG.swordBlocking()) {
			float f = getShieldBlockDamageValue(stack);
			double g = getShieldKnockbackResistanceValue(stack);
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
		cir.setReturnValue(getWeaponType().getDamage(getTier()));
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
		ItemStack itemStack = user.getItemInHand(hand);
		if(Combatify.CONFIG.swordBlocking() && hand != InteractionHand.OFF_HAND) {
			strengthTimer = 0;
			ItemStack oppositeStack = user.getItemInHand(InteractionHand.OFF_HAND);
			if(oppositeStack.isEmpty()) {
				if(user.isSprinting()) {
					user.setSprinting(false);
				}
				user.startUsingItem(hand);
				return InteractionResultHolder.consume(itemStack);
			}
		}
		return InteractionResultHolder.pass(itemStack);
	}

	@Override
	public @NotNull UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BLOCK;
	}

	@Override
	public void setStackSize(int stackSize) {
		this.maxStackSize = stackSize;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000;
	}
	@Override
	public double getShieldKnockbackResistanceValue(ItemStack itemStack) {
		return 0.0;
	}

	@Override
	public float getShieldBlockDamageValue(ItemStack itemStack) {
		Tier var2 = getTier();
		float strengthIncrease = var2.getAttackDamageBonus() <= 1.0F ? -1F : 0.0F;
		strengthIncrease += Combatify.CONFIG.swordProtectionEfficacy();
		strengthIncrease = Math.max(strengthIncrease, -3);
		if(Combatify.CONFIG.defender()) {
			strengthIncrease += EnchantmentHelper.getItemEnchantmentLevel(DefendingEnchantment.DEFENDER, itemStack);
		}
		return Math.min(0.5F + (strengthIncrease * 0.125F), 1);
	}

	@Override
	public void block(LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl) {
		if(instance.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
			boolean blocked = !source.is(DamageTypeTags.IS_EXPLOSION) && !source.is(DamageTypeTags.IS_PROJECTILE);
			if (source.is(DamageTypeTags.IS_EXPLOSION)) {
				g.set(Math.min(amount.get(), 10));
			} else if (blocked) {
				((LivingEntityExtensions)instance).setIsParryTicker(0);
				((LivingEntityExtensions)instance).setIsParry(true);
				float actualStrength = this.getShieldBlockDamageValue(blockingItem);
				g.set(amount.get() * actualStrength);
				entity = source.getDirectEntity();
				if (entity instanceof LivingEntity) {
					instance.blockUsingShield((LivingEntity) entity);
				}
				bl.set(true);
			}

			amount.set(amount.get() - g.get());
		}
	}

	@Override
	public BlockingType getBlockingType() {
		return BlockingType.SWORD;
	}

	@Override
	public void addStrengthTimer() {
		++strengthTimer;
	}

	@Override
	public void setDefaultModifiers(ImmutableMultimap<Attribute, AttributeModifier> modifiers) {
		defaultModifiers = modifiers;
	}

	@Override
	public WeaponType getWeaponType() {
		return WeaponType.SWORD;
	}

	@Override
	public double getChargedAttackBonus() {
		return getWeaponType().getChargedReach();
	}
}
