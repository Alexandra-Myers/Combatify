package net.atlas.combatify.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
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
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@SuppressWarnings("deprecation")
@Mixin(BlockEntityWithoutLevelRenderer.class)
public class RendererMixin {
	private ShieldModel modelWoodenShield;
	private ShieldModel modelIronShield;
	private ShieldModel modelGoldenShield;
	private ShieldModel modelDiamondShield;
	private ShieldModel modelNetheriteShield;
	private static final Material WOODEN_SHIELD_BASE = new Material(Sheets.SHIELD_SHEET, new ResourceLocation("entity/shield_base"));
	private static final Material WOODEN_SHIELD_BASE_NO_PATTERN = new Material(Sheets.SHIELD_SHEET, new ResourceLocation("entity/shield_base_nopattern"));
	private static final Material IRON_SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/iron_shield_base"));
	private static final Material IRON_SHIELD_BASE_NO_PATTERN = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/iron_shield_base_nopattern"));
	private static final Material GOLDEN_SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/golden_shield_base"));
	private static final Material GOLDEN_SHIELD_BASE_NO_PATTERN = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/golden_shield_base_nopattern"));
	private static final Material DIAMOND_SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/diamond_shield_base"));
	private static final Material DIAMOND_SHIELD_BASE_NO_PATTERN = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/diamond_shield_base_nopattern"));
	private static final Material NETHERITE_SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/netherite_shield_base"));
	private static final Material NETHERITE_SHIELD_BASE_NO_PATTERN = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/netherite_shield_base_nopattern"));

	@Final
	@Shadow
	private EntityModelSet entityModelSet;


	@Inject(method = "onResourceManagerReload", at = @At("HEAD"))
	private void setModelNetheriteShield(CallbackInfo ci){
		if (Combatify.CONFIG.tieredShields.get()) {
			this.modelWoodenShield = new ShieldModel(this.entityModelSet.bakeLayer(CombatifyClient.WOODEN_SHIELD_MODEL_LAYER));
			this.modelIronShield = new ShieldModel(this.entityModelSet.bakeLayer(CombatifyClient.IRON_SHIELD_MODEL_LAYER));
			this.modelGoldenShield = new ShieldModel(this.entityModelSet.bakeLayer(CombatifyClient.GOLDEN_SHIELD_MODEL_LAYER));
			this.modelDiamondShield = new ShieldModel(this.entityModelSet.bakeLayer(CombatifyClient.DIAMOND_SHIELD_MODEL_LAYER));
			this.modelNetheriteShield = new ShieldModel(this.entityModelSet.bakeLayer(CombatifyClient.NETHERITE_SHIELD_MODEL_LAYER));
		}
	}

