package net.atlas.combatify.annotation.mixin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Incompatible {
    /**
     * Declares certain mods incompatible with a mixin.
     * All mod ids listed here will prevent the mixin from loading
     * if they are active in the environment.
     */
    String[] value() default "";
}
