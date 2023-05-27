package net.alexandra.atlas.atlas_combat.mixin;

import com.google.common.collect.ImmutableMultimap;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.IShieldItem;
import net.alexandra.atlas.atlas_combat.extensions.ISwordItem;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.alexandra.atlas.atlas_combat.item.WeaponType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SwordItem.class)
public class SwordItemMixin extends TieredItem implements ItemExtensions, IShieldItem, ISwordItem {
	public int strengthTimer = 0;

	public SwordItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
		if(AtlasCombat.CONFIG.swordBlocking()) {
			float f = getShieldBlockDamageValue(stack);
			float g = getShieldKnockbackResistanceValue(stack);
			tooltip.add((Component.literal("")).append(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.MULTIPLY_TOTAL.toValue(), new Object[]{ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((double) f * 100), Component.translatable("attribute.name.generic.sword_block_strength")})).withStyle(ChatFormatting.DARK_GREEN));
			if (g > 0.0F) {
				tooltip.add((Component.literal("")).append(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADDITION.toValue(), new Object[]{ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((double) (g * 10.0F)), Component.translatable("attribute.name.generic.knockback_resistance")})).withStyle(ChatFormatting.DARK_GREEN));
			}
		}
		super.appendHoverText(stack, world, tooltip, context);
	}
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;build()Lcom/google/common/collect/ImmutableMultimap;"))
	public ImmutableMultimap test(ImmutableMultimap.Builder instance) {
		ImmutableMultimap.Builder var3 = ImmutableMultimap.builder();
		WeaponType.SWORD.addCombatAttributes(this.getTier(), var3);
		return var3.build();
	}
	@Inject(method = "getDamage", at = @At(value = "RETURN"), cancellable = true)
	public void getDamage(CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(WeaponType.SWORD.getDamage(this.getTier()));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
		if(AtlasCombat.CONFIG.swordBlocking() && hand != InteractionHand.OFF_HAND) {
			strengthTimer = 0;
			ItemStack itemStack = user.getItemInHand(hand);
			ItemStack oppositeStack = user.getItemInHand(InteractionHand.OFF_HAND);
			if(user.isSprinting()) {
				user.setSprinting(false);
			}
			if(oppositeStack.isEmpty()) {
				user.startUsingItem(hand);
				return InteractionResultHolder.consume(itemStack);
			} else {
				return InteractionResultHolder.fail(itemStack);
			}
		}
		return super.use(world,user,hand);
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BLOCK;
	}

	@Override
	public double getAttackReach(Player player) {
		float var2 = 0.0F;
		float var3 = player.getAttackStrengthScale(1.0F);
		if (var3 > 1.95F && !player.isCrouching()) {
			var2 = 1.0F;
		}
		return WeaponType.SWORD.getReach() + 2.5 + var2;
	}

	@Override
	public double getAttackSpeed(Player player) {
		return WeaponType.SWORD.getSpeed(this.getTier()) + 4.0;
	}

	@Override
	public double getAttackDamage(Player player) {
		return WeaponType.SWORD.getDamage(this.getTier()) + 2.0;
	}

	@Override
	public void setStackSize(int stackSize) {
		this.maxStackSize = stackSize;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000;
	}
	@Override
	public float getShieldKnockbackResistanceValue(ItemStack itemStack) {
		return 0.0F;
	}

	@Override
	public float getShieldBlockDamageValue(ItemStack itemStack) {
		Tier var2 = getTier();
		float strengthIncrease = var2.getAttackDamageBonus() <= 1.0F ? -1F : 0.0F;
		strengthIncrease += AtlasCombat.CONFIG.swordProtectionEfficacy();
		strengthIncrease = Math.max(strengthIncrease, -3);
		return 0.5F + (strengthIncrease * 0.125F);
	}
	@Override
	public void addStrengthTimer() {
		++strengthTimer;
	}
	@Override
	public void subStrengthTimer() {
		--strengthTimer;
	}

	@Override
	public int getStrengthTimer() {
		return strengthTimer;
	}
}
