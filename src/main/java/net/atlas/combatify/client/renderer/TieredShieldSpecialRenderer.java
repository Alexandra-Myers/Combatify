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
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class TieredShieldSpecialRenderer implements SpecialModelRenderer<@NotNull DataComponentMap> {
	private final SpriteGetter sprites;
	private final ShieldModel model;
	private final ShieldMaterial shieldMaterial;

	public TieredShieldSpecialRenderer(SpriteGetter sprites, ShieldModel shieldModel, ShieldMaterial shieldMaterial) {
		this.sprites = sprites;
		this.model = shieldModel;
		this.shieldMaterial = shieldMaterial;
	}

	@Nullable
	public DataComponentMap extractArgument(ItemStack itemStack) {
		return itemStack.immutableComponents();
	}

	@Override
	public void submit(final @org.jspecify.annotations.Nullable DataComponentMap components, final @NonNull PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final int lightCoords, final int overlayCoords, final boolean hasFoil, final int outlineColor) {
		BannerPatternLayers patterns = components != null ? components.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY) : BannerPatternLayers.EMPTY;
		DyeColor baseColor = components != null ? components.get(DataComponents.BASE_COLOR) : null;
		boolean hasPatterns = !patterns.layers().isEmpty() || baseColor != null;
		SpriteId base = this.shieldMaterial.choose(hasPatterns);
		submitNodeCollector.submitModel(this.model, Unit.INSTANCE, poseStack, lightCoords, overlayCoords, -1, base, this.sprites, outlineColor, null);
		if (hasPatterns) {
			BannerRenderer.submitPatterns(this.sprites, poseStack, submitNodeCollector, lightCoords, overlayCoords, this.model, Unit.INSTANCE, false, Objects.requireNonNullElse(baseColor, DyeColor.WHITE), patterns, null);
		}

		if (hasFoil) {
			submitNodeCollector.submitModel(this.model, Unit.INSTANCE, poseStack, RenderTypes.entityGlint(), lightCoords, overlayCoords, -1, this.sprites.get(base), 0, (ModelFeatureRenderer.CrumblingOverlay)null);
		}
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
