package net.atlas.combatify.util;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.extensions.Tierable;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NewShieldBlockingType extends BlockingType {
	public NewShieldBlockingType(String name) {
		super(name);
	}

	@Override
	public void block(LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl) {
		if (instance instanceof Player player && player.getCooldowns().isOnCooldown(blockingItem.getItem()))
			return;
		boolean hurt = false;
		float factor = this.getShieldBlockFactor(blockingItem);
		float base = this.getShieldBlockBase(blockingItem);
		g.set(Math.min(base + (amount.get() * factor), amount.get()));
		if (source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_PROJECTILE))
			g.set(amount.get());
		else {
			entity = source.getDirectEntity();
			if (entity instanceof LivingEntity) {
				instance.hurtCurrentlyUsedShield(g.get());
				hurt = true;
				instance.blockUsingShield((LivingEntity) entity);
			}
		}

		if (!hurt) instance.hurtCurrentlyUsedShield(g.get());
		amount.set(amount.get() - g.get());
		bl.set(true);
	}

	@Override
	public float getShieldBlockBase(ItemStack stack) {
		Float configBase = getShieldBlockBaseConfig(stack);
		if (configBase != null) return configBase;
		Tier var2 = stack.getItem() instanceof TieredItem tieredItem ? tieredItem.getTier() : Tiers.WOOD;
		if (stack.getItem() instanceof Tierable tierable)
			var2 = tierable.getTier();
		int strengthIncrease = var2.getLevel() >= 4 ? 1 : 0;
		if (Combatify.CONFIG.defender()) strengthIncrease += getDefenderBonus(stack);
		return strengthIncrease;
	}

	@Override
	public float getShieldBlockFactor(ItemStack stack) {
		Float configFactor = getShieldBlockFactorConfig(stack);
		if (configFactor != null) return configFactor;
		Tier var2 = stack.getItem() instanceof TieredItem tieredItem ? tieredItem.getTier() : Tiers.WOOD;
		if (stack.getItem() instanceof Tierable tierable)
			var2 = tierable.getTier();
		float strengthIncrease = (var2.getAttackDamageBonus()) / 2F - 2F;
		strengthIncrease = Mth.ceil(strengthIncrease);
		return Math.min(0.5F + (strengthIncrease * 0.1F), 1);
	}

	@Override
	public double getShieldKnockbackResistanceValue(ItemStack stack) {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(stack.getItem())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(stack.getItem());
			if (configurableItemData.blockKbRes != null) {
				return configurableItemData.blockKbRes;
			}
		}
		Tier var2 = stack.getItem() instanceof TieredItem tieredItem ? tieredItem.getTier() : Tiers.WOOD;
		if (stack.getItem() instanceof Tierable tierable)
			var2 = tierable.getTier();
		if (var2.getLevel() >= 4)
			return 0.5;
		if (var2 != Tiers.WOOD)
			return 0.4;
		return 0.3;
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		player.startUsingItem(interactionHand);
		return InteractionResultHolder.consume(itemStack);
	}

	@Override
	public boolean canUse(Level world, Player user, InteractionHand hand) {
		return true;
	}
}
