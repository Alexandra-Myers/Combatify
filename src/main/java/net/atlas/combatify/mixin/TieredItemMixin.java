package net.atlas.combatify.mixin;

import net.atlas.combatify.extensions.WeaponWithType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(TieredItem.class)
public abstract class TieredItemMixin extends Item implements WeaponWithType {
	@Shadow
	public abstract Tier getTier();

	@Mutable
	@Shadow
	@Final
	private Tier tier;

	public TieredItemMixin(Properties properties) {
		super(properties);
	}

	@Override
	public Item combatify$self() {
		return this;
	}

	@Override
	public Tier getConfigTier() {
		Optional<Tier> tier = getTierFromConfig();
		if (tier.isPresent()) {
			this.tier = tier.get();
			return tier.get();
		}
		return getTier();
	}
}
