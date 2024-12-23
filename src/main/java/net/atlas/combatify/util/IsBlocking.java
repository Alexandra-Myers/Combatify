package net.atlas.combatify.util;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record IsBlocking() implements ConditionalItemModelProperty {
	public static final IsBlocking INSTANCE = new IsBlocking();
	public static final MapCodec<IsBlocking> MAP_CODEC = MapCodec.unit(INSTANCE);

	public boolean get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i, ItemDisplayContext itemDisplayContext) {
		return livingEntity != null && livingEntity.isBlocking() && MethodHandler.getBlockingItem(livingEntity).stack() == itemStack;
	}

	public MapCodec<IsBlocking> type() {
		return MAP_CODEC;
	}
}
