package net.atlas.combatify.config.cookey.category;

import net.atlas.combatify.config.cookey.ModConfig;
import net.atlas.combatify.config.cookey.option.BooleanOption;
import net.fabricmc.loader.api.FabricLoader;

public class MiscCategory extends Category {
    private final BooleanOption showOwnNameInThirdPerson;
	private final BooleanOption showModButton;
    private final BooleanOption fixLocalPlayerHandling;
	public final BooleanOption force100PercentRecharge;

    public MiscCategory(ModConfig modConfig) {
        super(modConfig);
        showOwnNameInThirdPerson = this.register(new BooleanOption("showOwnNameInThirdPerson", this, false));
		BooleanOption modButtonOpt = new BooleanOption("showModButton", this, true);
		this.showModButton = FabricLoader.getInstance().isModLoaded("modmenu") ? this.register(modButtonOpt) : modButtonOpt;
        fixLocalPlayerHandling = this.register(new BooleanOption("fixLocalPlayerHandling", this, true));
		force100PercentRecharge = register(new BooleanOption("force100PercentRecharge", this, false));
    }

    @Override
    public String getId() {
        return "misc";
    }

    public BooleanOption showOwnNameInThirdPerson() {
        return showOwnNameInThirdPerson;
    }

	public BooleanOption showModButton() {
		return showModButton;
	}

    public BooleanOption fixLocalPlayerHandling() {
        return fixLocalPlayerHandling;
    }

	public BooleanOption force100PercentRecharge() {
		return force100PercentRecharge;
	}
}
