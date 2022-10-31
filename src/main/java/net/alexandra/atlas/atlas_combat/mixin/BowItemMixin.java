package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.config.ConfigHelper;
import net.alexandra.atlas.atlas_combat.extensions.IBowItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public abstract class BowItemMixin extends ProjectileWeaponItem implements IBowItem {

	@Unique
	public final float configUncertainty = AtlasCombat.CONFIG.bowUncertainty();
	@Shadow
	public static float getPowerForTime(int useTicks) {
		return 0;
	}


	public BowItemMixin(Properties properties) {
		super(properties);
	}
	@Inject(method = "releaseUsing", at = @At(value = "HEAD"), cancellable = true)
	public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
		stopUsing(stack, world, user, remainingUseTicks);
		ci.cancel();
	}
	@Override
	public void stopUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
		if (user instanceof Player player) {
			boolean bl = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
			ItemStack itemStack = player.getProjectile(stack);
			if (!itemStack.isEmpty() || bl) {
				if (itemStack.isEmpty()) {
					itemStack = new ItemStack(Items.ARROW);
				}

				int i = this.getUseDuration(stack) - remainingUseTicks;
				float f = getPowerForTime(i);
				if (!((double)f < 0.1)) {
					float fatigue = getFatigueForTime(i);
					boolean bl2 = bl && itemStack.is(Items.ARROW);
					if (!world.isClientSide) {
						ArrowItem arrowItem = (ArrowItem)(itemStack.getItem() instanceof ArrowItem ? itemStack.getItem() : Items.ARROW);
						AbstractArrow abstractArrow = arrowItem.createArrow(world, itemStack, player);
						abstractArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, f * 3.0F, configUncertainty * fatigue);
						if (f == 1.0F && fatigue <= 0.5F) {
							abstractArrow.setCritArrow(true);
						}

						int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
						if (j > 0) {
							abstractArrow.setBaseDamage(abstractArrow.getBaseDamage() + (double)j * 0.5 + 0.5);
						}

						int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
						if (k > 0) {
							abstractArrow.setKnockback(k);
						}

						if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
							abstractArrow.setSecondsOnFire(100);
						}

						stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
						if (bl2 || player.getAbilities().instabuild && (itemStack.getItem() instanceof SpectralArrowItem || itemStack.getItem() instanceof TippedArrowItem)) {
							abstractArrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
						}

						world.addFreshEntity(abstractArrow);
					}

					world.playSound(
							null,
							player.getX(),
							player.getY(),
							player.getZ(),
							SoundEvents.ARROW_SHOOT,
							SoundSource.PLAYERS,
							1.0F,
							1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F
					);
					if (!bl2 && !player.getAbilities().instabuild) {
						itemStack.shrink(1);
						if (itemStack.isEmpty()) {
							player.getInventory().removeItem(itemStack);
						}
					}

					player.awardStat(Stats.ITEM_USED.get(this));
				}
			}
		}
	}

	@Override
	public float getFatigueForTime(int f) {
		if (f < 60) {
			return 0.5F;
		} else {
			return f >= 200 ? 10.5F : 0.5F + 10.0F * (float)(f - 60) / 140.0F;
		}
	}
}
