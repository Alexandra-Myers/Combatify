package net.atlas.combatify.util.blocking.condition;

import com.mojang.serialization.MapCodec;
import net.atlas.combatify.util.IDUtils;
//? >1.21.1 {
/*import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs.LateBoundIdMapper;
*///?} <=1.21.1 {
import net.atlas.defaulted.utils.LateBoundIdMapper;
//?}
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class BlockingConditions {
	public static final LateBoundIdMapper<@NotNull ResourceLocation, @NotNull MapCodec<? extends BlockingCondition>> ID_MAPPER = new LateBoundIdMapper<>();
	public static final MapCodec<BlockingCondition> MAP_CODEC = ID_MAPPER.codec(ResourceLocation.CODEC)
		.dispatchMap("condition", BlockingCondition::type, Function.identity());

	public static void bootstrap() {
		ID_MAPPER.put(Unconditional.ID, Unconditional.MAP_CODEC);
		ID_MAPPER.put(RequiresEmptyHand.ID, RequiresEmptyHand.MAP_CODEC);
		ID_MAPPER.put(ItemMatches.ID, ItemMatches.MAP_CODEC);
		ID_MAPPER.put(AllOf.ID, AllOf.MAP_CODEC);
		ID_MAPPER.put(AnyOf.ID, AnyOf.MAP_CODEC);
	}
}
