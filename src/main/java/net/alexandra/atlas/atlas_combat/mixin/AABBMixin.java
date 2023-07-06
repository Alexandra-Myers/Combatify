package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.AABBExtensions;
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
	public Vec3 getNearestPointTo(Vec3 paramVec3) {
		if (paramVec3.x < this.minX && paramVec3.y < this.minY && paramVec3.z < this.minZ)
			return new Vec3(this.minX, this.minY, this.minZ);
		if (paramVec3.x < this.minX && paramVec3.y < this.minY && paramVec3.z > this.maxZ)
			return new Vec3(this.minX, this.minY, this.maxZ);
		if (paramVec3.x < this.minX && paramVec3.y > this.maxY && paramVec3.z < this.minZ)
			return new Vec3(this.minX, this.maxY, this.minZ);
		if (paramVec3.x < this.minX && paramVec3.y > this.maxY && paramVec3.z > this.maxZ)
			return new Vec3(this.minX, this.maxY, this.maxZ);
		if (paramVec3.x > this.maxX && paramVec3.y < this.minY && paramVec3.z < this.minZ)
			return new Vec3(this.maxX, this.minY, this.minZ);
		if (paramVec3.x > this.maxX && paramVec3.y < this.minY && paramVec3.z > this.maxZ)
			return new Vec3(this.maxX, this.minY, this.maxZ);
		if (paramVec3.x > this.maxX && paramVec3.y > this.maxY && paramVec3.z < this.minZ)
			return new Vec3(this.maxX, this.maxY, this.minZ);
		if (paramVec3.x > this.maxX && paramVec3.y > this.maxY && paramVec3.z > this.maxZ)
			return new Vec3(this.maxX, this.maxY, this.maxZ);

		if (paramVec3.x < this.minX)
			return new Vec3(this.minX, paramVec3.y, paramVec3.z);
		if (paramVec3.y < this.minY)
			return new Vec3(paramVec3.x, this.minY, paramVec3.z);
		if (paramVec3.z < this.minZ)
			return new Vec3(paramVec3.x, paramVec3.y, this.minZ);
		if (paramVec3.x > this.maxX)
			return new Vec3(this.maxX, paramVec3.y, paramVec3.z);
		if (paramVec3.y > this.maxY)
			return new Vec3(paramVec3.x, this.maxY, paramVec3.z);
		if (paramVec3.z > this.maxZ)
			return new Vec3(paramVec3.x, paramVec3.y, this.maxZ);
		return paramVec3;
	}
}
