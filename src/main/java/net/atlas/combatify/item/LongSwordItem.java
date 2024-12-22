package net.atlas.combatify.item;

import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ConfigurableWeaponData;
import net.atlas.combatify.extensions.ToolMaterialWrapper;
import net.atlas.combatify.extensions.WeaponWithType;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class LongSwordItem extends TieredItem implements WeaponWithType {
	public LongSwordItem(Tier tier, Properties properties) {
		super(tier, properties.component(DataComponents.TOOL, createToolProperties()).attributes(baseAttributeModifiers(tier)));
	}

	public static ItemAttributeModifiers baseAttributeModifiers(Tier tier) {
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		WeaponType.LONGSWORD.addCombatAttributes(tier, builder);
		return builder.build();
	}

	@Override
	public ItemAttributeModifiers modifyAttributeModifiers(ItemAttributeModifiers original) {
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		combatify$getWeaponType().addCombatAttributes(getConfigTier(), builder);
		return builder.build();
	}

	@Override
	public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player miner) {
		return !miner.isCreative();
	}

	private static Tool createToolProperties() {
		return new Tool(List.of(Tool.Rule.minesAndDrops(List.of(Blocks.COBWEB), 15.0F), Tool.Rule.overrideSpeed(BlockTags.SWORD_EFFICIENT, 1.5F)), 1.0F, 2);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
		return true;
	}

	@Override
	public double getPiercingLevel() {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(this);
		if (configurableItemData != null) {
			Double piercingLevel = configurableItemData.weaponStats().piercingLevel();
			if (piercingLevel != null)
				return piercingLevel;
		}
		ConfigurableWeaponData configurableWeaponData = MethodHandler.forWeapon(combatify$getWeaponType());
		if (configurableWeaponData != null) {
			Double piercingLevel = configurableWeaponData.piercingLevel();
			if (piercingLevel != null)
				return piercingLevel;
		}
		Tier tier = getConfigTier();
		return tier == Tiers.NETHERITE || ToolMaterialWrapper.getLevel(tier) >= 4 ? 0.2
			: tier == Tiers.GOLD || tier == Tiers.WOOD || tier == Tiers.STONE || ToolMaterialWrapper.getLevel(tier) <= 1 ? 0.0
			: (0.1 * (ToolMaterialWrapper.getLevel(tier) - 1));
	}

	@Override
	public Item combatify$self() {
		return this;
	}

	@Override
	public WeaponType combatify$getWeaponType() {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(this);
		if (configurableItemData != null) {
			WeaponType type = configurableItemData.weaponStats().weaponType();
			if (type != null)
				return type;
		}
		return WeaponType.LONGSWORD;
	}
}
