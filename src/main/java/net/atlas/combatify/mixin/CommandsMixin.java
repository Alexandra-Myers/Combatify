package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.tree.CommandNode;
import net.atlas.atlascore.AtlasCore;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Commands.class)
public class CommandsMixin {
	@ModifyExpressionValue(method = "fillUsableCommands", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/tree/CommandNode;canUse(Ljava/lang/Object;)Z"))
	public boolean removeAtlasConfigForUnmodded(boolean original, @Local(ordinal = 0, argsOnly = true) CommandSourceStack commandSourceStack, @Local(ordinal = 2) CommandNode<?> commandNode) {
		ServerPlayer player = commandSourceStack.getPlayer();
		boolean matches = !commandNode.getName().equals("atlas_config") || player == null || ServerPlayNetworking.canSend(player, AtlasCore.AtlasConfigPacket.TYPE);
		return original && matches;
	}
}
