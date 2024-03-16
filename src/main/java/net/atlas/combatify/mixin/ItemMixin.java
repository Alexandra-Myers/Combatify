package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemExtensions {
	@Mutable
	@Shadow
	@Final
	private int maxDamage;

	@Inject(method = "verifyComponentsAfterLoad", at = @At(value = "HEAD"))
	public void editModifiers(ItemStack itemStack, CallbackInfo ci) {
		if (getTierFromConfig() != null)
			setDurability(itemStack, getTierFromConfig().getUses());
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(self())) {
			Integer durability = Combatify.ITEMS.configuredItems.get(self()).durability;
			if (durability != null)
				setDurability(itemStack, durability);
		}
		MethodHandler.updateModifiers(itemStack);
	}

	@Override
	public Item self() {
		return Item.class.cast(this);
	}
	public void setDurability(ItemStack stack, int value) {
		if (!stack.has(DataComponents.DAMAGE))
			stack.set(DataComponents.DAMAGE, 0);
		maxDamage = value;
	}
}
