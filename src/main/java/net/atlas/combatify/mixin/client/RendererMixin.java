package net.atlas.combatify.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.item.TieredShieldItem;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Holder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static net.atlas.combatify.CombatifyClient.*;

@Mixin(BlockEntityWithoutLevelRenderer.class)
public class RendererMixin {
	@Shadow
	private ShieldModel shieldModel;

	@Inject(method = "renderByItem", at = @At("HEAD"))
	private void mainRender(ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, CallbackInfo ci) {
		if (Combatify.CONFIG.tieredShields()) {
			if (stack.getItem() instanceof TieredShieldItem) {
				boolean hasBannerPatterns = stack.getTagElement("BlockEntityTag") != null;
				poseStack.pushPose();
				poseStack.scale(1.0F, -1.0F, -1.0F);
				Material spriteIdentifier = getMaterial(stack, hasBannerPatterns);

				VertexConsumer vertexConsumer = spriteIdentifier.sprite().wrap(ItemRenderer.getFoilBufferDirect(multiBufferSource, shieldModel.renderType(spriteIdentifier.atlasLocation()), true, stack.hasFoil()));
				shieldModel.handle().render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
				if (hasBannerPatterns) {
					List<Pair<Holder<BannerPattern>, DyeColor>> list = BannerBlockEntity.createPatterns(ShieldItem.getColor(stack), BannerBlockEntity.getItemPatterns(stack));
					BannerRenderer.renderPatterns(poseStack, multiBufferSource, i, j, shieldModel.plate(), spriteIdentifier, false, list, stack.hasFoil());
				} else {
					shieldModel.plate().render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
				}
				poseStack.popPose();
			}
		}
	}

	@Unique
	private static @NotNull Material getMaterial(ItemStack stack, boolean hasBanner) {
		Material spriteIdentifier = hasBanner ? ModelBakery.SHIELD_BASE : ModelBakery.NO_PATTERN_SHIELD;
		if (stack.is(TieredShieldItem.IRON_SHIELD)) spriteIdentifier = hasBanner ? IRON_SHIELD_BASE : IRON_SHIELD_BASE_NO_PATTERN;
		if (stack.is(TieredShieldItem.GOLD_SHIELD)) spriteIdentifier = hasBanner ? GOLDEN_SHIELD_BASE : GOLDEN_SHIELD_BASE_NO_PATTERN;
		if (stack.is(TieredShieldItem.DIAMOND_SHIELD)) spriteIdentifier = hasBanner ? DIAMOND_SHIELD_BASE : DIAMOND_SHIELD_BASE_NO_PATTERN;
		if (stack.is(TieredShieldItem.NETHERITE_SHIELD)) spriteIdentifier = hasBanner ? NETHERITE_SHIELD_BASE : NETHERITE_SHIELD_BASE_NO_PATTERN;
		return spriteIdentifier;
	}
}
