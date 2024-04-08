package net.atlas.combatify.util;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static net.atlas.combatify.util.MethodHandler.arrowDisable;

public class CurrentShieldBlockingType extends BlockingType {
	public CurrentShieldBlockingType(String name) {
		super(name);
	}

	@Override
	public void block(LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl) {
		if (instance instanceof Player player && player.getCooldowns().isOnCooldown(blockingItem.getItem()))
			return;
		instance.hurtCurrentlyUsedShield(amount.get());
		g.set(amount.get());
		amount.set(0.0f);
		if (!source.is(DamageTypeTags.IS_PROJECTILE) && (entity = source.getDirectEntity()) instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity)entity;
			instance.blockUsingShield(livingEntity);
		} else if (source.is(DamageTypeTags.IS_PROJECTILE)) {
			switch (source.getDirectEntity()) {
				case Arrow arrow when Combatify.CONFIG.arrowDisableMode().satisfiesConditions(arrow) ->
					arrowDisable(instance, blockingItem);
				case SpectralArrow arrow when Combatify.CONFIG.arrowDisableMode().satisfiesConditions(arrow) ->
					arrowDisable(instance, blockingItem);
				case null, default -> {
					// Do nothing
				}
			}
		}
		bl.set(true);
	}

	@Override
	public float getShieldBlockDamageValue(ItemStack stack) {
		return 1;
	}

	@Override
	public double getShieldKnockbackResistanceValue(ItemStack stack) {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(stack.getItem())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(stack.getItem());
			if (configurableItemData.blockKbRes != null)
				return configurableItemData.blockKbRes;
		}
		return 0;
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

	@Override
	public void appendTooltipInfo(Consumer<Component> consumer, Player player, ItemStack stack) {

	}
}
