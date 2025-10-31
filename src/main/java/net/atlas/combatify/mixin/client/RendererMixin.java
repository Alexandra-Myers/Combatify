package net.atlas.combatify.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.client.ShieldMaterial;
import net.atlas.combatify.item.TieredShieldItem;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(BlockEntityWithoutLevelRenderer.class)
public class RendererMixin {
	@Shadow
	private ShieldModel shieldModel;

	@Inject(method = "renderByItem", at = @At("HEAD"))
	private void mainRender(ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, CallbackInfo ci) {
		if (Combatify.CONFIG.tieredShields()) {
			if (stack.is(TieredShieldItem.IRON_SHIELD))
				renderExtra(poseStack, ShieldMaterial.IRON_SHIELD, stack.getComponents(), itemDisplayContext, stack, multiBufferSource, i, j);
			if (stack.is(TieredShieldItem.GOLD_SHIELD))
				renderExtra(poseStack, ShieldMaterial.GOLDEN_SHIELD, stack.getComponents(), itemDisplayContext, stack, multiBufferSource, i, j);
			if (stack.is(TieredShieldItem.DIAMOND_SHIELD))
				renderExtra(poseStack, ShieldMaterial.DIAMOND_SHIELD, stack.getComponents(), itemDisplayContext, stack, multiBufferSource, i, j);
			if (stack.is(TieredShieldItem.NETHERITE_SHIELD))
				renderExtra(poseStack, ShieldMaterial.NETHERITE_SHIELD, stack.getComponents(), itemDisplayContext, stack, multiBufferSource, i, j);
		}
	}

	@Unique
	private void renderExtra(PoseStack poseStack, ShieldMaterial material, DataComponentMap dataComponentMap,
									ItemDisplayContext itemDisplayContext, ItemStack itemStack, MultiBufferSource multiBufferSource, int i, int j) {
		BannerPatternLayers bannerPatternLayers = dataComponentMap != null
			? dataComponentMap.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
			: BannerPatternLayers.EMPTY;
		DyeColor dyeColor = dataComponentMap != null ? dataComponentMap.get(DataComponents.BASE_COLOR) : null;
		boolean hasBanner = !bannerPatternLayers.layers().isEmpty() || dyeColor != null;
		poseStack.pushPose();
		poseStack.scale(1.0F, -1.0F, -1.0F);
		Material result = material.choose(hasBanner);
		VertexConsumer vertexConsumer = result.sprite()
			.wrap(ItemRenderer.getFoilBuffer(multiBufferSource, shieldModel.renderType(result.atlasLocation()), itemDisplayContext == ItemDisplayContext.GUI, itemStack.hasFoil()));
		shieldModel.handle().render(poseStack, vertexConsumer, i, j);
		if (hasBanner)
			BannerRenderer.renderPatterns(poseStack, multiBufferSource, i, j, shieldModel.plate(), result, false, Objects.requireNonNullElse(dyeColor, DyeColor.WHITE), bannerPatternLayers, itemStack.hasFoil());
		else
			shieldModel.plate().render(poseStack, vertexConsumer, i, j);

		poseStack.popPose();
	}


}
