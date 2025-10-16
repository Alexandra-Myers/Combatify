package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.component.CustomDataComponents;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.core.component.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static net.atlas.combatify.util.MethodHandler.getBlocking;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder {

	@Unique
	public ItemStack stack = ItemStack.class.cast(this);

	@Shadow
	public abstract Item getItem();

	@Shadow
	public abstract String toString();

	@Shadow
	public abstract <T extends TooltipProvider> void addToTooltip(DataComponentType<T> dataComponentType, Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag);

	@Shadow
	public abstract boolean isEmpty();

	@Shadow
	public abstract DataComponentPatch getComponentsPatch();

	@Shadow
	public abstract int getUseDuration(LivingEntity livingEntity);

	@Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/util/AttributeUtil;addAttributeTooltips(Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;Lnet/neoforged/neoforge/common/util/AttributeTooltipContext;)V"))
	public void appendCanSweepTooltip(Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir, @Local(ordinal = 0) Consumer<Component> consumer) {
		addToTooltip(CustomDataComponents.CAN_SWEEP, tooltipContext, consumer, tooltipFlag);
	}
	@ModifyReturnValue(method = "getUseDuration", at = @At(value = "RETURN"))
	public int getUseDuration(int original) {
		if (getBlocking(this.stack).canOverrideUseDurationAndAnimation(this.stack)) return getBlocking(this.stack).useTicks();
		//noinspection OptionalAssignedToNull
		if (getComponentsPatch().get(DataComponents.FOOD) != null) return original;
		ConfigurableItemData configurableItemData = MethodHandler.forItem(getItem());
		if (configurableItemData != null) {
			if (configurableItemData.useDuration() != null)
				return (int) (configurableItemData.useDuration() * 20);
		}
		return original;
	}
	@ModifyReturnValue(method = "useOn", at = @At(value = "RETURN"))
	public InteractionResult addBlockAbility(InteractionResult original, @Local(ordinal = 0, argsOnly = true) UseOnContext useOnContext) {
		InteractionResult result;
		ItemStack stack = Objects.requireNonNull(useOnContext.getPlayer()).getItemInHand(useOnContext.getHand());
		result = getBlocking(stack).use(stack, useOnContext.getLevel(), useOnContext.getPlayer(), useOnContext.getHand(), original);
		ConfigurableItemData configurableItemData = MethodHandler.forItem(getItem());
		if (configurableItemData != null && getUseDuration(useOnContext.getPlayer()) == 0 && (original.consumesAction() || (result != null && result.consumesAction()))) {
			if (configurableItemData.cooldownSeconds() != null)
				MethodHandler.getCooldowns(useOnContext.getPlayer()).addCooldown(getItem(), (int) (configurableItemData.cooldownSeconds() * 20.0F));
		}
		if (result != null)
			return result;
		return original;
	}

	@ModifyReturnValue(method = "use", at = @At(value = "RETURN"))
	public InteractionResultHolder<ItemStack> addBlockAbility(InteractionResultHolder<ItemStack> original, @Local(ordinal = 0, argsOnly = true) Level world, @Local(ordinal = 0, argsOnly = true) Player player, @Local(ordinal = 0, argsOnly = true) InteractionHand hand) {
		InteractionResult result = getBlocking(stack).use(stack, world, player, hand, original.getResult());
		ConfigurableItemData configurableItemData = MethodHandler.forItem(getItem());
		if (configurableItemData != null && getUseDuration(player) == 0 && (original.getResult().consumesAction() || (result != null && result.consumesAction()))) {
			if (configurableItemData.cooldownSeconds() != null)
				MethodHandler.getCooldowns(player).addCooldown(getItem(), (int) (configurableItemData.cooldownSeconds() * 20.0F));
		}
		if (result != null)
			return new InteractionResultHolder<>(result, stack);
		return original;
	}

	@Inject(method = "releaseUsing", at = @At("RETURN"))
	public void addCooldown(Level level, LivingEntity livingEntity, int i, CallbackInfo ci) {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(getItem());
		if (configurableItemData != null) {
			if (configurableItemData.cooldownSeconds() != null)
				MethodHandler.getCooldowns(livingEntity).addCooldown(getItem(), (int) (configurableItemData.cooldownSeconds() * 20.0F));
		}
	}
	@ModifyReturnValue(method = "getUseAnimation", at = @At(value = "RETURN"))
	public UseAnim addBlockAnim(UseAnim original) {
		if (getBlocking(this.stack).canOverrideUseDurationAndAnimation(this.stack))
			return UseAnim.BLOCK;
		return original;
	}
}

