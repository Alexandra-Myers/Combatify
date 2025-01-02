package net.atlas.combatify.util.blocking;

import com.mojang.serialization.MapCodec;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.critereon.ItemBlockingLevelPredicate;
import net.atlas.combatify.critereon.ItemHasComponentPredicate;
import net.atlas.combatify.critereon.ItemSubPredicateInit;
import net.atlas.combatify.util.blocking.condition.AnyOf;
import net.atlas.combatify.util.blocking.condition.ItemMatches;
import net.atlas.combatify.util.blocking.damage_parsers.*;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static net.atlas.combatify.Combatify.defineBlockingTypeFactory;
import static net.atlas.combatify.Combatify.defineDefaultBlockingType;
import static net.minecraft.resources.ResourceKey.createRegistryKey;

public class BlockingTypeInit {
	public static final ResourceKey<Registry<MapCodec<? extends DamageParser>>> DAMAGE_PARSER_TYPE = createRegistryKey(Combatify.id("damage_parser"));
	public static final Registry<MapCodec<? extends DamageParser>> DAMAGE_PARSER_TYPE_REG = FabricRegistryBuilder.createSimple(
		DAMAGE_PARSER_TYPE
	).attribute(RegistryAttribute.OPTIONAL).buildAndRegister();
	public static final ComponentModifier SHIELD_PROTECTION_WITHOUT_BANNER = new ComponentModifier(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id(), Component.translatable("attribute.name.shield_strength")), new AddValue(LevelBasedValue.perLevel(5, 1)), Optional.empty());
	public static final List<ConditionalEffect<ComponentModifier>> SHIELD_PROTECTION = List.of(new ConditionalEffect<>(SHIELD_PROTECTION_WITHOUT_BANNER, Optional.empty()),
		ComponentModifier.matchingConditions(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id(), Component.translatable("attribute.name.shield_strength")),
			new AddValue(LevelBasedValue.constant(5)),
			new AnyOf(new ItemMatches(ItemPredicate.Builder.item().withSubPredicate(ItemSubPredicateInit.HAS_COMPONENT, new ItemHasComponentPredicate(List.of(DataComponents.BASE_COLOR), true)).build(), false),
				new ItemMatches(ItemPredicate.Builder.item().hasComponents(DataComponentPredicate.builder().expect(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).build()).build(), true)),
			AnyOfCondition.anyOf(MatchTool.toolMatches(ItemPredicate.Builder.item().withSubPredicate(ItemSubPredicateInit.HAS_COMPONENT, new ItemHasComponentPredicate(List.of(DataComponents.BASE_COLOR), true))),
				InvertedLootItemCondition.invert(MatchTool.toolMatches(ItemPredicate.Builder.item().hasComponents(DataComponentPredicate.builder().expect(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).build())))).build()));
	public static final ConditionalEffect<ComponentModifier> OLD_SWORD_PROTECTION = ComponentModifier.noConditions(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.id(), Component.translatable("attribute.name.damage_reduction")), new AddValue(LevelBasedValue.constant(50)));
	public static final ConditionalEffect<ComponentModifier> SWORD_PROTECTION = ComponentModifier.noConditions(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.id(), Component.translatable("attribute.name.damage_reduction")), new AddValue(LevelBasedValue.perLevel(10, 5)));
	public static final ConditionalEffect<ComponentModifier> NEW_SHIELD_PROTECTION = ComponentModifier.noConditions(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.id(), Component.translatable("attribute.name.shield_reduction")), new AddValue(LevelBasedValue.perLevel(30, 5)));
	public static final ComponentModifier SHIELD_KNOCKBACK = new ComponentModifier(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id(), Component.translatable("attribute.name.knockback_resistance")), new AddValue(LevelBasedValue.constant(5)), Optional.empty());
	public static final ComponentModifier BANNER_SHIELD_KNOCKBACK = new ComponentModifier(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id(), Component.translatable("attribute.name.knockback_resistance")),
		new AddValue(LevelBasedValue.constant(3)),
		Optional.of(new AnyOf(new ItemMatches(ItemPredicate.Builder.item().withSubPredicate(ItemSubPredicateInit.HAS_COMPONENT, new ItemHasComponentPredicate(List.of(DataComponents.BASE_COLOR), true)).build(), false),
			new ItemMatches(ItemPredicate.Builder.item().hasComponents(DataComponentPredicate.builder().expect(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).build()).build(), true))));
	public static final Function<Integer, List<ComponentModifier>> NEW_SHIELD_KNOCKBACK = i -> List.of(new ComponentModifier(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id(), Component.translatable("attribute.name.knockback_resistance")), new AddValue(LevelBasedValue.constant(2.5F)), Optional.empty()),
		new ComponentModifier(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id(), Component.translatable("attribute.name.knockback_resistance")), new AddValue(LevelBasedValue.constant(2.5F)), Optional.of(new ItemMatches(ItemPredicate.Builder.item().withSubPredicate(ItemSubPredicateInit.BLOCKING_LEVEL, new ItemBlockingLevelPredicate(MinMaxBounds.Ints.atLeast(i))).build(), false))));
	public static BlockingType.Factory SWORD_BLOCKING_TYPE_FACTORY;
	public static BlockingType.Factory OLD_SWORD_BLOCKING_TYPE_FACTORY;
	public static BlockingType.Factory SHIELD_BLOCKING_TYPE_FACTORY;
	public static BlockingType.Factory NON_BANNER_SHIELD_BLOCKING_TYPE_FACTORY;
	public static BlockingType.Factory CURRENT_SHIELD_BLOCKING_TYPE_FACTORY;
	public static BlockingType.Factory NEW_SHIELD_BLOCKING_TYPE_FACTORY;

	public static final BlockingType EMPTY = BlockingType.builder((name, blockingTypeData) -> new BlockingType(name, ResourceLocation.parse("empty"), new BlockingType.BlockingTypeHandler(Collections.emptyList(), Optional.empty(), Collections.emptyList(), Collections.emptyList(), false), new BlockingType.BlockingTypeData(false, false, false, false, false, false))).build("empty");
	public static void init() {
		DamageParser.bootstrap(DAMAGE_PARSER_TYPE_REG);
		SWORD_BLOCKING_TYPE_FACTORY = defineBlockingTypeFactory(Combatify.id("sword"), BlockingType.Factory.forHandler(new BlockingType.BlockingTypeHandler(
			Collections.singletonList(Percentage.ALL),
			Optional.empty(),
			Collections.singletonList(SWORD_PROTECTION),
			Collections.emptyList(),
			false), Combatify.id("sword")));
		OLD_SWORD_BLOCKING_TYPE_FACTORY = defineBlockingTypeFactory(Combatify.id("original_sword"), BlockingType.Factory.forHandler(new BlockingType.BlockingTypeHandler(
			Collections.singletonList(PercentageLimit.ALL),
			Optional.empty(),
			Collections.singletonList(OLD_SWORD_PROTECTION),
			Collections.emptyList(),
			false), Combatify.id("original_sword")));
		SHIELD_BLOCKING_TYPE_FACTORY = defineBlockingTypeFactory(Combatify.id("shield"), BlockingType.Factory.forHandler(new BlockingType.BlockingTypeHandler(
			List.of(Nullify.NULLIFY_EXPLOSIONS_AND_PROJECTILES, Direct.IGNORE_EXPLOSIONS_AND_PROJECTILES),
			Optional.empty(),
			SHIELD_PROTECTION,
			List.of(SHIELD_KNOCKBACK, BANNER_SHIELD_KNOCKBACK),
			true), Combatify.id("shield")));
		NON_BANNER_SHIELD_BLOCKING_TYPE_FACTORY = defineBlockingTypeFactory(Combatify.id("shield_no_banner"), BlockingType.Factory.forHandler(new BlockingType.BlockingTypeHandler(
			List.of(Nullify.NULLIFY_EXPLOSIONS_AND_PROJECTILES, Direct.IGNORE_EXPLOSIONS_AND_PROJECTILES),
			Optional.empty(),
			Collections.singletonList(new ConditionalEffect<>(SHIELD_PROTECTION_WITHOUT_BANNER, Optional.empty())),
			Collections.singletonList(SHIELD_KNOCKBACK),
			true), Combatify.id("shield_no_banner")));
		CURRENT_SHIELD_BLOCKING_TYPE_FACTORY = defineBlockingTypeFactory(Combatify.id("current_shield"), BlockingType.Factory.forHandler(new BlockingType.BlockingTypeHandler(
			Collections.singletonList(Nullify.NULLIFY_ALL),
			Optional.empty(),
			Collections.emptyList(),
			Collections.emptyList(),
			true), Combatify.id("current_shield")));
		NEW_SHIELD_BLOCKING_TYPE_FACTORY = defineBlockingTypeFactory(Combatify.id("new_shield"), BlockingType.Factory.forHandler(new BlockingType.BlockingTypeHandler(
			List.of(Nullify.NULLIFY_EXPLOSIONS_AND_PROJECTILES, Percentage.IGNORE_EXPLOSIONS_AND_PROJECTILES),
			Optional.empty(),
			Collections.singletonList(NEW_SHIELD_PROTECTION),
			NEW_SHIELD_KNOCKBACK.apply(5),
			true), Combatify.id("new_shield")));
		defineBlockingTypeFactory(Combatify.id("test"), BlockingType.Factory.forHandler(new BlockingType.BlockingTypeHandler(
			List.of(Nullify.NULLIFY_EXPLOSIONS_AND_PROJECTILES, Percentage.IGNORE_EXPLOSIONS_AND_PROJECTILES),
			Optional.empty(),
			Collections.singletonList(SWORD_PROTECTION),
			NEW_SHIELD_KNOCKBACK.apply(5),
			false), Combatify.id("test")));
		defineDefaultBlockingType(BlockingType.builder(SWORD_BLOCKING_TYPE_FACTORY).setDisablement(false).setCrouchable(false).setBlockHit(true).setRequireFullCharge(false).setDelay(false).build("sword"));
		defineDefaultBlockingType(BlockingType.builder(OLD_SWORD_BLOCKING_TYPE_FACTORY).setDisablement(false).setCrouchable(false).setBlockHit(true).setRequireFullCharge(false).setDelay(false).build("original_sword"));
		defineDefaultBlockingType(BlockingType.builder(SHIELD_BLOCKING_TYPE_FACTORY).build("shield"));
		defineDefaultBlockingType(BlockingType.builder(NEW_SHIELD_BLOCKING_TYPE_FACTORY).setKbMechanics(false).build("new_shield"));
		defineDefaultBlockingType(BlockingType.builder(NON_BANNER_SHIELD_BLOCKING_TYPE_FACTORY).build("shield_no_banner"));
		defineDefaultBlockingType(BlockingType.builder(CURRENT_SHIELD_BLOCKING_TYPE_FACTORY).build("current_shield"));
	}
}
