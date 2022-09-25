package net.alexandra.atlas.atlas_combat.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.alexandra.atlas.atlas_combat.enchantment.CleavingEnchantment;
import net.alexandra.atlas.atlas_combat.extensions.CustomEnchantment;
import net.alexandra.atlas.atlas_combat.extensions.IEnchantmentHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

import static net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin implements IEnchantmentHelper {
	private static Enchantment currentEnchantment;
	private static ItemStack itemStack;
	@Shadow
	public static float getDamageBonus(ItemStack par1, MobType par2) {
		return 0;
	}

	@Override
	public float getDamageBonus(ItemStack level, LivingEntity entity){
		if(entity.getMobType() == MobType.WATER || entity.isInWaterOrRain()) {
			return getDamageBonus(level, MobType.WATER);
		}
		return getDamageBonus(level, entity.getMobType());
	}

	@Override
	public float getKnockbackDebuff(ItemStack level, LivingEntity entity){
		return (getDamageBonus(level, MobType.WATER)/2.5F);
	}
	@Override
	public int getFullEnchantmentLevel(Enchantment enchantment, LivingEntity entity) {
		Iterable<ItemStack> iterable = enchantment.getSlotItems(entity).values();
		if (iterable == null) {
			return 0;
		} else {
			int i = 0;

			for(ItemStack itemStack : iterable) {
				int j = getItemEnchantmentLevel(enchantment, itemStack);
				i += j;
			}

			return i;
		}
	}
	@Inject(method = "getAvailableEnchantmentResults", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;isTreasureOnly()Z"), locals = LocalCapture.CAPTURE_FAILHARD)
	private static void extractEnchantment(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentInstance>> cir, List<EnchantmentInstance> list, Item item, boolean bl, Iterator iterator, Enchantment enchantment) {
		currentEnchantment = enchantment;
		itemStack = stack;
	}
	@ModifyExpressionValue(method = "getAvailableEnchantmentResults", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentCategory;canEnchant(Lnet/minecraft/world/item/Item;)Z"))
	private static boolean redirectCanEnchant(boolean original) {
		return currentEnchantment instanceof CustomEnchantment customEnchantment && itemStack != null ? customEnchantment.isAcceptibleConditions(itemStack) : original;
	}
}
