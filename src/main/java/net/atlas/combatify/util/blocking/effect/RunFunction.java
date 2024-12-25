package net.atlas.combatify.util.blocking.effect;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;

public record RunFunction(ResourceLocation function) implements PostBlockEffect {
	public static final ResourceLocation ID = ResourceLocation.withDefaultNamespace("run_function");
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final MapCodec<RunFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(ResourceLocation.CODEC.fieldOf("function").forGetter(RunFunction::function)).apply(instance, RunFunction::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, RunFunction> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(RunFunction::new, RunFunction::id).mapStream(buf -> buf);

	@Override
	public void doEffect(ServerLevel serverLevel, EnchantedItemInUse enchantedItemInUse, LivingEntity attacker, DamageSource damageSource, int enchantmentLevel, LivingEntity toApply, Vec3 position) {
		MinecraftServer minecraftServer = serverLevel.getServer();
		ServerFunctionManager serverFunctionManager = minecraftServer.getFunctions();
		Optional<CommandFunction<CommandSourceStack>> optional = serverFunctionManager.get(this.function);
		if (optional.isPresent()) {
			CommandSourceStack commandSourceStack = minecraftServer.createCommandSourceStack()
				.withPermission(2)
				.withSuppressedOutput()
				.withEntity(toApply)
				.withLevel(serverLevel)
				.withPosition(position)
				.withRotation(toApply.getRotationVector());
			serverFunctionManager.execute(optional.get(), commandSourceStack);
		} else {
			LOGGER.error("Blocking effect run_function failed for non-existent function {}", this.function);
		}
	}

	@Override
	public MapCodec<? extends PostBlockEffect> type() {
		return MAP_CODEC;
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

	public static void mapStreamCodec(Map<ResourceLocation, StreamCodec<RegistryFriendlyByteBuf, PostBlockEffect>> map) {
		map.put(ID, STREAM_CODEC.map(runFunction -> runFunction, postBlockEffect -> (RunFunction) postBlockEffect));
	}
}
