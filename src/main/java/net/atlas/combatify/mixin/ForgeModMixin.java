package net.atlas.combatify.mixin;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ForgeMod.class)
public class ForgeModMixin {
    @Shadow @Final private static DeferredRegister<Attribute> ATTRIBUTES;

    @Shadow
    @Mutable
    @Final
    public static RegistryObject<Attribute> ENTITY_REACH = ATTRIBUTES.register("attack_reach", () -> new RangedAttribute("attribute.name.generic.attack_reach", 2.5, -1024.0, 1024.0).setSyncable(true));
}
