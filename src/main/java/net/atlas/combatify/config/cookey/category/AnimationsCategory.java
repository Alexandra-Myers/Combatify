package net.atlas.combatify.config.cookey.category;

import net.atlas.combatify.config.cookey.ModConfig;
import net.atlas.combatify.config.cookey.option.BooleanOption;
import net.atlas.combatify.config.cookey.option.DoubleSliderOption;

public class AnimationsCategory extends Category {
    private final BooleanOption swingAndUseItem;
    private final DoubleSliderOption sneakAnimationSpeed;
    private final BooleanOption disableCameraBobbing;
    private final BooleanOption enableToolBlocking;
    private final BooleanOption showEatingInThirdPerson;

    public AnimationsCategory(ModConfig modConfig) {
        super(modConfig);
        swingAndUseItem = this.register(new BooleanOption("swingAndUseItem", this, false));
        sneakAnimationSpeed = this.register(new DoubleSliderOption("sneakAnimationSpeed", this, 1.0, 0.0, 2.0));
        disableCameraBobbing = this.register(new BooleanOption("disableCameraBobbing", this, false));
        enableToolBlocking = this.register(new BooleanOption("enableToolBlocking", this, false));
        showEatingInThirdPerson = this.register(new BooleanOption("showEatingInThirdPerson", this, false));
    }

    @Override
    public String getId() {
        return "animations";
    }

    public BooleanOption swingAndUseItem() {
        return swingAndUseItem;
    }

    public DoubleSliderOption sneakAnimationSpeed() {
        return sneakAnimationSpeed;
    }

    public BooleanOption disableCameraBobbing() {
        return disableCameraBobbing;
    }

    public BooleanOption enableToolBlocking() {
        return enableToolBlocking;
    }

    public BooleanOption showEatingInThirdPerson() {
        return showEatingInThirdPerson;
    }
}
