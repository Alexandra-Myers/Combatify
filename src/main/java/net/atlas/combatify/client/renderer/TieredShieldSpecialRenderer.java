package net.atlas.combatify.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.Set;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.CombatifyClient;
import net.atlas.defaulted.component.ToolMaterialWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class TieredShieldSpecialRenderer implements SpecialModelRenderer<DataComponentMap> {
	private final MaterialSet materials;
	private final ShieldModel model;
	private final ToolMaterial tier;

	public TieredShieldSpecialRenderer(MaterialSet materialSet, ShieldModel shieldModel, ToolMaterial tier) {
		this.materials = materialSet;
		this.model = shieldModel;
		this.tier = tier;
	}

	@Nullable
	public DataComponentMap extractArgument(ItemStack itemStack) {
		return itemStack.immutableComponents();
	}

	@Override
	public void submit(@Nullable DataComponentMap dataComponentMap, ItemDisplayContext itemDisplayContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, boolean bl, int k) {
		BannerPatternLayers bannerPatternLayers = dataComponentMap != null ? dataComponentMap.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY) : BannerPatternLayers.EMPTY;
		DyeColor dyeColor = dataComponentMap != null ? dataComponentMap.get(DataComponents.BASE_COLOR) : null;
		boolean hasBanner = !bannerPatternLayers.layers().isEmpty() || dyeColor != null;
		poseStack.pushPose();
		poseStack.scale(1.0F, -1.0F, -1.0F);
		Material material = CombatifyClient.tieredShieldMaterials.get(tier).choose(hasBanner);
		submitNodeCollector.submitModelPart(this.model.handle(), poseStack, this.model.renderType(material.atlasLocation()), i, j, this.materials.get(material));
		if (hasBanner) {
			BannerRenderer.submitPatterns(this.materials, poseStack, submitNodeCollector, i, j, this.model, Unit.INSTANCE, material, false, Objects.requireNonNullElse(dyeColor, DyeColor.WHITE), bannerPatternLayers, bl, null, k);
		} else {
			submitNodeCollector.submitModelPart(this.model.plate(), poseStack, this.model.renderType(material.atlasLocation()), i, j, this.materials.get(material), false, bl, -1, null, k);
		}

		poseStack.popPose();
	}

	@Override
	public void getExtents(Set<Vector3f> set) {
		PoseStack poseStack = new PoseStack();
		poseStack.scale(1.0F, -1.0F, -1.0F);
		this.model.root().getExtentsForGui(poseStack, set);
	}

	@Environment(EnvType.CLIENT)
	public record Unbaked(ToolMaterial tier) implements SpecialModelRenderer.Unbaked {
		public static final MapCodec<TieredShieldSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(unbakedInstance ->
			unbakedInstance.group(ToolMaterialWrapper.TOOL_MATERIAL_CODEC.optionalFieldOf("tier", ToolMaterial.WOOD).forGetter(Unbaked::tier)).apply(unbakedInstance, Unbaked::new));

		@Override
		public @Nullable SpecialModelRenderer<?> bake(BakingContext bakingContext) {
			return new TieredShieldSpecialRenderer(bakingContext.materials(), new ShieldModel(bakingContext.entityModelSet().bakeLayer(CombatifyClient.tieredShieldModelLayers.get(tier))), tier);
		}

		@Override
		public MapCodec<TieredShieldSpecialRenderer.Unbaked> type() {
			return MAP_CODEC;
		}
	}
}
