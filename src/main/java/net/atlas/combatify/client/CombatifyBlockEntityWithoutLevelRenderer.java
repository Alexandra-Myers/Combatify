package net.atlas.combatify.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.item.TieredShieldItem;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("removal")
public class CombatifyBlockEntityWithoutLevelRenderer extends BlockEntityWithoutLevelRenderer {
	private ShieldModel shieldModel;
	private static final Material IRON_SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/iron_shield_base"));
	private static final Material IRON_SHIELD_BASE_NO_PATTERN = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/iron_shield_base_nopattern"));
	private static final Material GOLDEN_SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/golden_shield_base"));
	private static final Material GOLDEN_SHIELD_BASE_NO_PATTERN = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/golden_shield_base_nopattern"));
	private static final Material DIAMOND_SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/diamond_shield_base"));
	private static final Material DIAMOND_SHIELD_BASE_NO_PATTERN = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/diamond_shield_base_nopattern"));
	private static final Material NETHERITE_SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/netherite_shield_base"));
	private static final Material NETHERITE_SHIELD_BASE_NO_PATTERN = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("combatify/netherite_shield_base_nopattern"));
    private final EntityModelSet entityModelSet;
    public CombatifyBlockEntityWithoutLevelRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
        super(p_172550_, p_172551_);
        this.entityModelSet = p_172551_;
		this.shieldModel = new ShieldModel(this.entityModelSet.bakeLayer(ModelLayers.SHIELD));
    }

	@Override
	public void onResourceManagerReload(ResourceManager arg) {
		super.onResourceManagerReload(arg);
		this.shieldModel = new ShieldModel(this.entityModelSet.bakeLayer(ModelLayers.SHIELD));
	}

	@Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		if (Combatify.CONFIG.tieredShields.get()) {
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
		super.renderByItem(stack, displayContext, poseStack, multiBufferSource, i, j);
	}

	private static @NotNull Material getMaterial(ItemStack stack, boolean hasBanner) {
		Material spriteIdentifier = hasBanner ? ModelBakery.SHIELD_BASE : ModelBakery.NO_PATTERN_SHIELD;
		if (stack.is(TieredShieldItem.IRON_SHIELD.get())) spriteIdentifier = hasBanner ? IRON_SHIELD_BASE : IRON_SHIELD_BASE_NO_PATTERN;
		if (stack.is(TieredShieldItem.GOLD_SHIELD.get())) spriteIdentifier = hasBanner ? GOLDEN_SHIELD_BASE : GOLDEN_SHIELD_BASE_NO_PATTERN;
		if (stack.is(TieredShieldItem.DIAMOND_SHIELD.get())) spriteIdentifier = hasBanner ? DIAMOND_SHIELD_BASE : DIAMOND_SHIELD_BASE_NO_PATTERN;
		if (stack.is(TieredShieldItem.NETHERITE_SHIELD.get())) spriteIdentifier = hasBanner ? NETHERITE_SHIELD_BASE : NETHERITE_SHIELD_BASE_NO_PATTERN;
		return spriteIdentifier;
	}
}
