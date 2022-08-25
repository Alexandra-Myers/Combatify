package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.IMob;
import net.alexandra.atlas.atlas_combat.extensions.LivingEntityExtensions;
import net.alexandra.atlas.atlas_combat.util.MobInventory;
import net.minecraft.core.GlobalPos;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity implements IMob {
//	Mob mob = ((Mob)(Object)this);
//	public AbstractContainerMenu containerMenu;
//	public final MobInventory inventory = new MobInventory(mob);

	protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}
//	@Override
//	public void die(DamageSource source) {
//		super.die(source);
//		this.reapplyPosition();
//		if (!this.isSpectator()) {
//			this.dropAllDeathLoot(source);
//		}
//		this.clearFire();
//		this.setSharedFlagOnFire(false);
//	}
	@Redirect(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
	public void redirectKnockback(LivingEntity instance, double strength, double x, double z) {
		((LivingEntityExtensions)instance).newKnockback((float)strength, x, z);
	}
//	@Override
//	protected void dropEquipment() {
//		super.dropEquipment();
//		this.inventory.dropAll();
//	}
//	/**
//	 * @param throwRandomly if true, the item will be thrown in a random direction from the entity regardless of which direction the entity is facing
//	 */
//	@Nullable
//	@Override
//	public ItemEntity drop(ItemStack stack, boolean throwRandomly, boolean retainOwnership) {
//		if (stack.isEmpty() || mob.level == null) {
//			return null;
//		} else {
//			double d = mob.getEyeY() - 0.3F;
//			ItemEntity itemEntity = new ItemEntity(mob.level, mob.getX(), d, mob.getZ(), stack);
//			itemEntity.setPickUpDelay(40);
//			if (retainOwnership) {
//				itemEntity.setThrower(mob.getUUID());
//			}
//
//			if (throwRandomly) {
//				float f = mob.getRandom().nextFloat() * 0.5F;
//				float g = mob.getRandom().nextFloat() * (float) (Math.PI * 2);
//				itemEntity.setDeltaMovement((double)(-Mth.sin(g) * f), 0.2F, (double)(Mth.cos(g) * f));
//			} else {
//				float f = 0.3F;
//				float g = Mth.sin(mob.getXRot() * (float) (Math.PI / 180.0));
//				float h = Mth.cos(mob.getXRot() * (float) (Math.PI / 180.0));
//				float i = Mth.sin(mob.getYRot() * (float) (Math.PI / 180.0));
//				float j = Mth.cos(mob.getYRot() * (float) (Math.PI / 180.0));
//				float k = mob.getRandom().nextFloat() * (float) (Math.PI * 2);
//				float l = 0.02F * mob.getRandom().nextFloat();
//				itemEntity.setDeltaMovement(
//						(double)(-i * h * 0.3F) + Math.cos((double)k) * (double)l,
//						(double)(-g * 0.3F + 0.1F + (mob.getRandom().nextFloat() - mob.getRandom().nextFloat()) * 0.1F),
//						(double)(j * h * 0.3F) + Math.sin((double)k) * (double)l
//				);
//			}
//
//			return itemEntity;
//		}
//	}
//
//	@Override
//	public AbstractContainerMenu getContainerMenu() {
//		return containerMenu;
//	}
//
//	@Override
//	public MobInventory getInventory() {
//		return inventory;
//	}
}