	@Inject(method = "renderByItem", at = @At("HEAD"))
	private void mainRender(ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, CallbackInfo ci) {
		if (Combatify.CONFIG.tieredShields.get()) {
			if (stack.is(TieredShieldItem.WOODEN_SHIELD.get())) {
				boolean bl = stack.getTagElement("BlockEntityTag") != null;
				poseStack.pushPose();
				poseStack.scale(1.0F, -1.0F, -1.0F);
				Material spriteIdentifier = bl ? WOODEN_SHIELD_BASE : WOODEN_SHIELD_BASE_NO_PATTERN;
				VertexConsumer vertexConsumer = spriteIdentifier.sprite().wrap(ItemRenderer.getFoilBufferDirect(multiBufferSource, modelWoodenShield.renderType(spriteIdentifier.atlasLocation()), true, stack.hasFoil()));
				modelWoodenShield.handle().render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
				if (bl) {
					List<Pair<Holder<BannerPattern>, DyeColor>> list = BannerBlockEntity.createPatterns(ShieldItem.getColor(stack), BannerBlockEntity.getItemPatterns(stack));
					BannerRenderer.renderPatterns(poseStack, multiBufferSource, i, j, modelWoodenShield.plate(), spriteIdentifier, false, list, stack.hasFoil());
				} else {
					modelWoodenShield.plate().render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
				}
				poseStack.popPose();
			}
			if (stack.is(TieredShieldItem.IRON_SHIELD.get())) {
				boolean bl = stack.getTagElement("BlockEntityTag") != null;
				poseStack.pushPose();
				poseStack.scale(1.0F, -1.0F, -1.0F);
				Material spriteIdentifier = bl ? IRON_SHIELD_BASE : IRON_SHIELD_BASE_NO_PATTERN;
				VertexConsumer vertexConsumer = spriteIdentifier.sprite().wrap(ItemRenderer.getFoilBufferDirect(multiBufferSource, modelIronShield.renderType(spriteIdentifier.atlasLocation()), true, stack.hasFoil()));
				modelIronShield.handle().render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
				if (bl) {
					List<Pair<Holder<BannerPattern>, DyeColor>> list = BannerBlockEntity.createPatterns(ShieldItem.getColor(stack), BannerBlockEntity.getItemPatterns(stack));
					BannerRenderer.renderPatterns(poseStack, multiBufferSource, i, j, modelIronShield.plate(), spriteIdentifier, false, list, stack.hasFoil());
				} else {
					modelIronShield.plate().render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
				}
				poseStack.popPose();
			}
			if (stack.is(TieredShieldItem.GOLD_SHIELD.get())) {
				boolean bl = stack.getTagElement("BlockEntityTag") != null;
				poseStack.pushPose();
				poseStack.scale(1.0F, -1.0F, -1.0F);
				Material spriteIdentifier = bl ? GOLDEN_SHIELD_BASE : GOLDEN_SHIELD_BASE_NO_PATTERN;
				VertexConsumer vertexConsumer = spriteIdentifier.sprite().wrap(ItemRenderer.getFoilBufferDirect(multiBufferSource, modelGoldenShield.renderType(spriteIdentifier.atlasLocation()), true, stack.hasFoil()));
				modelGoldenShield.handle().render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
				if (bl) {
					List<Pair<Holder<BannerPattern>, DyeColor>> list = BannerBlockEntity.createPatterns(ShieldItem.getColor(stack), BannerBlockEntity.getItemPatterns(stack));
					BannerRenderer.renderPatterns(poseStack, multiBufferSource, i, j, modelGoldenShield.plate(), spriteIdentifier, false, list, stack.hasFoil());
				} else {
					modelGoldenShield.plate().render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
				}
				poseStack.popPose();
			}
			if (stack.is(TieredShieldItem.DIAMOND_SHIELD.get())) {
				boolean bl = stack.getTagElement("BlockEntityTag") != null;
				poseStack.pushPose();
				poseStack.scale(1.0F, -1.0F, -1.0F);
				Material spriteIdentifier = bl ? DIAMOND_SHIELD_BASE : DIAMOND_SHIELD_BASE_NO_PATTERN;
				VertexConsumer vertexConsumer = spriteIdentifier.sprite().wrap(ItemRenderer.getFoilBufferDirect(multiBufferSource, modelDiamondShield.renderType(spriteIdentifier.atlasLocation()), true, stack.hasFoil()));
				modelDiamondShield.handle().render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
				if (bl) {
					List<Pair<Holder<BannerPattern>, DyeColor>> list = BannerBlockEntity.createPatterns(ShieldItem.getColor(stack), BannerBlockEntity.getItemPatterns(stack));
					BannerRenderer.renderPatterns(poseStack, multiBufferSource, i, j, modelDiamondShield.plate(), spriteIdentifier, false, list, stack.hasFoil());
				} else {
					modelDiamondShield.plate().render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
				}
				poseStack.popPose();
			}
			if (stack.is(TieredShieldItem.NETHERITE_SHIELD.get())) {
				boolean bl = stack.getTagElement("BlockEntityTag") != null;
				poseStack.pushPose();
				poseStack.scale(1.0F, -1.0F, -1.0F);
				Material spriteIdentifier = bl ? NETHERITE_SHIELD_BASE : NETHERITE_SHIELD_BASE_NO_PATTERN;
				VertexConsumer vertexConsumer = spriteIdentifier.sprite().wrap(ItemRenderer.getFoilBufferDirect(multiBufferSource, modelNetheriteShield.renderType(spriteIdentifier.atlasLocation()), true, stack.hasFoil()));
				modelNetheriteShield.handle().render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
				if (bl) {
					List<Pair<Holder<BannerPattern>, DyeColor>> list = BannerBlockEntity.createPatterns(ShieldItem.getColor(stack), BannerBlockEntity.getItemPatterns(stack));
					BannerRenderer.renderPatterns(poseStack, multiBufferSource, i, j, modelNetheriteShield.plate(), spriteIdentifier, false, list, stack.hasFoil());
				} else {
					modelNetheriteShield.plate().render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
				}
				poseStack.popPose();
			}
		}
	}


}
