package net.atlas.combatify.item;

import net.atlas.combatify.component.CustomDataComponents;
import net.atlas.combatify.component.custom.ExtendedBlockingData;
import net.atlas.combatify.util.blocking.BlockingTypeInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.BlocksAttacks.DamageReduction;
import net.minecraft.world.item.component.BlocksAttacks.ItemDamageFunction;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import static net.atlas.combatify.Combatify.id;
import static net.atlas.combatify.Combatify.shields;
import static net.atlas.combatify.item.ItemRegistry.registerItem;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TieredShieldItem extends ShieldItem {
	public final ToolMaterial tier;
	public static final ItemDamageFunction SHIELD_DAMAGE_FUNCTION = new ItemDamageFunction(3.0F, 0.0F, 1.0F);
	public static final ItemDamageFunction GOLDEN_SHIELD_DAMAGE_FUNCTION = new ItemDamageFunction(4.0F, 0.0F, 0.5F);
	public static final Item IRON_SHIELD = registerItem(id("iron_shield"), properties -> new TieredShieldItem(ToolMaterial.IRON, properties, 0, 0.5F, 3, 3, ExtendedBlockingData.NEW_SHIELD, SHIELD_DAMAGE_FUNCTION), new Item.Properties());
	public static final Item GOLD_SHIELD = registerItem(id("golden_shield"), properties -> new TieredShieldItem(ToolMaterial.GOLD, properties, -0.2F, 0.5F, 1, 4, ExtendedBlockingData.NEW_SHIELD, GOLDEN_SHIELD_DAMAGE_FUNCTION), new Item.Properties());
	public static final Item COPPER_SHIELD = registerItem(id("copper_shield"), properties -> new TieredShieldItem(ToolMaterial.COPPER, properties, 0.1F, 1.25F, 2, 3, ExtendedBlockingData.NEW_SHIELD.withKnockback(Collections.singletonList(BlockingTypeInit.WEAK_SHIELD_KNOCKBACK)).withProtection(Collections.singletonList(BlockingTypeInit.COPPER_SHIELD_PROTECTION)), SHIELD_DAMAGE_FUNCTION), new Item.Properties());
	public static final Item DIAMOND_SHIELD = registerItem(id("diamond_shield"), properties -> new TieredShieldItem(ToolMaterial.DIAMOND, properties, 0, 0.5F, 5, 2, ExtendedBlockingData.NEW_SHIELD, SHIELD_DAMAGE_FUNCTION),new Item.Properties());
	public static final Item NETHERITE_SHIELD = registerItem(id("netherite_shield"), properties -> new TieredShieldItem(ToolMaterial.NETHERITE, properties, 0, 1, 5, 2, ExtendedBlockingData.NEW_SHIELD.withKnockback(Collections.singletonList(BlockingTypeInit.SHIELD_KNOCKBACK)).withProtection(Collections.singletonList(BlockingTypeInit.NETHERITE_SHIELD_PROTECTION)), SHIELD_DAMAGE_FUNCTION), new Item.Properties().fireResistant());

	public TieredShieldItem(ToolMaterial tier, Properties properties, float disableCooldownModifier, float baseProt, int lvl, int tierDurabilityModifier, ExtendedBlockingData baseExtendedBlockingData, ItemDamageFunction itemDamageFunction) {
		super(properties.durability(tier.durability() * tierDurabilityModifier)
			.equippableUnswappable(EquipmentSlot.OFFHAND)
			.component(CustomDataComponents.BLOCKING_LEVEL, lvl)
			.repairable(tier.repairItems())
			.enchantable(tier.enchantmentValue())
			.component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
			.delayedComponent(DataComponents.BLOCKS_ATTACKS, provider -> create(provider, 0, 1 + disableCooldownModifier, List.of(new DamageReduction(73.8723797F, Optional.empty(), baseProt, 0.25f + lvl * 0.05F)), itemDamageFunction, DamageTypeTags.BYPASSES_SHIELD))
			.component(CustomDataComponents.EXTENDED_BLOCKING_DATA, baseExtendedBlockingData));
		this.tier = tier;
		shields.add(this);
	}

	public static BlocksAttacks create(HolderLookup.Provider provider,
									   float blockDelaySeconds,
									   float disableCooldownScale,
									   List<BlocksAttacks.DamageReduction> damageReductions,
									   BlocksAttacks.ItemDamageFunction itemDamage,
									   TagKey<DamageType> bypassedBy) {
		return new BlocksAttacks(blockDelaySeconds,
			disableCooldownScale,
			damageReductions,
			itemDamage,
			Optional.of(provider.getOrThrow(bypassedBy)),
			Optional.of(SoundEvents.SHIELD_BLOCK),
			Optional.of(SoundEvents.SHIELD_BREAK));
	}

	public static void init() {

	}
}
