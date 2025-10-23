package net.atlas.combatify.mixin;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.atlas.combatify.item.NewAttributes;
import net.atlas.combatify.util.CombatUtil;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(ReachEntityAttributes.class)
public class ReachEntityAttributesMixin {
	@Inject(method = "getAttackRange", at = @At(value = "HEAD"), cancellable = true)
	private static void getRealReach(LivingEntity entity, double baseAttackRange, CallbackInfoReturnable<Double> cir) {
		if (entity instanceof Player player)
			cir.setReturnValue(MethodHandler.getCurrentAttackReach(player, 1.0F));
	}
	@Inject(method = "isWithinAttackRange", at = @At(value = "HEAD"), cancellable = true)
	private static void getTrueReach(Player player, Entity entity, CallbackInfoReturnable<Boolean> cir) {
		double d = MethodHandler.getCurrentAttackReach(player, 1.0F) + 1;
		d *= d;
		if(!player.hasLineOfSight(entity)) {
			d = 6.25;
		}

		AABB aABB = entity.getBoundingBox();
		Vec3 eyePos = player.getEyePosition(0.0F);
		double dist = eyePos.distanceToSqr(MethodHandler.getNearestPointTo(aABB, eyePos));
		if (entity instanceof ServerPlayer target && player instanceof ServerPlayer attacker) {
			if (CombatUtil.allowReach(attacker, target)) {
				dist = 0;
			} else {
				dist = Integer.MAX_VALUE;
			}
		}
		cir.setReturnValue(dist < d);
	}
	@Inject(method = "make", at = @At(value = "HEAD"), cancellable = true)
	private static void deleteAttackRange(String name, double base, double min, double max, CallbackInfoReturnable<Attribute> cir) {
		if(Objects.equals(name, "attack_range"))
			cir.setReturnValue(NewAttributes.ATTACK_REACH);
	}
	@Redirect(method = "onInitialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;register(Lnet/minecraft/core/Registry;Lnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 1))
	private <V, T extends V> T disableRegister(Registry<V> registry, ResourceLocation resourceLocation, T object) {
		return object;
	}
	@Inject(method = "onInitialize", at = @At("RETURN"), remap = false)
	private void addAliasAttackReach(CallbackInfo ci) {
		var holder = BuiltInRegistries.ATTRIBUTE
			.getHolderOrThrow(BuiltInRegistries.ATTRIBUTE.getResourceKey(NewAttributes.ATTACK_REACH).orElseThrow());
		@SuppressWarnings("unchecked")
		var reg = (MappedRegistryAccessor<Attribute>) BuiltInRegistries.ATTRIBUTE;
		var loc = new ResourceLocation(ReachEntityAttributes.MOD_ID, "attack_range");
		var key = ResourceKey.create(Registries.ATTRIBUTE, loc);
		reg.getByKey().put(key, holder);
		reg.getByLocation().put(loc, holder);
	}
}
