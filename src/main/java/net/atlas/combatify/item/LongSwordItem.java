package net.atlas.combatify.item;

import net.atlas.combatify.component.CustomDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class LongSwordItem extends Item {
	public final ToolMaterial toolMaterial;
	public LongSwordItem(ToolMaterial toolMaterial, int weaponLevel, Properties properties) {
		super(toolMaterial.applySwordProperties(properties, 0, 0).component(CustomDataComponents.PIERCING_LEVEL, piercingLevelForTier(weaponLevel)).attributes(baseAttributeModifiers(weaponLevel, toolMaterial)));
		this.toolMaterial = toolMaterial;
	}

	public static ItemAttributeModifiers baseAttributeModifiers(int weaponLevel, ToolMaterial tier) {
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
