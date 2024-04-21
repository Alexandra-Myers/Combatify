package net.atlas.combatify.mixin.cookey;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.atlas.combatify.CombatifyClient;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.atlas.combatify.config.cookey.option.BooleanOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player {

    BooleanOption disableEffectBasedFovChange = CombatifyClient.getInstance().getConfig().hudRendering().disableEffectBasedFovChange();

    private AbstractClientPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @WrapOperation(method = "getFieldOfViewModifier", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getAttributeValue(Lnet/minecraft/core/Holder;)D"))
    public double disableEffectBasedFov(AbstractClientPlayer player, Holder<Attribute> attribute, Operation<Double> original) {
        if (this.disableEffectBasedFovChange.get()) {
            double mov = player.getAttributeBaseValue(attribute);
            if (this.isSprinting() && LivingEntityAccessor.SPEED_MODIFIER_SPRINTING() != null) {
                mov *= 1 + LivingEntityAccessor.SPEED_MODIFIER_SPRINTING().amount();
            }
            return mov;
        } else {
            return original.call(player, attribute);
        }
    }
}
