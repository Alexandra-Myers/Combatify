package net.atlas.combatify.config.wrapper;

import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record ItemStackWrapper(ItemStack value) implements GenericAPIWrapper<ItemStack> {
	public ItemWrapper getItem() {
		return new ItemWrapper(value.getItem());
	}
	public int getCount() {
		return value.getCount();
	}
	public PatchedDataComponentMapWrapper getComponents(Level level) {
		return new PatchedDataComponentMapWrapper(RegistryOps.create(NbtOps.INSTANCE, level.registryAccess()), Registries.DATA_COMPONENT_TYPE, (PatchedDataComponentMap) value.getComponents());
	}
	@Override
	public ItemStack unwrap() {
		return value;
	}
}
