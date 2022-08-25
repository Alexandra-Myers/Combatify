package net.alexandra.atlas.atlas_combat.extensions;

import net.alexandra.atlas.atlas_combat.util.MobInventory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IMob {
	@Nullable ItemEntity drop(ItemStack stack, boolean throwRandomly, boolean retainOwnership);

	AbstractContainerMenu getContainerMenu();

	MobInventory getInventory();
}
