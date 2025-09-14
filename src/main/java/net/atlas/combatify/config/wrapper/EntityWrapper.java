package net.atlas.combatify.config.wrapper;

import net.atlas.combatify.Combatify;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EntityWrapper<E extends Entity> implements GenericAPIWrapper<E> {
	public final E value;

	public EntityWrapper(E value) {
		this.value = value;
	}

	public final boolean matchesTag(String tag) {
		return value.getType().is(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(tag)));
	}

	public final String getType() {
		return BuiltInRegistries.ENTITY_TYPE.getKey(value.getType()).toString();
	}

	public final boolean isLivingEntity() {
		return value instanceof LivingEntity;
	}

	public final RandomSourceWrapper getLevelRandom() {
		return new RandomSourceWrapper(value.level().getRandom());
	}
	public final RandomSourceWrapper getRandom() {
		return new RandomSourceWrapper(value.getRandom());
	}

	public final void kill() {
		value.kill();
	}

	public final void discard() {
		value.discard();
	}

	public final Vec2 getRotationVector() {
		return value.getRotationVector();
	}

	public final Vec3 getEyePosition() {
		return value.getEyePosition();
	}

	public final Vec3 position() {
		return value.position();
	}

	public final void teleportTo(double d, double e, double f) {
		value.teleportTo(d, e, f);
	}

	public final Vec3 getDeltaMovement() {
		return value.getDeltaMovement();
	}

	public final void setDeltaMovement(double d, double e, double f) {
		value.setDeltaMovement(d, e, f);
	}

	public final void addDeltaMovement(double d, double e, double f) {
		value.addDeltaMovement(new Vec3(d, e, f));
	}

	public final void igniteForSeconds(float seconds) {
		value.igniteForSeconds(seconds);
	}

	public final void extinguishFire() {
		value.extinguishFire();
	}

	public final void playSound(String soundEvent, float f, float g) {
		value.playSound(Objects.requireNonNull(BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(soundEvent))), f, g);
	}

	public final void playSound(String soundEvent) {
		value.playSound(Objects.requireNonNull(BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(soundEvent))));
	}

	public final float getFallDistance() {
		return value.fallDistance;
	}

	public final boolean isFreezing() {
		return value.isFreezing();
	}

	public final boolean isSilent() {
		return value.isSilent();
	}

	public final void setSilent(boolean bl) {
		value.setSilent(bl);
	}

	public final boolean isNoGravity() {
		return value.isNoGravity();
	}

	public final void setNoGravity(boolean bl) {
		value.setNoGravity(bl);
	}

	public final double getGravity() {
		return value.getGravity();
	}

	public final double distanceToSqr(EntityWrapper<?> other) {
		return value.distanceToSqr(other.value);
	}

	public final float distanceTo(EntityWrapper<?> other) {
		return value.distanceTo(other.value);
	}

	public final double distanceToSqr(double d, double e, double f) {
		return value.distanceToSqr(d, e, f);
	}

	public final DamageSource createDamageSourceNoCause(String damageType) {
		return value.damageSources().source(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse(damageType)));
	}

	public final DamageSource createDamageSourceAsCause(String damageType) {
		return value.damageSources().source(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse(damageType)), value);
	}

	public final boolean hurt(DamageSource damageSource, float amount) {
		return value.hurt(damageSource, amount);
	}

	public final boolean onGround() {
		return value.onGround();
	}

	public final boolean isInWater() {
		return value.isInWater();
	}

	public final boolean isInWaterOrRain() {
		return value.isInWaterOrRain();
	}

	public final boolean isPickable() {
		return value.isPickable();
	}

	public final boolean isOnFire() {
		return value.isOnFire();
	}

	public final boolean isPassenger() {
		return value.isPassenger();
	}

	public final boolean isVehicle() {
		return value.isVehicle();
	}

	public final boolean dismountsUnderwater() {
		return value.dismountsUnderwater();
	}

	public final boolean canControlVehicle() {
		return value.canControlVehicle();
	}

	public final void setShiftKeyDown(boolean down) {
		value.setShiftKeyDown(down);
	}

	public final boolean isShiftKeyDown() {
		return value.isShiftKeyDown();
	}

	public final boolean isSteppingCarefully() {
		return value.isSteppingCarefully();
	}

	public final boolean isSuppressingBounce() {
		return value.isSuppressingBounce();
	}

	public final boolean isDiscrete() {
		return value.isDiscrete();
	}

	public final boolean isDescending() {
		return value.isDescending();
	}

	public final boolean isCrouching() {
		return value.isCrouching();
	}

	public final boolean isSprinting() {
		return value.isSprinting();
	}

	public final void setSprinting(boolean bl) {
		value.setSprinting(bl);
	}

	public final boolean isSwimming() {
		return value.isSwimming();
	}

	public final boolean isVisuallySwimming() {
		return value.isVisuallySwimming();
	}

	public final boolean isVisuallyCrawling() {
		return value.isVisuallyCrawling();
	}

	public final void setSwimming(boolean bl) {
		value.setSwimming(bl);
	}

	public final boolean hasGlowingTag() {
		return value.hasGlowingTag();
	}

	public final void setGlowingTag(boolean bl) {
		value.setGlowingTag(bl);
	}

	public final boolean isCurrentlyGlowing() {
		return value.isCurrentlyGlowing();
	}

	public final boolean isInvisible() {
		return value.isInvisible();
	}

	public final List<EntityWrapper<Entity>> getPassengers() {
		return value.getPassengers().stream().map(EntityWrapper::new).toList();
	}

	public final void executeFunction(String command) {
		ResourceLocation function = ResourceLocation.parse(command);
		if (value.level() instanceof ServerLevel serverLevel) {
			MinecraftServer minecraftServer = serverLevel.getServer();
			ServerFunctionManager serverFunctionManager = minecraftServer.getFunctions();
			Optional<CommandFunction<CommandSourceStack>> optional = serverFunctionManager.get(function);
			if (optional.isPresent()) {
				CommandSourceStack commandSourceStack = minecraftServer.createCommandSourceStack()
					.withPermission(2)
					.withSuppressedOutput()
					.withEntity(value)
					.withLevel(serverLevel)
					.withPosition(position())
					.withRotation(value.getRotationVector());
				serverFunctionManager.execute(optional.get(), commandSourceStack);
			} else {
				Combatify.LOGGER.unwrap().error("Running MCFunction in JavaScript failed for non-existent function {}", function);
			}
		}
	}

	@Override
	public E unwrap() {
		return value;
	}
}
