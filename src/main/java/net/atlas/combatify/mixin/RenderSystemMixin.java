package net.atlas.combatify.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.atlas.combatify.util.TestBlockingType;
import net.atlas.combatify.util.UtilClass;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Mixin(value = RenderSystem.class, remap = false)
public class RenderSystemMixin {
	@Shadow
	@Nullable
	private static Thread renderThread;

	@Shadow
	@Nullable
	private static Thread gameThread;

	@Shadow
	@Final
	static Logger LOGGER;

	@Redirect(method = "initRenderThread", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;currentThread()Ljava/lang/Thread;", ordinal = 1))
	private static Thread initRenderThread() {
		return UtilClass.renderingThread;
	}

	@Redirect(method = "isOnRenderThread", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;currentThread()Ljava/lang/Thread;"))
	private static Thread isOnRenderThread() {
		return UtilClass.renderingThread;
	}

	@Redirect(method = "initGameThread", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;currentThread()Ljava/lang/Thread;", ordinal = 0))
	private static Thread initGameThread(boolean bl) {
		return UtilClass.renderingThread;
	}
}
