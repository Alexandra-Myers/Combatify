package net.atlas.combatify.util.blocking;

import com.mojang.serialization.MapCodec;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.critereon.ItemHasComponentPredicate;
import net.atlas.combatify.critereon.ItemSubPredicateInit;
import net.atlas.combatify.util.blocking.ComponentModifier.CombinedModifier;
import net.atlas.combatify.util.blocking.condition.AnyOf;
import net.atlas.combatify.util.blocking.condition.ItemMatches;
import net.atlas.combatify.util.blocking.damage_parsers.*;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.List;
import java.util.Optional;

import static net.atlas.combatify.Combatify.defineDefaultBlockingType;
import static net.minecraft.resources.ResourceKey.createRegistryKey;

public class BlockingTypeInit {
	public static final ResourceKey<Registry<MapCodec<? extends DamageParser>>> DAMAGE_PARSER_TYPE = createRegistryKey(Combatify.id("damage_parser"));
	public static final Registry<MapCodec<? extends DamageParser>> DAMAGE_PARSER_TYPE_REG = FabricRegistryBuilder.createSimple(
		DAMAGE_PARSER_TYPE
	).attribute(RegistryAttribute.OPTIONAL).buildAndRegister();
	public static final CombinedModifier SHIELD_PROTECTION_WITHOUT_BANNER = CombinedModifier.createBaseOnly(new ComponentModifier(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id(), Component.translatableWithFallback("attribute.name.shield_strength", "Shield Strength")), new AddValue(LevelBasedValue.perLevel(5, 1))), Optional.empty());
	public static final List<CombinedModifier> SHIELD_PROTECTION = List.of(SHIELD_PROTECTION_WITHOUT_BANNER,
		CombinedModifier.createBaseOnly(new ComponentModifier(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id(), Component.translatableWithFallback("attribute.name.shield_strength", "Shield Strength")),
			new AddValue(LevelBasedValue.constant(5)),
			new AnyOf(new ItemMatches(ItemPredicate.Builder.item().withSubPredicate(ItemSubPredicateInit.HAS_COMPONENT, new ItemHasComponentPredicate(List.of(DataComponents.BASE_COLOR), true)).build(), false),
				new ItemMatches(ItemPredicate.Builder.item().hasComponents(DataComponentPredicate.builder().expect(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).build()).build(), true))), Optional.empty()));
	public static final CombinedModifier NEW_SHIELD_PROTECTION = CombinedModifier.createFactorOnly(new ComponentModifier(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.id(), Component.translatableWithFallback("attribute.name.shield_reduction", "Shield Damage Reduction")), new AddValue(LevelBasedValue.perLevel(30, 5))), Optional.empty());
	public static final ComponentModifier SHIELD_KNOCKBACK = new ComponentModifier(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id(), Component.translatable("attribute.name.knockback_resistance")), new AddValue(LevelBasedValue.constant(5)));
	public static final ComponentModifier BANNER_SHIELD_KNOCKBACK = new ComponentModifier(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id(), Component.translatable("attribute.name.knockback_resistance")),
		new AddValue(LevelBasedValue.constant(3)),
		Optional.of(new AnyOf(new ItemMatches(ItemPredicate.Builder.item().withSubPredicate(ItemSubPredicateInit.HAS_COMPONENT, new ItemHasComponentPredicate(List.of(DataComponents.BASE_COLOR), true)).build(), false),
			new ItemMatches(ItemPredicate.Builder.item().hasComponents(DataComponentPredicate.builder().expect(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).build()).build(), true))));
	public static final ComponentModifier NEW_SHIELD_KNOCKBACK = new ComponentModifier(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id(), Component.translatable("attribute.name.knockback_resistance")), new AddValue(LevelBasedValue.constant(2.5F)), Optional.empty());

	public static final BlockingType EMPTY = BlockingType.builder().build("empty");
	public static void init() {
		DamageParser.bootstrap(DAMAGE_PARSER_TYPE_REG);
		defineDefaultBlockingType(BlockingType.builder().setDisablement(false).setCrouchable(false).setBlockHit(true).setRequireFullCharge(false).setDelay(false).build("sword"));
		defineDefaultBlockingType(BlockingType.builder().setDisablement(false).setCrouchable(false).setBlockHit(true).setRequireFullCharge(false).setDelay(false).build("original_sword"));
		defineDefaultBlockingType(BlockingType.builder().build("shield"));
		defineDefaultBlockingType(BlockingType.builder().setKbMechanics(false).build("new_shield"));
		defineDefaultBlockingType(BlockingType.builder().build("shield_no_banner"));
		defineDefaultBlockingType(BlockingType.builder().build("current_shield"));
	}
}
