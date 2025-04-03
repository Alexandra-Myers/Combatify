package net.atlas.combatify.config.wrapper;

import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class PlayerWrapper<P extends Player> extends LivingEntityWrapper<P> {
	public PlayerWrapper(P value) {
		super(value);
	}

	public final ItemStackWrapper getInventoryItem(int slotID) {
		return new ItemStackWrapper(value.getInventory().getItem(slotID));
	}

	public final float getAttackStrengthScale(float baseTime) {
		return value.getAttackStrengthScale(baseTime);
	}

	public final double getCurrentAttackReach(float baseTime) {
		return MethodHandler.getCurrentAttackReach(value, baseTime);
	}

	public final void resetAttackStrengthTicker(boolean hit) {
		value.combatify$resetAttackStrengthTicker(hit);
	}

	public final void attack(EntityWrapper<?> entityWrapper) {
		value.attack(entityWrapper.value);
	}

	public final void attackAir() {
		value.swing(InteractionHand.MAIN_HAND);
		value.combatify$attackAir();
	}
}
