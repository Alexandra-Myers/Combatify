package net.atlas.combatify.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.item.TieredShieldItem;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@SuppressWarnings("deprecation")
@Mixin(BlockEntityWithoutLevelRenderer.class)
public class RendererMixin {
	private ShieldModel modelWoodenShield;
	private ShieldModel modelIronShield;
	private ShieldModel modelGoldenShield;
	private ShieldModel modelDiamondShield;
	private ShieldModel modelNetheriteShield;
	private static final Material WOODEN_SHIELD_BASE = new Material(Sheets.SHIELD_SHEET, ResourceLocation.withDefaultNamespace("entity/shield_base"));
	private static final Material WOODEN_SHIELD_BASE_NO_PATTERN = new Material(Sheets.SHIELD_SHEET, ResourceLocation.withDefaultNamespace("entity/shield_base_nopattern"));
	private static final Material IRON_SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("combatify/iron_shield_base"));
	private static final Material IRON_SHIELD_BASE_NO_PATTERN = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("combatify/iron_shield_base_nopattern"));
	private static final Material GOLDEN_SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("combatify/golden_shield_base"));
	private static final Material GOLDEN_SHIELD_BASE_NO_PATTERN = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("combatify/golden_shield_base_nopattern"));
	private static final Material DIAMOND_SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("combatify/diamond_shield_base"));
	private static final Material DIAMOND_SHIELD_BASE_NO_PATTERN = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("combatify/diamond_shield_base_nopattern"));
	private static final Material NETHERITE_SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("combatify/netherite_shield_base"));
	private static final Material NETHERITE_SHIELD_BASE_NO_PATTERN = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("combatify/netherite_shield_base_nopattern"));

	@Final
	@Shadow
	private EntityModelSet entityModelSet;


	@Inject(method = "onResourceManagerReload", at = @At("HEAD"))
	private void setModelNetheriteShield(CallbackInfo ci){
		if (Combatify.CONFIG.tieredShields()) {
			this.modelWoodenShield = new ShieldModel(this.entityModelSet.bakeLayer(CombatifyClient.WOODEN_SHIELD_MODEL_LAYER));
			this.modelIronShield = new ShieldModel(this.entityModelSet.bakeLayer(CombatifyClient.IRON_SHIELD_MODEL_LAYER));
			this.modelGoldenShield = new ShieldModel(this.entityModelSet.bakeLayer(CombatifyClient.GOLDEN_SHIELD_MODEL_LAYER));
			this.modelDiamondShield = new ShieldModel(this.entityModelSet.bakeLayer(CombatifyClient.DIAMOND_SHIELD_MODEL_LAYER));
			this.modelNetheriteShield = new ShieldModel(this.entityModelSet.bakeLayer(CombatifyClient.NETHERITE_SHIELD_MODEL_LAYER));
		}
	}

	@Inject(method = "renderByItem", at = @At("HEAD"))
	private void mainRender(ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, CallbackInfo ci) {
		if (Combatify.CONFIG.tieredShields()) {
			if (stack.is(TieredShieldItem.WOODEN_SHIELD)) {
				BannerPatternLayers bannerPatternLayers = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
				DyeColor dyeColor = stack.get(DataComponents.BASE_COLOR);
				boolean bl = !bannerPatternLayers.layers().isEmpty() || dyeColor != null;
				poseStack.pushPose();
				poseStack.scale(1.0F, -1.0F, -1.0F);
				Material material = bl ? WOODEN_SHIELD_BASE : WOODEN_SHIELD_BASE_NO_PATTERN;
				VertexConsumer vertexConsumer = material.sprite().wrap(ItemRenderer.getFoilBufferDirect(multiBufferSource, this.modelWoodenShield.renderType(material.atlasLocation()), true, stack.hasFoil()));
				this.modelWoodenShield.handle().render(poseStack, vertexConsumer, i, j);
				if (bl)
					BannerRenderer.renderPatterns(poseStack, multiBufferSource, i, j, this.modelWoodenShield.plate(), material, false, Objects.requireNonNullElse(dyeColor, DyeColor.WHITE), bannerPatternLayers, stack.hasFoil());
				else
					this.modelWoodenShield.plate().render(poseStack, vertexConsumer, i, j);

				poseStack.popPose();
			}
			if (stack.is(TieredShieldItem.IRON_SHIELD)) {
				BannerPatternLayers bannerPatternLayers = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
				DyeColor dyeColor = stack.get(DataComponents.BASE_COLOR);
				boolean bl = !bannerPatternLayers.layers().isEmpty() || dyeColor != null;
				poseStack.pushPose();
				poseStack.scale(1.0F, -1.0F, -1.0F);
				Material material = bl ? IRON_SHIELD_BASE : IRON_SHIELD_BASE_NO_PATTERN;
				VertexConsumer vertexConsumer = material.sprite().wrap(ItemRenderer.getFoilBufferDirect(multiBufferSource, this.modelIronShield.renderType(material.atlasLocation()), true, stack.hasFoil()));
				this.modelIronShield.handle().render(poseStack, vertexConsumer, i, j);
				if (bl)
					BannerRenderer.renderPatterns(poseStack, multiBufferSource, i, j, this.modelIronShield.plate(), material, false, Objects.requireNonNullElse(dyeColor, DyeColor.WHITE), bannerPatternLayers, stack.hasFoil());
				else
					this.modelIronShield.plate().render(poseStack, vertexConsumer, i, j);

				poseStack.popPose();
			}
			if (stack.is(TieredShieldItem.GOLD_SHIELD)) {
				BannerPatternLayers bannerPatternLayers = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
				DyeColor dyeColor = stack.get(DataComponents.BASE_COLOR);
				boolean bl = !bannerPatternLayers.layers().isEmpty() || dyeColor != null;
				poseStack.pushPose();
				poseStack.scale(1.0F, -1.0F, -1.0F);
				Material material = bl ? GOLDEN_SHIELD_BASE : GOLDEN_SHIELD_BASE_NO_PATTERN;
				VertexConsumer vertexConsumer = material.sprite().wrap(ItemRenderer.getFoilBufferDirect(multiBufferSource, this.modelGoldenShield.renderType(material.atlasLocation()), true, stack.hasFoil()));
				this.modelGoldenShield.handle().render(poseStack, vertexConsumer, i, j);
				if (bl)
					BannerRenderer.renderPatterns(poseStack, multiBufferSource, i, j, this.modelGoldenShield.plate(), material, false, Objects.requireNonNullElse(dyeColor, DyeColor.WHITE), bannerPatternLayers, stack.hasFoil());
				else
					this.modelGoldenShield.plate().render(poseStack, vertexConsumer, i, j);

				poseStack.popPose();
			}
			if (stack.is(TieredShieldItem.DIAMOND_SHIELD)) {
				BannerPatternLayers bannerPatternLayers = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
				DyeColor dyeColor = stack.get(DataComponents.BASE_COLOR);
				boolean bl = !bannerPatternLayers.layers().isEmpty() || dyeColor != null;
				poseStack.pushPose();
				poseStack.scale(1.0F, -1.0F, -1.0F);
				Material material = bl ? DIAMOND_SHIELD_BASE : DIAMOND_SHIELD_BASE_NO_PATTERN;
				VertexConsumer vertexConsumer = material.sprite().wrap(ItemRenderer.getFoilBufferDirect(multiBufferSource, this.modelDiamondShield.renderType(material.atlasLocation()), true, stack.hasFoil()));
				this.modelDiamondShield.handle().render(poseStack, vertexConsumer, i, j);
				if (bl)
					BannerRenderer.renderPatterns(poseStack, multiBufferSource, i, j, this.modelDiamondShield.plate(), material, false, Objects.requireNonNullElse(dyeColor, DyeColor.WHITE), bannerPatternLayers, stack.hasFoil());
				else
					this.modelDiamondShield.plate().render(poseStack, vertexConsumer, i, j);

				poseStack.popPose();
			}
			if (stack.is(TieredShieldItem.NETHERITE_SHIELD)) {
				BannerPatternLayers bannerPatternLayers = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
				DyeColor dyeColor = stack.get(DataComponents.BASE_COLOR);
				boolean bl = !bannerPatternLayers.layers().isEmpty() || dyeColor != null;
				poseStack.pushPose();
				poseStack.scale(1.0F, -1.0F, -1.0F);
				Material material = bl ? NETHERITE_SHIELD_BASE : NETHERITE_SHIELD_BASE_NO_PATTERN;
				VertexConsumer vertexConsumer = material.sprite().wrap(ItemRenderer.getFoilBufferDirect(multiBufferSource, this.modelNetheriteShield.renderType(material.atlasLocation()), true, stack.hasFoil()));
				this.modelNetheriteShield.handle().render(poseStack, vertexConsumer, i, j);
				if (bl)
					BannerRenderer.renderPatterns(poseStack, multiBufferSource, i, j, this.modelNetheriteShield.plate(), material, false, Objects.requireNonNullElse(dyeColor, DyeColor.WHITE), bannerPatternLayers, stack.hasFoil());
				else
					this.modelNetheriteShield.plate().render(poseStack, vertexConsumer, i, j);

				poseStack.popPose();
			}
		}
	}


}
