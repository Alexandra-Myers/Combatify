package net.atlas.combatify.util;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.conditional.IsUsingItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record IsBlocking() implements ConditionalItemModelProperty {
	public static final MapCodec<IsUsingItem> MAP_CODEC = MapCodec.unit(new IsUsingItem());

	public IsBlocking() {
	}

	public boolean get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i, ItemDisplayContext itemDisplayContext) {
		if (livingEntity == null) {
			return false;
		} else {
			return livingEntity.isBlocking() && MethodHandler.getBlockingItem(livingEntity).stack() == itemStack;
		}
	}

	public MapCodec<IsUsingItem> type() {
		return MAP_CODEC;
	}
}
