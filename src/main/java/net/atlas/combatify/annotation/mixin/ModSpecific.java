package net.atlas.combatify.annotation.mixin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface ModSpecific {
    /**
     * Declares a mod-specific mixin.
     * Mixins marked with this annotation will only load if one
     * of the mod ids listed here is present in the environment.
     */
    String[] value() default "";
}
