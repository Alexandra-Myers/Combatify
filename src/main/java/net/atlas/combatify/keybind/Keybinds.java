package net.atlas.combatify.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public class Keybinds {
    private final KeyMapping openOptions;

    public Keybinds() {
        openOptions = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.cookeymod_options", InputConstants.UNKNOWN.getValue(), "key.categories.cookeymod"));
    }

    public KeyMapping openOptions() {
        return openOptions;
    }
}
