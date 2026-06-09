package net.atlas.combatify.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.function.Consumer;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.client.ShieldMaterial;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

@Environment(EnvType.CLIENT)
public class TieredShieldSpecialRenderer implements SpecialModelRenderer<@NotNull DataComponentMap> {
	private final SpriteGetter materials;
	private final ShieldModel model;
	private final ShieldMaterial shieldMaterial;

	public TieredShieldSpecialRenderer(SpriteGetter materialSet, ShieldModel shieldModel, ShieldMaterial shieldMaterial) {
		this.materials = materialSet;
		this.model = shieldModel;
		this.shieldMaterial = shieldMaterial;
	}

	@Nullable
	public DataComponentMap extractArgument(ItemStack itemStack) {
		return itemStack.immutableComponents();
	}

	@Override
	public void submit(@org.jspecify.annotations.Nullable @NotNull DataComponentMap dataComponentMap, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
		BannerPatternLayers bannerPatternLayers = dataComponentMap != null ? dataComponentMap.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY) : BannerPatternLayers.EMPTY;
		DyeColor dyeColor = dataComponentMap != null ? dataComponentMap.get(DataComponents.BASE_COLOR) : null;
		boolean hasBanner = !bannerPatternLayers.layers().isEmpty() || dyeColor != null;
		poseStack.pushPose();
		poseStack.scale(1.0F, -1.0F, -1.0F);
		Material material = shieldMaterial.choose(hasBanner);
		submitNodeCollector.submitModelPart(this.model.handle(), poseStack, this.model.renderType(material.atlasLocation()), lightCoords, overlayCoords, this.materials.get(material));
		if (hasBanner) {
			BannerRenderer.submitPatterns(
				this.materials,
				poseStack,
				submitNodeCollector,
				lightCoords,
				overlayCoords,
				this.model,
				Unit.INSTANCE,
				material,
				false,
				Objects.requireNonNullElse(dyeColor, DyeColor.WHITE),
				bannerPatternLayers,
				hasFoil,
				outlineColor
			);
		} else {
			submitNodeCollector.submitModelPart(this.model.plate(), poseStack, this.model.renderType(material.atlasLocation()), lightCoords, overlayCoords, this.materials.get(material), false, hasFoil, -1, null, outlineColor);
		}

		poseStack.popPose();
	}

	@Override
	public void getExtents(@NotNull Consumer<Vector3fc> set) {
		PoseStack poseStack = new PoseStack();
		poseStack.scale(1.0F, -1.0F, -1.0F);
		this.model.root().getExtentsForGui(poseStack, set);
	}

	@Environment(EnvType.CLIENT)
	public record Unbaked(ShieldMaterial material) implements SpecialModelRenderer.Unbaked {
		public static final MapCodec<TieredShieldSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(unbakedInstance ->
			unbakedInstance.group(ShieldMaterial.CODEC.forGetter(Unbaked::material)).apply(unbakedInstance, Unbaked::new));

		@Override
		public @NotNull SpecialModelRenderer<?> bake(BakingContext bakingContext) {
			return new TieredShieldSpecialRenderer(bakingContext.sprites(), new ShieldModel(bakingContext.entityModelSet().bakeLayer(ModelLayers.SHIELD)), material);
		}

		@Override
		public @NotNull MapCodec<TieredShieldSpecialRenderer.Unbaked> type() {
			return MAP_CODEC;
		}
	}
}
