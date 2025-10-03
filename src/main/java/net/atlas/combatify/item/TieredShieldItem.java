package net.atlas.combatify.item;

import net.atlas.combatify.component.CustomDataComponents;
import net.atlas.combatify.component.custom.ExtendedBlockingData;
import net.atlas.combatify.util.blocking.BlockingTypeInit;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import static net.atlas.combatify.Combatify.id;
import static net.atlas.combatify.Combatify.shields;
import static net.atlas.combatify.item.ItemRegistry.registerItem;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TieredShieldItem extends ShieldItem {
	public final ToolMaterial tier;
	public static final Item IRON_SHIELD = registerItem(id("iron_shield"), properties -> new TieredShieldItem(ToolMaterial.IRON, properties, 0, 3, ExtendedBlockingData.NEW_SHIELD), new Item.Properties());
	public static final Item GOLD_SHIELD = registerItem(id("golden_shield"), properties -> new TieredShieldItem(ToolMaterial.GOLD, properties, 0, 1, ExtendedBlockingData.NEW_SHIELD), new Item.Properties());
	public static final Item DIAMOND_SHIELD = registerItem(id("diamond_shield"), properties -> new TieredShieldItem(ToolMaterial.DIAMOND, properties, 0, 5, ExtendedBlockingData.NEW_SHIELD),new Item.Properties());
	public static final Item NETHERITE_SHIELD = registerItem(id("netherite_shield"), properties -> new TieredShieldItem(ToolMaterial.NETHERITE, properties, 1, 5, ExtendedBlockingData.NEW_SHIELD.withKnockback(Collections.singletonList(BlockingTypeInit.SHIELD_KNOCKBACK)).withProtection(List.of(BlockingTypeInit.NETHERITE_SHIELD_PROTECTION, BlockingTypeInit.NEW_SHIELD_PROTECTION))), new Item.Properties().fireResistant());

	public TieredShieldItem(ToolMaterial tier, Properties properties, float base, int lvl, ExtendedBlockingData baseExtendedBlockingData) {
		super(properties.durability(tier.durability() * 2)
			.equippableUnswappable(EquipmentSlot.OFFHAND)
			.component(CustomDataComponents.BLOCKING_LEVEL, lvl)
			.repairable(tier.repairItems())
			.enchantable(tier.enchantmentValue())
			.component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
			.component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(0, 1, List.of(new BlocksAttacks.DamageReduction(73.8723797F, Optional.empty(), base, 0.25f + lvl * 0.05F)), BlocksAttacks.ItemDamageFunction.DEFAULT, Optional.empty(), Optional.of(SoundEvents.SHIELD_BLOCK), Optional.of(SoundEvents.SHIELD_BREAK)))
			.component(CustomDataComponents.EXTENDED_BLOCKING_DATA, baseExtendedBlockingData));
		this.tier = tier;
		shields.add(this);
	}

	@Override
	public Item combatify$self() {
		return this;
	}

	public static void init() {

	}
}
