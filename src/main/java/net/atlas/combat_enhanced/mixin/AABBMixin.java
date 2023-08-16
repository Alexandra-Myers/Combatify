package net.atlas.combat_enhanced.mixin;

import net.atlas.combat_enhanced.extensions.AABBExtensions;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AABB.class)
public class AABBMixin implements AABBExtensions {

	@Shadow
	@Final
	public double minX;

	@Shadow
	@Final
	public double minY;

	@Shadow
	@Final
	public double minZ;

	@Shadow
	@Final
	public double maxX;

	@Shadow
	@Final
	public double maxZ;

	@Shadow
	@Final
	public double maxY;

	@Override
	public Vec3 getNearestPointTo(Vec3 vec3) {
		double x = Mth.clamp(vec3.x, this.minX, this.maxX);
		double y = Mth.clamp(vec3.y, this.minY, this.maxY);
		double z = Mth.clamp(vec3.z, this.minZ, this.maxZ);

		return new Vec3(x, y, z);
	}
}
