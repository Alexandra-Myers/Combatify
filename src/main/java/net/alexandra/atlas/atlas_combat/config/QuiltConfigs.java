package net.alexandra.atlas.atlas_combat.config;

import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import org.quiltmc.config.api.WrappedConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.config.api.annotations.FloatRange;

import java.util.List;

public class QuiltConfigs extends WrappedConfig {
	@Comment("General feature configs")
	public final GeneralConfigs general = new GeneralConfigs();
	public static class GeneralConfigs implements Section {
		@Comment("Max amount of ticks for the Client to wait before disconnecting")
		public final int maxWaitForPacketResponse = 20;
		@Comment("Duration for drinking")
		public final int milkBucketUseDuration = 20;
		@Comment("Duration for drinking")
		public final int potionUseDuration = 20;
		@Comment("Duration for drinking")
		public final int honeyBottleUseDuration = 20;
		@Comment("Health gained from Instant Health")
		public final int instantHealthBonus = 6;
		@Comment("Snowball cooldown")
		public final int snowballItemCooldown = 4;
		@Comment("Egg cooldown")
		public final int eggItemCooldown = 4;
		@FloatRange(min = 0.0, max = 2.0)
		@Comment("Bow Uncertainty, also known as accuracy. Defines how big the difference in trajectory the arrow can be")
		public final float bowUncertainty = 0.25F;
		@FloatRange(min = 0.0, max = 100.0)
		@Comment("Snowball damage value")
		public final float snowballDamage = 0.5F;
		@Comment("Defines if every tool only takes 1 durability when attacking, and can access Sweeping Edge and Knockback")
		public final boolean toolsAreWeapons = false;
		@Comment("Defines if weapons can use their special function")
		public final boolean specialWeaponFunctions = false;
		@Comment("Defines if Knockback is based off of your momentum")
		public final boolean momentumKnockback = false;
		@Comment("Defines if block reach default length matches Bedrock")
		public final boolean bedrockBlockReach = false;
		@Comment("Defines if axes can use their own knockback function")
		public final boolean axeFunction = true;
		@Comment("Defines if pickaxes can use their own knockback function")
		public final boolean pickaxeFunction = true;
		@Comment("Defines if shovel can use their own knockback function")
		public final boolean shovelFunction = true;
		@Comment("Defines if hoes can use their own knockback function")
		public final boolean hoeFunction = true;
		@Comment("Defines if swords can use their own knockback function")
		public final boolean swordFunction = true;
		@Comment("Defines if tridents can use their own knockback function")
		public final boolean tridentFunction = true;
		@Comment("Defines if block reach is a feature")
		public final boolean blockReach = true;
	}

}
