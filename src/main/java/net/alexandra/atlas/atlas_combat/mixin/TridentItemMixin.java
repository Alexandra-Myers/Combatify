package net.alexandra.atlas.atlas_combat.mixin;

import com.google.common.collect.ImmutableMultimap;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.alexandra.atlas.atlas_combat.item.WeaponType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TridentItem.class)
public class TridentItemMixin extends Item implements Vanishable, ItemExtensions {
	public TridentItemMixin(Item.Properties properties) {
		super(properties);
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;build()Lcom/google/common/collect/ImmutableMultimap;"),remap = false)
	public ImmutableMultimap<Attribute, AttributeModifier> test(ImmutableMultimap.Builder<Attribute, AttributeModifier> instance) {
		ImmutableMultimap.Builder<Attribute, AttributeModifier> var3 = ImmutableMultimap.builder();
		WeaponType.TRIDENT.addCombatAttributes(Tiers.NETHERITE, var3);
		return var3.build();
	}
	@Override
	public double getAttackReach(Player player) {
		float var2 = 0.0F;
		float var3 = player.getAttackStrengthScale(1.0F);
		if (var3 > 1.95F && !player.isCrouching()) {
			var2 = 1.0F;
		}
		return WeaponType.TRIDENT.getReach() + 2.5 + var2;
	}

	@Override
	public double getAttackSpeed(Player player) {
		return WeaponType.TRIDENT.getSpeed(Tiers.NETHERITE) + 4.0;
	}

	@Override
	public double getAttackDamage(Player player) {
		return WeaponType.TRIDENT.getDamage(Tiers.NETHERITE) + 2.0;
	}

	@Override
	public void setStackSize(int stackSize) {
		this.maxStackSize = stackSize;
	}
}
