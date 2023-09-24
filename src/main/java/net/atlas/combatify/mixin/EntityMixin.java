package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public class EntityMixin {
	@ModifyReturnValue(method = "getBoundingBox", at = @At(value = "RETURN"))
	public AABB inflateBoxes(AABB original) {
		return MethodHandler.inflateBoundingBox(original);
	}
}
