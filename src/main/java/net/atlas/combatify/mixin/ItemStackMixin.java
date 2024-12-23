package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder {
	@Shadow
	public abstract Item getItem();

	@Shadow
	public abstract String toString();

	@Inject(method = "addModifierTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;operation()Lnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;", ordinal = 0))
	public void addAttackReach(Consumer<Component> consumer, Player player, Holder<Attribute> holder, AttributeModifier attributeModifier, CallbackInfo ci, @Local(ordinal = 0) LocalDoubleRef d, @Local(ordinal = 0) LocalBooleanRef bl) {
		if (player != null) {
			if (attributeModifier.id() == WeaponType.BASE_ATTACK_SPEED_CTS_ID) {
				d.set(d.get() + player.getAttributeBaseValue(Attributes.ATTACK_SPEED) - 1.5);
				bl.set(true);
			}
			if (attributeModifier.id() == WeaponType.BASE_ATTACK_REACH_ID) {
				d.set(d.get() + player.getAttributeBaseValue(Attributes.ENTITY_INTERACTION_RANGE) + (Combatify.CONFIG.attackReach() ? 0 : 0.5));
				bl.set(true);
			}
		}
	}
	@Inject(method = "addAttributeTooltips", at = @At("RETURN"))
	public void addPiercing(Consumer<Component> consumer, Player player, CallbackInfo ci) {
		ItemAttributeModifiers itemAttributeModifiers = getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		if (itemAttributeModifiers.showInTooltip() && player != null) {
			double piercingLevel = MethodHandler.getPiercingLevel(ItemStack.class.cast(this));
			piercingLevel += CustomEnchantmentHelper.getBreach(ItemStack.class.cast(this), player.getRandom());
			piercingLevel = Mth.clamp(piercingLevel, 0, 1);
			if (piercingLevel > 0) {
				consumer.accept(CommonComponents.EMPTY);
				consumer.accept(Component.translatable("item.modifiers.mainhand").withStyle(ChatFormatting.GRAY));
				consumer.accept(
					CommonComponents.space().append(
						Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.id(),
							ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(piercingLevel * 100),
							Component.translatable("attribute.name.armor_piercing"))).withStyle(ChatFormatting.DARK_GREEN));
			}
			Item item = getItem();
			if (!item.combatify$getBlockingType().requiresSwordBlocking() || Combatify.CONFIG.swordBlocking())
				item.combatify$getBlockingType().appendTooltipInfo(consumer, player, ItemStack.class.cast(this));
		}
	}
	@ModifyReturnValue(method = "getUseDuration", at = @At(value = "RETURN"))
	public int getUseDuration(int original) {
		Item item = getItem();
		ConfigurableItemData configurableItemData = MethodHandler.forItem(item);
		if (configurableItemData != null) {
			if (configurableItemData.useDuration() != null)
				return configurableItemData.useDuration();
		} else if (!item.combatify$getBlockingType().isEmpty() && (!item.combatify$getBlockingType().requiresSwordBlocking() || Combatify.CONFIG.swordBlocking()))
			return 72000;
		return original;
	}
	@ModifyReturnValue(method = "useOn", at = @At(value = "RETURN"))
	public InteractionResult addBlockAbility(InteractionResult original, @Local(ordinal = 0, argsOnly = true) UseOnContext useOnContext) {
		InteractionResult holder = null;
		Item item = Objects.requireNonNull(useOnContext.getPlayer()).getItemInHand(useOnContext.getHand()).getItem();
		if (!item.combatify$getBlockingType().isEmpty() && original == InteractionResult.PASS)
			if (item.combatify$getBlockingType().canUse(useOnContext.getLevel(), useOnContext.getPlayer(), useOnContext.getHand()))
				holder = item.combatify$getBlockingType().use(useOnContext.getLevel(), useOnContext.getPlayer(), useOnContext.getHand());
		if (holder != null)
			return holder;
		return original;
	}

	@ModifyReturnValue(method = "use", at = @At(value = "RETURN"))
	public InteractionResult addBlockAbility(InteractionResult original, @Local(ordinal = 0, argsOnly = true) Level world, @Local(ordinal = 0, argsOnly = true) Player player, @Local(ordinal = 0, argsOnly = true) InteractionHand hand) {
		InteractionResult holder = null;
		Item item = player.getItemInHand(hand).getItem();
		if (!item.combatify$getBlockingType().isEmpty() && original == InteractionResult.PASS)
			if(item.combatify$getBlockingType().canUse(world, player, hand))
				holder = item.combatify$getBlockingType().use(world, player, hand);
		if (holder != null)
			return holder;
		return original;
	}
	@ModifyReturnValue(method = "getUseAnimation", at = @At(value = "RETURN"))
	public ItemUseAnimation addBlockAnim(ItemUseAnimation original) {
		if (original == ItemUseAnimation.NONE)
			if (!getItem().combatify$getBlockingType().isEmpty())
				return ItemUseAnimation.BLOCK;
		return original;
	}
}

