package net.atlas.combatify.mixin.cookey;

import com.mojang.blaze3d.vertex.PoseStack;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TieredItem;
import net.atlas.combatify.config.cookey.option.BooleanOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {
    private ItemInHandLayerMixin(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

	@Unique
	private BooleanOption enableToolBlocking;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void injectOptions(RenderLayerParent<T, M> renderLayerParent, ItemInHandRenderer itemInHandRenderer, CallbackInfo ci) {
		enableToolBlocking = CombatifyClient.getInstance().getConfig().animations().enableToolBlocking();
	}

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    public void hideShieldWithToolBlocking(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext displayContext, HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (this.enableToolBlocking.get()) {
            InteractionHand otherHand = humanoidArm == livingEntity.getMainArm() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack otherHandStack = livingEntity.getItemInHand(otherHand);
            if (itemStack.getItem() instanceof ShieldItem && otherHandStack.getItem() instanceof TieredItem && (!MethodHandler.getBlockingItem(livingEntity).stack().isEmpty() && MethodHandler.getBlockingItem(livingEntity).getItem() instanceof ShieldItem)) {
                ci.cancel();
            }
        }
    }
}
