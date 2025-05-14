package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.component.CustomDataComponents;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.*;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
	public abstract boolean hasNonDefault(DataComponentType<?> dataComponentType);

	@Shadow
	public abstract boolean isEmpty();

	@Shadow
	public abstract <T extends TooltipProvider> void addToTooltip(DataComponentType<T> dataComponentType, Item.TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag);

	@Inject(method = "addDetailsToTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;addAttributeTooltips(Ljava/util/function/Consumer;Lnet/minecraft/world/item/component/TooltipDisplay;Lnet/minecraft/world/entity/player/Player;)V"))
	public void appendCanSweepTooltip(Item.TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Player player, TooltipFlag tooltipFlag, Consumer<Component> consumer, CallbackInfo ci) {
		this.addToTooltip(CustomDataComponents.CAN_SWEEP, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
	}

	@Inject(method = "addModifierTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;operation()Lnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;", ordinal = 0))
	public void addAttackReach(Consumer<Component> consumer, Player player, Holder<Attribute> holder, AttributeModifier attributeModifier, CallbackInfo ci, @Local(ordinal = 0) LocalDoubleRef d, @Local(ordinal = 0) LocalBooleanRef bl) {
		if (player != null) {
			if (attributeModifier.is(WeaponType.BASE_ATTACK_SPEED_CTS_ID)) {
				d.set(d.get() + player.getAttributeBaseValue(Attributes.ATTACK_SPEED) - 1.5);
				bl.set(true);
			}
			if (attributeModifier.is(WeaponType.BASE_ATTACK_REACH_ID)) {
				d.set(d.get() + player.getAttributeBaseValue(Attributes.ENTITY_INTERACTION_RANGE) + (Combatify.CONFIG.attackReach() ? 0 : 0.5));
				bl.set(true);
			}
		}
	}
	@Inject(method = "addAttributeTooltips", at = @At("RETURN"))
	public void addPiercing(Consumer<Component> consumer, TooltipDisplay tooltipDisplay, Player player, CallbackInfo ci) {
		if (tooltipDisplay.shows(DataComponents.ATTRIBUTE_MODIFIERS) && player != null) {
			double piercingLevel = MethodHandler.getPiercingLevel(this.stack);
			piercingLevel += CustomEnchantmentHelper.getBreach(this.stack, player.getRandom());
			piercingLevel = Mth.clamp(piercingLevel, 0, 1);
			if (piercingLevel > 0) {
				consumer.accept(CommonComponents.EMPTY);
				consumer.accept(Component.translatable("item.modifiers.mainhand").withStyle(ChatFormatting.GRAY));
				consumer.accept(
					CommonComponents.space().append(
						Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.id(),
							ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(piercingLevel * 100),
							Component.translatableWithFallback("attribute.name.armor_piercing", "Armor Piercing"))).withStyle(ChatFormatting.DARK_GREEN));
			}
			if (getBlocking(this.stack).canShowInTooltip(this.stack, player)) getBlocking(this.stack).tooltip().appendTooltipInfo(consumer, player, this.stack);
		}
	}
	@ModifyReturnValue(method = "getUseDuration", at = @At(value = "RETURN"))
	public int getUseDuration(int original) {
		if (hasNonDefault(DataComponents.CONSUMABLE)) return original;
		ConfigurableItemData configurableItemData = MethodHandler.forItem(getItem());
		if (configurableItemData != null) {
			if (configurableItemData.useDuration() != null)
				return (int) (configurableItemData.useDuration() * 20);
		}
		return original;
	}
}

