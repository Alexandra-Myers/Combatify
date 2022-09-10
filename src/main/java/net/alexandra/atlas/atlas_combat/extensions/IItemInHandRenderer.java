package net.alexandra.atlas.atlas_combat.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.HumanoidArm;

public interface IItemInHandRenderer {
    void applyItemBlockTransform2(PoseStack poseStack, HumanoidArm humanoidArm);

	void setExtras(boolean extras);
}
