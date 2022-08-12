package net.alexandra.atlas.atlas_combat.extensions;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.alexandra.atlas.atlas_combat.item.NewAttributes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public interface PlayerExtensions {
	boolean isAttackAvailable(float baseTime);
	float getCurrentAttackReach(float baseValue);

	default boolean customShieldInteractions(float damage) {return false;}

	default boolean hasEnabledShieldOnCrouch() {
		return false;
	}

	default int getAttackDelay(Player player) {
		float var1 = (float)player.getAttribute(Attributes.ATTACK_SPEED).getValue() - 1.5F;
		var1 = Mth.clamp(var1, 0.1F, 1024.0F);
		return (int)(1.0F / var1 * 20.0F + 0.5F);
	}
	Multimap getAdditionalModifiers();
	boolean getMissedAttackRecovery();
	int getAttackStrengthStartValue();
	double getReach(final LivingEntity entity, final double baseAttackRange);

	double getSquaredReach(final LivingEntity entity, final double sqBaseAttackRange);
	double getAttackRange(final LivingEntity entity, final double baseAttackRange);

	double getSquaredAttackRange(final LivingEntity entity, final double sqBaseAttackRange);
	void attackAir();


}
