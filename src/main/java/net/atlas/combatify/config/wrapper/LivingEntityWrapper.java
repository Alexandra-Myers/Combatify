package net.atlas.combatify.config.wrapper;

import net.atlas.combatify.util.MethodHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class LivingEntityWrapper<L extends LivingEntity> extends EntityWrapper<L> {
	public LivingEntityWrapper(L value) {
		super(value);
	}

	public final boolean isOnCooldown(ItemStackWrapper itemStackWrapper) {
		return MethodHandler.isItemOnCooldown(value, itemStackWrapper.value());
	}

	public final void resetAttackStrengthTicker(boolean hit) {
		value.combatify$resetAttackStrengthTicker(hit);
	}

	public final void resetAttackStrengthTicker(boolean hit, boolean force) {
		value.combatify$resetAttackStrengthTicker(hit, force);
	}

	public final void swingInHand(String hand) {
		value.swing(InteractionHand.valueOf(hand.toUpperCase()));
	}

	public final void swingInHand(String hand, boolean force) {
		value.swing(InteractionHand.valueOf(hand.toUpperCase()), force);
	}

	public final ItemStackWrapper getItemInHand(String hand) {
		return new ItemStackWrapper(value.getItemInHand(InteractionHand.valueOf(hand.toUpperCase())));
	}

	public final ItemStackWrapper getItemInSlot(String slot) {
		return new ItemStackWrapper(value.getItemBySlot(EquipmentSlot.valueOf(slot.toUpperCase())));
	}

	public final FakeUseItemWrapper getBlockingItem() {
		return new FakeUseItemWrapper(MethodHandler.getBlockingItem(value));
	}

	public final boolean hasEffect(String mobEffect) {
		return value.hasEffect(BuiltInRegistries.MOB_EFFECT.getOrThrow(ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.parse(mobEffect))));
	}

	public final boolean onClimbable() {
		return value.onClimbable();
	}

	public final void heal(float amount) {
		value.heal(amount);
	}

	public final float getHealth() {
		return value.getHealth();
	}

	public final float getMaxHealth() {
		return value.getMaxHealth();
	}

	public final float getMaxAbsorption() {
		return value.getMaxAbsorption();
	}

	public final void setHealth(float newHealth) {
		value.setHealth(newHealth);
	}
}
