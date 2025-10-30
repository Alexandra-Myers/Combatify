package net.atlas.combatify.config.wrapper;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.entity.player.Player;

public class PlayerWrapper<P extends Player> extends LivingEntityWrapper<P> {
	public PlayerWrapper(P value) {
		super(value);
	}

	public final ItemStackWrapper getInventoryItem(int slotID) {
		return new ItemStackWrapper(value.getInventory().getItem(slotID));
	}

	public final boolean isAttackAvailable(float baseTime) {
		return value.combatify$isAttackAvailable(baseTime);
	}

	// Respect future versions
	public final boolean isAttackAvailable(float baseTime, ItemStackWrapper stack) {
		return value.combatify$isAttackAvailable(baseTime);
	}

	public final boolean isChargeAttack(float baseTime) {
		return value.getAttackStrengthScale(baseTime) > (Combatify.CONFIG.chargedAttacks() ? 1.95 : 0.9);
	}

	public final float getAttackStrengthScale(float baseTime) {
		return value.getAttackStrengthScale(baseTime);
	}

	public final double getCurrentAttackReach(float baseTime) {
		return MethodHandler.getCurrentAttackReach(value, baseTime);
	}

	public final void attack(EntityWrapper<?> entityWrapper) {
		value.attack(entityWrapper.value);
	}

	public final void stabAttack(String slot, EntityWrapper<?> entityWrapper, float damage, boolean dealDamage, boolean dealKnockback, boolean dismountTarget) {
		// Don't crash, but just do a normal attack instead
		value.attack(entityWrapper.value);
		Combatify.JS_LOGGER.warn("Attempted to call stabAttack() on version before Mounts of Mayhem! Falling back to standard attack.");
	}

	public final void attackAir() {
		value.combatify$attackAir();
	}

	public final void lungeForwardMaybe() {
		// Do nothing, just let the game not crash
		Combatify.JS_LOGGER.warn("Attempted to call lungeForwardMaybe() on version before Mounts of Mayhem!");
	}
}
