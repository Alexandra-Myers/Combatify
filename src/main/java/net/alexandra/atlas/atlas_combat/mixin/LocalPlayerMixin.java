package net.alexandra.atlas.atlas_combat.mixin;

import com.mojang.math.Vector3f;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.config.ConfigHelper;
import net.alexandra.atlas.atlas_combat.extensions.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.rizecookey.cookeymod.CookeyMod;
import net.rizecookey.cookeymod.config.category.MiscCategory;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements PlayerExtensions, LivingEntityExtensions {
	@Shadow
	public abstract void startUsingItem(InteractionHand interactionHand);

	@Shadow
	private boolean startedUsingItem;
	@Unique
	@Final
	@Environment(EnvType.CLIENT)
	public Minecraft minecraft = Minecraft.getInstance();
	LocalPlayer thisPlayer = (LocalPlayer)(Object)this;
	@Environment(EnvType.CLIENT)
	@Inject(method = "tick", at = @At("HEAD"))
	public void injectSneakShield(CallbackInfo ci) {
		if(thisPlayer.isOnGround()) {
			if (this.hasEnabledShieldOnCrouch() && thisPlayer.isCrouching()) {
				for (InteractionHand interactionHand : InteractionHand.values()) {
					ItemStack itemStack = ((LivingEntityExtensions)this.thisPlayer).getBlockingItem();
					if (!itemStack.isEmpty() && itemStack.getItem() instanceof ShieldItem shieldItem && thisPlayer.isCrouching()) {
						if(!thisPlayer.getCooldowns().isOnCooldown(shieldItem)) {
							((IMinecraft) minecraft).startUseItem(interactionHand);
							if (lowShieldEnabled()) {
								minecraft.gameRenderer.itemInHandRenderer.itemUsed(interactionHand);
							}
						}
					}
				}
			} else if ((this.hasEnabledShieldOnCrouch() && thisPlayer.isUsingItem() && minecraft.options.keyShift.consumeClick() && !minecraft.options.keyShift.isDown()) && !minecraft.options.keyUse.isDown()) {
				for (InteractionHand interactionHand : InteractionHand.values()) {
					ItemStack itemStack = this.thisPlayer.getItemInHand(interactionHand);
					if (!itemStack.isEmpty() && (itemStack.getItem() instanceof ShieldItem)) {
						minecraft.gameMode.releaseUsingItem(thisPlayer);
						startedUsingItem = false;
					}
				}
			}
		}
	}
	@Override
	public boolean isAttackAvailable(float baseTime) {
		MiscCategory miscCategory = CookeyMod.getInstance().getConfig().getCategory(MiscCategory.class);
		if (!(getAttackStrengthScale(baseTime) < 1.0F)) {
			return true;
		} else {
			return getMissedAttackRecovery() && (float)getAttackStrengthStartValue() - ((float)this.attackStrengthTicker - baseTime) > 20.0F && !((IMiscCategory)miscCategory).getForce100PercentRecharge().get();
		}
	}
    public LocalPlayerMixin(Minecraft minecraft, ClientLevel clientLevel, ClientPacketListener clientPacketListener, StatsCounter statsCounter, ClientRecipeBook clientRecipeBook, boolean bl, boolean bl2) {
        super(clientLevel, clientPacketListener.getLocalGameProfile(), (ProfilePublicKey)minecraft.getProfileKeyPairManager().profilePublicKey().orElse(null));
    }
    @Redirect(method="hurtTo", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;invulnerableTime:I", opcode = Opcodes.PUTFIELD, ordinal=0))
    private void syncInvulnerability(LocalPlayer player, int x) {
        player.invulnerableTime = 5;
    }

	@Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/Input;tick(ZF)V"))
	private void isShieldCrouching(Input instance, boolean b, float v) {
		Item item = ((LivingEntityExtensions) thisPlayer).getBlockingItem().getItem();
		if(thisPlayer.getCooldowns().isOnCooldown(item)) {
			instance.tick(b, v);
		} else if(item instanceof ShieldItem && !thisPlayer.getCooldowns().isOnCooldown(item)) {
			if(v < 1.0F) {
				v = 1.0F;
			}
			instance.tick(false, v);
		} else {
			instance.tick(b, v);
		}
	}
	@ModifyConstant(method = "aiStep", constant = @Constant(floatValue = 0.2F))
	public float modifySwordBlockSpeed(float constant) {
		Item item = ((LivingEntityExtensions) thisPlayer).getBlockingItem().getItem();
		if(item instanceof SwordItem) {
			return 0.6F;
		}else {
			return constant;
		}
	}
	@Override
	public float getAttackAnim(float tickDelta) {
		LocalPlayer thisEntity = ((LocalPlayer)(Object)this);
		if(((IOptions)Minecraft.getInstance().options).rhythmicAttacks().get()) {
			float var2 = attackAnim - oAttackAnim;
			if (var2 < 0.0F) {
				++var2;
			}

			float var3 = this.oAttackAnim + var2 * tickDelta;
			return var3 > 0.4F && thisEntity.getAttackStrengthScale(tickDelta) < 1.95F ? 0.4F + 0.6F * (float)Math.pow((double)((var3 - 0.4F) / 0.6F), 4.0) : var3;
		}
		float f = this.attackAnim - this.oAttackAnim;
		if (f < 0.0F) {
			++f;
		}

		return this.oAttackAnim + f * tickDelta;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean hasEnabledShieldOnCrouch() {
		return ((IOptions)minecraft.options).shieldCrouch().get();
	}
	@Environment(EnvType.CLIENT)
	public boolean lowShieldEnabled() {
		return ((IOptions)minecraft.options).lowShield().get();
	}
}
