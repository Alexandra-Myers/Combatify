package net.atlas.combatify.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.client.ClientMethodHandler;
import net.atlas.combatify.config.wrapper.PlayerWrapper;
import net.atlas.combatify.extensions.MinecraftExtensions;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.atlas.combatify.util.MethodHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.atlas.combatify.util.MethodHandler.getBlockingItem;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements PlayerExtensions {
	public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
	}

	@Shadow
	public abstract void startUsingItem(@NotNull InteractionHand interactionHand);
	@Unique
	boolean wasShieldBlocking = false;
	@Unique
	InteractionHand shieldBlockingHand = InteractionHand.OFF_HAND;
	@Shadow
	@Final
	public ClientPacketListener connection;

	@Shadow
	public abstract boolean isUsingItem();

	@Shadow
	@Final
	protected Minecraft minecraft;
	@Unique
	LocalPlayer thisPlayer = (LocalPlayer)(Object)this;
	@Environment(EnvType.CLIENT)
	@Inject(method = "tick", at = @At("RETURN"))
	public void injectSneakShield(CallbackInfo ci) {
		if (this.connection.hasClientLoaded()) {
			boolean isBlocking = isBlocking();
			if (isBlocking != wasShieldBlocking) {
				wasShieldBlocking = isBlocking;
				if (isBlocking) shieldBlockingHand = getBlockingItem(thisPlayer).useHand();
				if (isBlocking && !isUsingItem())
					minecraft.gameRenderer.itemInHandRenderer.itemUsed(shieldBlockingHand);
			}
		}
	}

	@Override
	public void combatify$customSwing(InteractionHand interactionHand) {
		swing(interactionHand, false);
		connection.send(new ServerboundSwingPacket(interactionHand));
	}

	@WrapOperation(method = "isSprintingPossible", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEnoughFoodToDoExhaustiveManoeuvres()Z"))
	public boolean modifyFoodRequirement(LocalPlayer instance, Operation<Boolean> original) {
		return Combatify.getState().equals(Combatify.CombatifyState.VANILLA) ? original.call(instance) : this.getAbilities().mayfly || instance.getFoodData().getFoodLevel() > (float) Combatify.CONFIG.getFoodImpl().execGetterFunc(6.0F, "getMinimumSprintLevel(player)", new PlayerWrapper<>(thisPlayer));
	}
    @Redirect(method = "hurtTo", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;invulnerableTime:I", opcode = Opcodes.PUTFIELD, ordinal = 0))
    private void syncInvulnerability(LocalPlayer player, int x) {
        player.invulnerableTime = x / 2;
    }

	@ModifyExpressionValue(method = "isSlowDueToUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
	private boolean isShieldCrouching(boolean original) {
		return original || isBlocking();
	}
	@ModifyReceiver(method = "isSlowDueToUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
	private ItemStack isShieldCrouching(ItemStack instance, DataComponentType<?> dataComponentType, Object o) {
		ItemStack blockingItem = MethodHandler.getBlockingItem(thisPlayer).stack();
		return blockingItem.isEmpty() ? instance : blockingItem;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean combatify$hasEnabledShieldOnCrouch() {
		return CombatifyClient.shieldCrouch.get();
	}

	@ModifyExpressionValue(method = "raycastHitResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;"))
	public HitResult addSwingThroughGrass(HitResult original) {
		HitResult redirectedResult = ClientMethodHandler.redirectResult(original);
		if (redirectedResult != null && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) original = redirectedResult;
		return original;
	}

	@ModifyReturnValue(method = "raycastHitResult", at = @At(value = "RETURN"))
	public HitResult addBedrockBridging(HitResult original, @Local(ordinal = 0, argsOnly = true) Entity entity, @Local(ordinal = 0, argsOnly = true) float partialTicks) {
		if ((original == null || original.getType() == HitResult.Type.MISS) && Combatify.CONFIG.bedrockBridging() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			Vec3 viewVector = entity.getViewVector(1.0F);
			if (entity.onGround() && viewVector.y < -0.7) {
				Vec3 adjustedPos = entity.getPosition(partialTicks).add(0.0, -0.1, 0.0);
				Vec3 adjustedVector = adjustedPos.add(viewVector.x, 0.0, viewVector.z);
				BlockHitResult result = entity.level().clip(new ClipContext(adjustedVector, adjustedPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
				result.combatify$setIsLedgeEdge();

				original = result;
			}
		}
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA) || Combatify.CONFIG.aimAssistTicks() == 0)
			((MinecraftExtensions)minecraft).combatify$setAimAssistHitResult(null);
		else if (original.getType() == HitResult.Type.ENTITY)
			((MinecraftExtensions)minecraft).combatify$setAimAssistHitResult(original);
		return original;
	}
}
