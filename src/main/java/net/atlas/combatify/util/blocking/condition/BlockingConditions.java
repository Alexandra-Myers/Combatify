package net.atlas.combatify.util.blocking.condition;

import com.mojang.serialization.MapCodec;
//? >1.21.1 {
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs.LateBoundIdMapper;
//?} <=1.21.1 {
/*import net.atlas.defaulted.utils.LateBoundIdMapper;
*///?}
import org.jspecify.annotations.NonNull;

import java.util.function.Function;

public class BlockingConditions {
	public static final LateBoundIdMapper<@NonNull Identifier, @NonNull MapCodec<? extends BlockingCondition>> ID_MAPPER = new LateBoundIdMapper<>();
	public static final MapCodec<BlockingCondition> MAP_CODEC = ID_MAPPER.codec(Identifier.CODEC)
		.dispatchMap("condition", BlockingCondition::type, Function.identity());

	public static void bootstrap() {
		ID_MAPPER.put(Unconditional.ID, Unconditional.MAP_CODEC);
		ID_MAPPER.put(RequiresEmptyHand.ID, RequiresEmptyHand.MAP_CODEC);
		ID_MAPPER.put(ItemMatches.ID, ItemMatches.MAP_CODEC);
		ID_MAPPER.put(AllOf.ID, AllOf.MAP_CODEC);
		ID_MAPPER.put(AnyOf.ID, AnyOf.MAP_CODEC);
	}
}
