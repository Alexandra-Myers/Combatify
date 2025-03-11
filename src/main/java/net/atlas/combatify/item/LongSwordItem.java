package net.atlas.combatify.item;

import net.atlas.combatify.component.CustomDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class LongSwordItem extends TieredItem {
	public LongSwordItem(Tier tier, int weaponLevel, Properties properties) {
		super(tier, properties.component(DataComponents.TOOL, createToolProperties()).component(CustomDataComponents.PIERCING_LEVEL, piercingLevelForTier(weaponLevel)).attributes(baseAttributeModifiers(weaponLevel, tier)));
	}
	private static Tool createToolProperties() {
		return new Tool(List.of(Tool.Rule.minesAndDrops(List.of(Blocks.COBWEB), 15.0F), Tool.Rule.overrideSpeed(BlockTags.SWORD_EFFICIENT, 1.5F)), 1.0F, 2);
	}

	public static ItemAttributeModifiers baseAttributeModifiers(int weaponLevel, Tier tier) {
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		WeaponType.LONGSWORD.addCombatAttributes(weaponLevel, tier, builder);
		return builder.build();
	}

	@Override
	public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player miner) {
		return !miner.isCreative();
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
		return true;
	}

	public static float piercingLevelForTier(int weaponLevel) {
		return weaponLevel >= 4 ? 0.2F
			: weaponLevel <= 1 ? 0
			: (0.1F * (weaponLevel - 1));
	}
}
