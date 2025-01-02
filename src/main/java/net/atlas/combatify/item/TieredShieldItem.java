package net.atlas.combatify.item;

import net.atlas.combatify.component.CustomDataComponents;
import net.atlas.combatify.component.custom.Blocker;
import net.atlas.combatify.extensions.Tier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.Optional;

import static net.atlas.combatify.Combatify.id;
import static net.atlas.combatify.Combatify.shields;
import static net.atlas.combatify.item.ItemRegistry.registerItem;

public class TieredShieldItem extends ShieldItem {
	public final ToolMaterial tier;
	public static final Item WOODEN_SHIELD = registerItem(id("wooden_shield"), properties -> new TieredShieldItem(ToolMaterial.WOOD, properties, 1), new Item.Properties());
	public static final Item IRON_SHIELD = registerItem(id("iron_shield"), properties -> new TieredShieldItem(ToolMaterial.IRON, properties, 3), new Item.Properties());
	public static final Item GOLD_SHIELD = registerItem(id("golden_shield"), properties -> new TieredShieldItem(ToolMaterial.GOLD, properties, 1), new Item.Properties());
	public static final Item DIAMOND_SHIELD = registerItem(id("diamond_shield"), properties -> new TieredShieldItem(ToolMaterial.DIAMOND, properties, 4),new Item.Properties());
	public static final Item NETHERITE_SHIELD = registerItem(id("netherite_shield"), properties -> new TieredShieldItem(ToolMaterial.NETHERITE, properties, 5), new Item.Properties().fireResistant());

	public TieredShieldItem(ToolMaterial tier, Properties properties, int lvl) {
		super(properties.durability(tier.durability() * 2)
			.equippableUnswappable(EquipmentSlot.OFFHAND)
			.component(CustomDataComponents.BLOCKING_LEVEL, lvl)
			.repairable(tier.repairItems())
			.enchantable(tier.enchantmentValue())
			.component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
			.component(CustomDataComponents.BLOCKER, Blocker.NEW_SHIELD));
		this.tier = tier;
		shields.add(this);
	}

	@Override
	public Item combatify$self() {
		return this;
	}

	@Override
	public Tier getConfigTier() {
		Optional<Tier> tier = getTierFromConfig();
        return tier.orElse(this.tier);
    }
	public static void init() {

	}
}
