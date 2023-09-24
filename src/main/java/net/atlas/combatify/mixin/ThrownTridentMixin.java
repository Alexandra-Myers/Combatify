package net.atlas.combatify.mixin;

import net.atlas.combatify.util.CustomEnchantmentHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.atlas.combatify.util.MethodHandler.voidReturnLogic;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends AbstractArrow {

	@Shadow
	@Final
	private static EntityDataAccessor<Byte> ID_LOYALTY;

	@Shadow
	public ItemStack tridentItem;
	@Unique
	public LivingEntity livingEntity;

	protected ThrownTridentMixin(EntityType<? extends AbstractArrow> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "tick", at = @At(value = "HEAD"))
	public void injectVoidReturnLogic(CallbackInfo ci) {
		voidReturnLogic(ThrownTrident.class.cast(this), ID_LOYALTY);
	}
	@Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageBonus(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/MobType;)F"))
	public void extractTarget(EntityHitResult p_37573_, CallbackInfo ci) {
		Entity entity = p_37573_.getEntity();
		if(entity instanceof LivingEntity livingEntity1) {
			livingEntity = livingEntity1;
		}
	}
	@Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageBonus(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/MobType;)F"))
	public float dealtDamage(ItemStack p_44834_, MobType p_44835_) {
		return CustomEnchantmentHelper.getDamageBonus(this.tridentItem, livingEntity);
	}

}
