package net.atlas.combatify.item;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.WeaponWithType;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class KnifeItem extends TieredItem implements WeaponWithType {
	public KnifeItem(Tier tier, Properties properties) {
		super(tier, properties.attributes(baseAttributeModifiers(tier)));
	}

	public static ItemAttributeModifiers baseAttributeModifiers(Tier tier) {
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		WeaponType.KNIFE.addCombatAttributes(tier, builder);
		return builder.build();
	}

	@Override
	public ItemAttributeModifiers modifyAttributeModifiers(ItemAttributeModifiers original) {
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		getWeaponType().addCombatAttributes(getTier(), builder);
		return builder.build();
	}

	@Override
	public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player miner) {
		return !miner.isCreative();
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		if (state.is(Blocks.COBWEB)) {
			return 15.0F;
		} else {
			return state.is(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1.0F;
		}
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
		return true;
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner) {
		if (state.getDestroySpeed(world, pos) != 0.0F) {
			stack.hurtAndBreak(2, miner, EquipmentSlot.MAINHAND);
		}

		return true;
	}

	@Override
	public boolean isCorrectToolForDrops(BlockState state) {
		return state.is(Blocks.COBWEB);
	}

	@Override
	public WeaponType getWeaponType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			WeaponType type = Combatify.ITEMS.configuredItems.get(this).type;
			if (type != null)
				return type;
		}
		return WeaponType.KNIFE;
	}

	@Override
	public Item self() {
		return this;
	}
}
