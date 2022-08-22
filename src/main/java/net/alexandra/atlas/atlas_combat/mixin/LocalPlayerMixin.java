package net.alexandra.atlas.atlas_combat.mixin;

import com.mojang.math.Vector3f;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.IEnchantmentHelper;
import net.alexandra.atlas.atlas_combat.extensions.IMinecraft;
import net.alexandra.atlas.atlas_combat.extensions.IOptions;
import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
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
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements PlayerExtensions {
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
					ItemStack itemStack = this.thisPlayer.getItemInHand(interactionHand);
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
					if (!itemStack.isEmpty() && itemStack.getItem() instanceof ShieldItem shieldItem) {
						minecraft.gameMode.releaseUsingItem(thisPlayer);
					}
				}
			}
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
		for(InteractionHand hand : InteractionHand.values()) {
			if (thisPlayer.isUsingItem() && thisPlayer.getItemInHand(hand).getItem() instanceof ShieldItem) {
				if(v < 1.0F) {
					v = 1.0F;
				}
				instance.tick(false, v);
			} else {
				instance.tick(b, v);
			}
		}
	}
	@Override
	public float getAttackAnim(float tickDelta) {
		LocalPlayer thisEntity = ((LocalPlayer)(Object)this);
		if(((IOptions)Minecraft.getInstance().options).rhythmicAttacks().get()) {
			boolean specialWeaponFunctions = AtlasCombat.helper.getBoolean(AtlasCombat.helper.generalJsonObject,"specialWeaponFunctions");
			boolean axeFunctions = AtlasCombat.helper.getBoolean(AtlasCombat.helper.generalJsonObject,"axeFunction");
			boolean pickaxeFunctions = AtlasCombat.helper.getBoolean(AtlasCombat.helper.generalJsonObject,"pickaxeFunction");
			boolean shovelFunctions = AtlasCombat.helper.getBoolean(AtlasCombat.helper.generalJsonObject,"shovelFunction");
			boolean hoeFunctions = AtlasCombat.helper.getBoolean(AtlasCombat.helper.generalJsonObject,"hoeFunction");
			boolean swordFunctions = AtlasCombat.helper.getBoolean(AtlasCombat.helper.generalJsonObject,"swordFunction");
			boolean tridentFunctions = AtlasCombat.helper.getBoolean(AtlasCombat.helper.generalJsonObject,"tridentFunction");
			if(specialWeaponFunctions) {
				if(minecraft.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof AxeItem && axeFunctions && swordFunctions) {
					float var2 = attackAnim - oAttackAnim;
					if (var2 < 0.0F) {
						++var2;
					}
					int sweepingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.SWEEPING_EDGE, thisEntity);
					double sweeping = sweepingLevel > 0 ? sweepingLevel * 1.5 : 1.0;

					float var3 = this.oAttackAnim + var2 * tickDelta;
					return var3 > 0.4F && thisEntity.getAttackStrengthScale(tickDelta) < 1.95F ? 0.4F + 0.6F * (float)Math.pow(((var3 - 0.4F) / 0.6F), 2.0 * sweeping) : (float) ((var3/2) * sweeping);
				}else if(minecraft.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof AxeItem && axeFunctions) {
					float var2 = attackAnim - oAttackAnim;
					if (var2 < 0.0F) {
						++var2;
					}

					float var3 = this.oAttackAnim + var2 * tickDelta;
					return var3 > 0.4F && thisEntity.getAttackStrengthScale(tickDelta) < 1.95F ? 0.4F + 0.6F * (float)Math.pow(((var3 - 0.4F) / 0.6F), 2.0) : var3/2;
				}else if(minecraft.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof AxeItem) {
					float var2 = attackAnim - oAttackAnim;
					if (var2 < 0.0F) {
						++var2;
					}

					float var3 = this.oAttackAnim + var2 * tickDelta;
					return var3 > 0.4F && thisEntity.getAttackStrengthScale(tickDelta) < 1.95F ? 0.4F + 0.6F * (float)Math.pow((double)((var3 - 0.4F) / 0.6F), 4.0) : var3;
				}else if(minecraft.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof PickaxeItem && pickaxeFunctions) {
					float var2 = attackAnim - oAttackAnim;
					if (var2 < 0.0F) {
						++var2;
					}

					float var3 = this.oAttackAnim + var2 * tickDelta;
					return var3 > 0.4F && thisEntity.getAttackStrengthScale(tickDelta) < 1.95F ? 0.4F + 0.6F * (float)Math.pow(((var3 - 0.4F) / 0.6F), 2.0) : var3/2;
				}else if(minecraft.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof PickaxeItem) {
					float var2 = attackAnim - oAttackAnim;
					if (var2 < 0.0F) {
						++var2;
					}

					float var3 = this.oAttackAnim + var2 * tickDelta;
					return var3 > 0.4F && thisEntity.getAttackStrengthScale(tickDelta) < 1.95F ? 0.4F + 0.6F * (float)Math.pow((double)((var3 - 0.4F) / 0.6F), 4.0) : var3;
				}else if(minecraft.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ShovelItem && shovelFunctions) {
					float var2 = attackAnim - oAttackAnim;
					if (var2 < 0.0F) {
						++var2;
					}

					float var3 = this.oAttackAnim + var2 * tickDelta;
					return var3 > 0.4F && thisEntity.getAttackStrengthScale(tickDelta) < 1.95F ? 0.4F + 0.6F * (float)Math.pow((double)((var3 - 0.4F) / 0.6F), 5.0) : var3*1.25F;
				}else if(minecraft.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ShovelItem) {
					float var2 = attackAnim - oAttackAnim;
					if (var2 < 0.0F) {
						++var2;
					}

					float var3 = this.oAttackAnim + var2 * tickDelta;
					return var3 > 0.4F && thisEntity.getAttackStrengthScale(tickDelta) < 1.95F ? 0.4F + 0.6F * (float)Math.pow((double)((var3 - 0.4F) / 0.6F), 4.0) : var3;
				}else if(minecraft.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof HoeItem && hoeFunctions) {
					float var2 = attackAnim - oAttackAnim;
					if (var2 < 0.0F) {
						++var2;
					}

					float var3 = this.oAttackAnim + var2 * tickDelta;
					return var3 > 0.4F && thisEntity.getAttackStrengthScale(tickDelta) < 1.95F ? 0.4F + 0.6F * (float)Math.pow((double)((var3 - 0.4F) / 0.6F), 1.5) : var3/(2F+(2F/3F));
				}else if(minecraft.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof HoeItem) {
					float var2 = attackAnim - oAttackAnim;
					if (var2 < 0.0F) {
						++var2;
					}

					float var3 = this.oAttackAnim + var2 * tickDelta;
					return var3 > 0.4F && thisEntity.getAttackStrengthScale(tickDelta) < 1.95F ? 0.4F + 0.6F * (float)Math.pow((double)((var3 - 0.4F) / 0.6F), 4.0) : var3;
				}else if(minecraft.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SwordItem && swordFunctions) {
					float var2 = attackAnim - oAttackAnim;
					if (var2 < 0.0F) {
						++var2;
					}
					int sweepingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.SWEEPING_EDGE, thisEntity);
					double sweeping = sweepingLevel > 0 ? sweepingLevel * 1.5 : 1.0;

					float var3 = this.oAttackAnim + var2 * tickDelta;
					return var3 > 0.4F && thisEntity.getAttackStrengthScale(tickDelta) < 1.95F ? 0.4F + 0.6F * (float)Math.pow((double)((var3 - 0.4F) / 0.6F), sweeping) : (float) ((var3 / 4F) * sweeping);
				}else if(minecraft.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SwordItem) {
					float var2 = attackAnim - oAttackAnim;
					if (var2 < 0.0F) {
						++var2;
					}

					float var3 = this.oAttackAnim + var2 * tickDelta;
					return var3 > 0.4F && thisEntity.getAttackStrengthScale(tickDelta) < 1.95F ? 0.4F + 0.6F * (float)Math.pow((double)((var3 - 0.4F) / 0.6F), 4.0) : var3;
				}else if(minecraft.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof TridentItem && tridentFunctions) {
					float var2 = attackAnim - oAttackAnim;
					if (var2 < 0.0F) {
						++var2;
					}
					int sweepingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.IMPALING, thisEntity);
					double sweeping = sweepingLevel > 0 ? sweepingLevel * 1.20 : 1.0;

					float var3 = this.oAttackAnim + var2 * tickDelta;
					return var3 > 0.4F && thisEntity.getAttackStrengthScale(tickDelta) < 1.95F ? 0.4F + 0.6F * (float)Math.pow((double)((var3 - 0.4F) / 0.6F), sweeping) : (float) ((var3 / 4F) * sweeping);
				}else {
					float var2 = attackAnim - oAttackAnim;
					if (var2 < 0.0F) {
						++var2;
					}

					float var3 = this.oAttackAnim + var2 * tickDelta;
					return var3 > 0.4F && thisEntity.getAttackStrengthScale(tickDelta) < 1.95F ? 0.4F + 0.6F * (float)Math.pow((double)((var3 - 0.4F) / 0.6F), 4.0) : var3;
				}
			}
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
