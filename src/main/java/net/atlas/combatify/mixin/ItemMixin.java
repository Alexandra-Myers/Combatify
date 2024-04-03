package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.item.TieredShieldItem;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemExtensions {

	@Shadow
	@Final
	private DataComponentMap components;

	@Inject(method = "verifyComponentsAfterLoad", at = @At(value = "HEAD"))
	public void editModifiers(ItemStack itemStack, CallbackInfo ci) {
		boolean maxDamageChanged = false;
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(self())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(self());
			Integer durability = configurableItemData.durability;
			Integer maxStackSize = configurableItemData.stackSize;
			Tier tier = configurableItemData.tier;
			if (durability != null) {
				setDurability(itemStack, durability);
				maxDamageChanged = true;
			}
			if (maxStackSize != null
				&& ((!components.has(DataComponents.MAX_STACK_SIZE) && !itemStack.has(DataComponents.MAX_STACK_SIZE))
					|| Objects.equals(components.get(DataComponents.MAX_STACK_SIZE), itemStack.get(DataComponents.MAX_STACK_SIZE)))) {
				itemStack.set(DataComponents.MAX_STACK_SIZE, maxStackSize);
			}
			if (tier != null && self() instanceof DiggerItem && Objects.equals(components.get(DataComponents.TOOL), itemStack.get(DataComponents.TOOL))) {
				Tool original = components.get(DataComponents.TOOL);
				AtomicReference<Tool> tool = new AtomicReference<>();

                assert original != null;
                original.rules().forEach(rule -> {
					if (rule.blocks() instanceof HolderSet.Named<Block> named) {
						tool.set(tier.createToolProperties(named.key()));
					}
				});
				if (tool.get() != null)
					itemStack.set(DataComponents.TOOL, tool.get());
			}
		}
		if (!maxDamageChanged && getTierFromConfig() != null)
			setDurability(itemStack, getTierFromConfig().getUses());
		MethodHandler.updateModifiers(itemStack);
	}

	@Override
	public Item self() {
		return Item.class.cast(this);
	}
	@Unique
	public void setDurability(ItemStack stack, int value) {
		if (self() instanceof TieredShieldItem)
			value *= 2;
		if (!stack.has(DataComponents.DAMAGE))
			stack.set(DataComponents.DAMAGE, 0);
		if ((!components.has(DataComponents.MAX_DAMAGE) && !stack.has(DataComponents.MAX_DAMAGE)) || Objects.equals(components.get(DataComponents.MAX_DAMAGE), stack.get(DataComponents.MAX_DAMAGE))) {
			stack.set(DataComponents.MAX_DAMAGE, value);
		}
	}
}
