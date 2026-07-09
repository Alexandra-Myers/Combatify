package net.atlas.combatify.extensions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public interface PlayerExtensions extends ClientInformationHolder {
	default void combatify$resetAttackStrengthTicker(boolean hit, Consumer<Player> original) {
		throw new IllegalStateException("Extension has not been applied");
	}

	default void combatify$customSwing(InteractionHand interactionHand) {
		throw new IllegalStateException("Extension has not been applied");
	}

	default boolean combatify$getMissedAttackRecovery() {
		throw new IllegalStateException("Extension has not been applied");
	}

    default void combatify$attackAir() {
		throw new IllegalStateException("Extension has not been applied");
	}

	default float combatify$enchantedDamageForSweep(LivingEntity livingEntity, float damage, DamageSource damageSource) {
		throw new IllegalStateException("Extension has not been applied");
	}
}
