package net.atlas.combat_enhanced.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.atlas.combat_enhanced.CombatEnhanced;
import net.atlas.combat_enhanced.extensions.DefaultedItemExtensions;
import net.atlas.combat_enhanced.extensions.ItemExtensions;
import net.atlas.combat_enhanced.extensions.WeaponWithType;
import net.atlas.combat_enhanced.item.WeaponType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(DiggerItem.class)
public abstract class DiggerItemMixin extends TieredItem implements Vanishable, ItemExtensions, DefaultedItemExtensions, WeaponWithType {
	@Mutable
	@Final
	private WeaponType type;
	@Shadow
	private Multimap<Attribute, AttributeModifier> defaultModifiers;
	public DiggerItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}
	@Override
	public void modifyAttributeModifiers() {
		ImmutableMultimap.Builder<Attribute, AttributeModifier> var3 = ImmutableMultimap.builder();
		type = getWeaponType();
		type.addCombatAttributes(this.getTier(), var3);
		ImmutableMultimap<Attribute, AttributeModifier> output = var3.build();
		((DefaultedItemExtensions)this).setDefaultModifiers(output);
	}
	@Redirect(method = "hurtEnemy",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
	public <T extends LivingEntity> void damage(ItemStack instance, int amount, T entity, Consumer<T> breakCallback) {
		boolean bl = instance.getItem() instanceof AxeItem || instance.getItem() instanceof HoeItem;
		if (CombatEnhanced.CONFIG.toolsAreWeapons() || bl) {
			amount -= 1;
		}
		instance.hurtAndBreak(amount, entity, breakCallback);
	}
	@Override
	public double getAttackReach(Player player) {
		float var2 = 0.0F;
		float var3 = player.getAttackStrengthScale(1.0F);
		if (var3 > 1.95F && !player.isCrouching()) {
			var2 = 1.0F;
		}
		return type.getReach() + 2.5 + var2;
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
	public double getAttackSpeed(Player player) {
		return type.getSpeed(this.getTier()) + 4.0;
	}

	@Override
	public double getAttackDamage(Player player) {
		return type.getDamage(this.getTier()) + 2.0;
	}

	@Override
	public void setStackSize(int stackSize) {
		this.maxStackSize = stackSize;
	}

	@Override
	public WeaponType getWeaponType() {
		return WeaponType.PICKAXE;
	}
}
