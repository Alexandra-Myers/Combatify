package net.alexandra.atlas.atlas_combat.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.alexandra.atlas.atlas_combat.extensions.DefaultedItemExtensions;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.alexandra.atlas.atlas_combat.extensions.WeaponWithType;
import net.alexandra.atlas.atlas_combat.item.WeaponType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentItem.class)
public class TridentItemMixin extends Item implements Vanishable, ItemExtensions, DefaultedItemExtensions, WeaponWithType {
	@Mutable
	@Shadow
	@Final
	private Multimap<Attribute, AttributeModifier> defaultModifiers;

	public TridentItemMixin(Item.Properties properties) {
		super(properties);
	}

	@Inject(method = "<init>", at = @At(value = "TAIL"),remap = false)
	public void test(Properties properties, CallbackInfo ci) {
		ImmutableMultimap.Builder<Attribute, AttributeModifier> var3 = ImmutableMultimap.builder();
		getWeaponType().addCombatAttributes(Tiers.NETHERITE, var3);
		((DefaultedItemExtensions)this).setDefaultModifiers(var3.build());
	}
	@Override
	public double getAttackReach(Player player) {
		float var2 = 0.0F;
		float var3 = player.getAttackStrengthScale(1.0F);
		if (var3 > 1.95F && !player.isCrouching()) {
			var2 = 1.0F;
		}
		return getWeaponType().getReach() + 2.5 + var2;
	}

	@Override
	public double getAttackSpeed(Player player) {
		return getWeaponType().getSpeed(Tiers.NETHERITE) + 4.0;
	}

	@Override
	public double getAttackDamage(Player player) {
		return getWeaponType().getDamage(Tiers.NETHERITE) + 2.0;
	}

	@Override
	public void setStackSize(int stackSize) {
		this.maxStackSize = stackSize;
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultModifiers() {
		return defaultModifiers;
	}

	@Override
	public void setDefaultModifiers(ImmutableMultimap<Attribute, AttributeModifier> modifiers) {
		defaultModifiers = modifiers;
	}

	@Override
	public WeaponType getWeaponType() {
		return WeaponType.TRIDENT;
	}
}
