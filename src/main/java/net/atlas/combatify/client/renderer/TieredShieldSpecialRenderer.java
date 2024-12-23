package net.atlas.combatify.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import java.util.Objects;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.extensions.Tier;
import net.atlas.combatify.extensions.ToolMaterialWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class TieredShieldSpecialRenderer implements SpecialModelRenderer<DataComponentMap> {
	private final ShieldModel model;
	private final Tier tier;

	public TieredShieldSpecialRenderer(ShieldModel shieldModel, Tier tier) {
		this.model = shieldModel;
		this.tier = tier;
	}

	@Nullable
	public DataComponentMap extractArgument(ItemStack itemStack) {
		return itemStack.immutableComponents();
	}

	public void render(
		@Nullable DataComponentMap dataComponentMap,
		ItemDisplayContext itemDisplayContext,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		int j,
		boolean bl
	) {
		BannerPatternLayers bannerPatternLayers = dataComponentMap != null
			? dataComponentMap.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
			: BannerPatternLayers.EMPTY;
		DyeColor dyeColor = dataComponentMap != null ? dataComponentMap.get(DataComponents.BASE_COLOR) : null;
		boolean hasBanner = !bannerPatternLayers.layers().isEmpty() || dyeColor != null;
		poseStack.pushPose();
		poseStack.scale(1.0F, -1.0F, -1.0F);
		Material material = CombatifyClient.tieredShieldMaterials.get(tier).choose(hasBanner);
		VertexConsumer vertexConsumer = material.sprite()
			.wrap(ItemRenderer.getFoilBuffer(multiBufferSource, this.model.renderType(material.atlasLocation()), itemDisplayContext == ItemDisplayContext.GUI, bl));
		this.model.handle().render(poseStack, vertexConsumer, i, j);
		if (hasBanner) {
			BannerRenderer.renderPatterns(
				poseStack,
				multiBufferSource,
				i,
				j,
				this.model.plate(),
				material,
				false,
                    Objects.requireNonNullElse(dyeColor, DyeColor.WHITE),
				bannerPatternLayers,
				bl,
				false
			);
		} else {
			this.model.plate().render(poseStack, vertexConsumer, i, j);
		}

		poseStack.popPose();
	}

	@Environment(EnvType.CLIENT)
	public record Unbaked(Tier tier) implements SpecialModelRenderer.Unbaked {
		public static final MapCodec<TieredShieldSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(unbakedInstance ->
			unbakedInstance.group(ToolMaterialWrapper.TOOL_MATERIAL_CODEC.optionalFieldOf("tier", ToolMaterial.WOOD).forGetter(Unbaked::tier)).apply(unbakedInstance, Unbaked::new));

		@Override
		public MapCodec<TieredShieldSpecialRenderer.Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public SpecialModelRenderer<?> bake(EntityModelSet entityModelSet) {
			return new TieredShieldSpecialRenderer(new ShieldModel(entityModelSet.bakeLayer(CombatifyClient.tieredShieldModelLayers.get(tier))), tier);
		}
	}
}
