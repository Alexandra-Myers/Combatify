package net.atlas.combatify.mixin;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.atlas.combatify.util.UtilClass.renderingThread;

@Mixin(Main.class)
public class MainMixin {

	@Inject(method = "main", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;setName(Ljava/lang/String;)V"), remap = false)
	private static void injectThread(String[] strings, CallbackInfo ci) {
		renderingThread.setName("Render thread");
	}
	@Inject(method = "main", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;initGameThread(Z)V"), remap = false)
	private static void rename(String[] strings, CallbackInfo ci) {
		Thread.currentThread().setName("Game thread");
	}
}
