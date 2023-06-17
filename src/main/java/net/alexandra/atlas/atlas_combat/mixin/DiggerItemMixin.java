package net.alexandra.atlas.atlas_combat.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.DefaultedItemExtensions;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.alexandra.atlas.atlas_combat.item.WeaponType;
import net.minecraft.tags.TagKey;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

import static net.alexandra.atlas.atlas_combat.item.WeaponType.AXE;

@Mixin(DiggerItem.class)
public class DiggerItemMixin extends TieredItem implements Vanishable, ItemExtensions, DefaultedItemExtensions {
	public boolean allToolsAreWeapons = AtlasCombat.CONFIG.toolsAreWeapons();
	@Mutable
	@Final
	private WeaponType type;
	@Shadow
	private Multimap<Attribute, AttributeModifier> defaultModifiers;
	public DiggerItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}

	@Inject(method = "<init>", at = @At(value = "TAIL"),remap = false)
	public void test(float f, float g, Tier tier, TagKey tagKey, Properties properties, CallbackInfo ci) {
		ImmutableMultimap.Builder<Attribute, AttributeModifier> var3 = ImmutableMultimap.builder();
		var digger = DiggerItem.class.cast(this);

		if (digger instanceof AxeItem) {
			type = AXE;
		} else if (digger instanceof PickaxeItem) {
			type = WeaponType.PICKAXE;
		} else if (digger instanceof ShovelItem) {
			type = WeaponType.SHOVEL;
		} else {
			type = WeaponType.HOE;
		}
		type.addCombatAttributes(this.getTier(), var3);
		((DefaultedItemExtensions)this).setDefaultModifiers(var3.build());
	}
	@Redirect(method = "hurtEnemy",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
	public <T extends LivingEntity> void damage(ItemStack instance, int amount, T entity, Consumer<T> breakCallback) {
		boolean bl = instance.getItem() instanceof AxeItem || instance.getItem() instanceof HoeItem;
		if(allToolsAreWeapons || bl) {
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
}
