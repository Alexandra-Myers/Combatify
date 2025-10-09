package net.atlas.combatify.mixin.compatibility.appleskin.neoforge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.wrapper.FoodDataWrapper;
import net.atlas.combatify.config.wrapper.FoodPropertiesWrapper;
import net.atlas.combatify.config.wrapper.PlayerWrapper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import squeek.appleskin.api.event.HUDOverlayEvent;
import squeek.appleskin.client.HUDOverlayHandler;
import squeek.appleskin.helpers.FoodHelper;

@Pseudo
@Mixin(targets = {"squeek.appleskin.client.HudOverlayHandler$HungerOverlay"})
public class HungerOverlayMixin {
	@SuppressWarnings("UnresolvedMixinReference")
	@WrapOperation(method = "Lsqueek/appleskin/client/HudOverlayHandler$HungerOverlay;render(Lnet/minecraft/client/Minecraft;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/client/gui/GuiGraphics;IIII)V", at = @At(value = "INVOKE", target = "Lsqueek/appleskin/client/HUDOverlayHandler;drawHungerOverlay(Lsqueek/appleskin/api/event/HUDOverlayEvent$HungerRestored;Lnet/minecraft/world/entity/player/Player;IFZI)V"))
	public void modifyNewSaturation(HUDOverlayHandler instance, HUDOverlayEvent.HungerRestored event, Player player, int hunger, float alpha, boolean useRottenTextures, int guiTicks, Operation<Void> original, @Local(ordinal = 0) FoodData foodData, @Local(ordinal = 0) FoodHelper.QueriedFoodResult foodResult) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			original.call(instance, event, player, hunger, alpha, useRottenTextures, guiTicks);
			return;
		}
		original.call(instance, event, player, (int) Combatify.CONFIG.getFoodImpl().execGetterFunc(hunger, "estimateGainedFoodLevel(foodData, player, foodProperties)", new FoodDataWrapper(foodData), new PlayerWrapper<>(player), new FoodPropertiesWrapper(foodResult.modifiedFoodComponent)), alpha, useRottenTextures, guiTicks);
	}
}
