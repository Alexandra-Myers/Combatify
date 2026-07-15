package net.atlas.combatify.item;

import net.atlas.combatify.component.CustomDataComponents;
import net.atlas.combatify.component.custom.ExtendedBlockingData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.BlocksAttacks.DamageReduction;
import net.minecraft.world.item.component.BlocksAttacks.ItemDamageFunction;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import static net.atlas.combatify.Combatify.shields;

import java.util.List;
import java.util.Optional;

public class TieredShieldItem extends ShieldItem {
	public final ToolMaterial tier;

	public TieredShieldItem(ToolMaterial tier, Properties properties, float disableCooldownModifier, float baseProt, int lvl, int tierDurabilityModifier, ExtendedBlockingData baseExtendedBlockingData, ItemDamageFunction itemDamageFunction) {
		super(properties.durability(tier.durability() * tierDurabilityModifier)
			.equippableUnswappable(EquipmentSlot.OFFHAND)
			.component(CustomDataComponents.BLOCKING_LEVEL.get(), lvl)
			.repairable(tier.repairItems())
			.enchantable(tier.enchantmentValue())
			.component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
			.delayedComponent(DataComponents.BLOCKS_ATTACKS, provider -> create(provider, 0, 1 + disableCooldownModifier, List.of(new DamageReduction(73.8723797F, Optional.empty(), baseProt, 0.25f + lvl * 0.05F)), itemDamageFunction, DamageTypeTags.BYPASSES_SHIELD))
			.component(CustomDataComponents.EXTENDED_BLOCKING_DATA.get(), baseExtendedBlockingData));
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
}
