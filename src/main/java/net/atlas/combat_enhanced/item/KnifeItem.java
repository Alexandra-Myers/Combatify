package net.atlas.combat_enhanced.item;

import net.minecraft.world.item.Tier;

public class KnifeItem extends AbstractKnifeItem {
	public KnifeItem(Tier tier, Properties properties) {
		super(tier, properties);
	}

	@Override
	public void setStackSize(int stackSize) {
		this.maxStackSize = stackSize;
	}
}
