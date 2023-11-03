package net.atlas.combatify.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.HumanoidArm;

public interface IItemInHandRenderer {
    void combatify$applyItemBlockTransform2(PoseStack poseStack, HumanoidArm humanoidArm);
}
