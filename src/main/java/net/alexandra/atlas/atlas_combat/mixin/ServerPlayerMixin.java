package net.alexandra.atlas.atlas_combat.mixin;


import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.alexandra.atlas.atlas_combat.extensions.LivingEntityExtensions;
import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin {
	public ServerPlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}
}
