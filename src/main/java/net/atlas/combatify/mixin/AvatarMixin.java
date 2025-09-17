package net.atlas.combatify.mixin;

import net.atlas.combatify.extensions.ClientInformationHolder;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Avatar.class)
public abstract class AvatarMixin extends LivingEntity implements ClientInformationHolder {
	/**
	 * This is a crime, I know,
	 * But it's okay we have to do this to fix a CTS bug
	 */
	@SuppressWarnings("WrongEntityDataParameterClass")
	@Unique
	private static final EntityDataAccessor<Boolean> DATA_PLAYER_USES_SHIELD_CROUCH = SynchedEntityData.defineId(Avatar.class, EntityDataSerializers.BOOLEAN);
	@Shadow
	@Final
	protected static EntityDataAccessor<Byte> DATA_PLAYER_MODE_CUSTOMISATION;
	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	public void appendShieldOnCrouch(SynchedEntityData.Builder builder, CallbackInfo ci) {
		builder.define(DATA_PLAYER_USES_SHIELD_CROUCH, true);
	}
	protected AvatarMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public boolean combatify$hasEnabledShieldOnCrouch() {
		return entityData.get(DATA_PLAYER_USES_SHIELD_CROUCH);
	}

	@Override
	public void combatify$setShieldOnCrouch(boolean hasShieldOnCrouch) {
		entityData.set(DATA_PLAYER_USES_SHIELD_CROUCH, hasShieldOnCrouch);
	}
}
