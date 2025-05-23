package net.atlas.combatify.util.blocking;

import net.atlas.combatify.util.blocking.ComponentModifier.CombinedModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;

import java.util.Optional;

import static net.atlas.combatify.Combatify.defineDefaultBlockingType;

public class BlockingTypeInit {
	public static final CombinedModifier NEW_SHIELD_PROTECTION = CombinedModifier.createFactorOnly(new ComponentModifier(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.id(), Component.translatableWithFallback("attribute.name.shield_reduction", "Shield Damage Reduction")), new AddValue(LevelBasedValue.perLevel(0.3F, 0.05F)), 100), Optional.empty());
	public static final ComponentModifier SHIELD_KNOCKBACK = new ComponentModifier(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id(), Component.translatable("attribute.name.knockback_resistance")), new AddValue(LevelBasedValue.constant(0.5F)), 10);
	public static final ComponentModifier NEW_SHIELD_KNOCKBACK = new ComponentModifier(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id(), Component.translatable("attribute.name.knockback_resistance")), new AddValue(LevelBasedValue.constant(0.25F)), Optional.empty(), 10);

	public static final BlockingType EMPTY = BlockingType.builder().build("empty");
	public static void init() {
		defineDefaultBlockingType(BlockingType.builder().setDisablement(false).setCrouchable(false).setBlockHit(true).setRequireFullCharge(false).setDelay(false).build("sword"));
		defineDefaultBlockingType(BlockingType.builder().build("shield"));
		defineDefaultBlockingType(BlockingType.builder().setKbMechanics(false).build("new_shield"));
	}
}
