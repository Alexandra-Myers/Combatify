package net.atlas.combatify.util;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.enchantment.DefendingEnchantment;
import net.atlas.combatify.extensions.Tierable;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwordBlockingType extends BlockingType {
	public SwordBlockingType(String name) {
		super(name);
	}

	@Override
	public void block(LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl) {
		if(instance.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
			boolean blocked = !source.is(DamageTypeTags.IS_EXPLOSION) && !source.is(DamageTypeTags.IS_PROJECTILE);
            float actualStrength = this.getShieldBlockDamageValue(blockingItem);
            g.set(amount.get() * actualStrength);
			entity = source.getDirectEntity();
			if (blocked && entity instanceof LivingEntity)
				instance.blockUsingShield((LivingEntity) entity);

            amount.set(amount.get() - g.get());
		}
	}

	@Override
	public float getShieldBlockDamageValue(ItemStack stack) {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(stack.getItem())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(stack.getItem());
			if (configurableItemData.blockStrength != null) {
				return (float) (configurableItemData.blockStrength / 100.0) + (Combatify.CONFIG.defender() ? EnchantmentHelper.getItemEnchantmentLevel(DefendingEnchantment.DEFENDER, stack) * 0.1F : 0);
			}
		}
		Tier var2 = stack.getItem() instanceof TieredItem tieredItem ? tieredItem.getTier() : Tiers.WOOD;
		if (stack.getItem() instanceof Tierable tierable)
			var2 = tierable.getTier();
		float strengthIncrease = (var2.getAttackDamageBonus()) / 2F - 2F;
		strengthIncrease = Math.max(strengthIncrease, -3);
		if(Combatify.CONFIG.defender())
			strengthIncrease += EnchantmentHelper.getItemEnchantmentLevel(DefendingEnchantment.DEFENDER, stack);
		return Math.min(0.3F + (strengthIncrease * 0.1F), 1);
	}
	@Override
	public double getShieldKnockbackResistanceValue(ItemStack stack) {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(stack.getItem())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(stack.getItem());
			if (configurableItemData.blockKbRes != null) {
				return configurableItemData.blockKbRes;
			}
		}
		return 0.0;
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
		ItemStack itemStack = user.getItemInHand(hand);
		if(user.isSprinting()) {
			user.setSprinting(false);
		}
		user.startUsingItem(hand);
		return InteractionResultHolder.consume(itemStack);
	}

	@Override
	public boolean canUse(Level world, Player user, InteractionHand hand) {
		ItemStack oppositeStack = user.getItemInHand(InteractionHand.OFF_HAND);
		return hand != InteractionHand.OFF_HAND && oppositeStack.isEmpty() && Combatify.CONFIG.swordBlocking();
	}
}
