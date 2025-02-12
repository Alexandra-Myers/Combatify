package net.atlas.combatify.item;

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

public class KnifeItem extends Item {
	public final ToolMaterial toolMaterial;
	public KnifeItem(ToolMaterial toolMaterial, Properties properties) {
		super(toolMaterial.applySwordProperties(properties, 0, 0).attributes(baseAttributeModifiers(toolMaterial)));
		this.toolMaterial = toolMaterial;
	}

	public static ItemAttributeModifiers baseAttributeModifiers(ToolMaterial tier) {
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		WeaponType.KNIFE.addCombatAttributes(0, tier, builder);
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
}
