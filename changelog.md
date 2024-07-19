### Changes:
***
- Now requiring Java 21.

- Integration of CookeyMod directly into Combatify

- Now requiring Cloth Config

- Un-integration of Atlas Lib out of Combatify

- New primary config in `config/combatify/combatify-general.json`

- Reformatted the config, now sorted between CTS and custom

- Added Attack Indicator Min Value

- Removed automatic attack speed conversion for modded tools/weapons

- Added support for the Mace by default.

- Removal of unused client options

- Removal of "1.7 Fishing Rod" client-side option... Unfitting and not necessary

- Integration of Polymer for the Cleaving enchantment (optional dependency)

- Added `minecraft:enchantable/axe` and `minecraft:enchantable/breach` item tags, breach is used for the Breach enchantment and axe is used for Cleaving
  * Longswords now inherit Breach by default

- Added `combatify:double_tier_durability` item tag for if an item will have double its tier's durability

- Breach now applies the Armour Piercing tooltip

- Slightly adjusted the Netherite Shield texture
***
#### Config Changes:
***
- New config GUI

- Removed Piercer as a custom enchantment, functionality replaced by Breach

- Added the config option `attackDecay`, controls whether weapon strength and crits are based off of charge

- Added the config option `bedrockImpaling`, controls whether impaling works in rain

- Added the config option `bowFatigue`, controls whether bows can fatigue

- Added the config option `canAttackEarly`, controls whether you can attack before you have 100%

- Added the config option `canSweepOnMiss`, controls whether sweeping works on missed attacks

- Added the config option `chargedAttacks`, controls whether your charge goes up to 200%

- Added the config option `chargedReach`, controls whether you gain reach with a charged attack (independent of chargedAttacks)

- Added the config option `creativeReach`, controls whether you gain reach in creative

- Added the config option `ctsKB`, controls whether the knockback is like CTS

- Added the config option `ctsMomentumPassedToProjectiles`, controls whether momentum is passed to projectiles like in CTS (off is how it is in vanilla)

- Added the config option `dispensableTridents`, controls if tridents can be shot from dispensers

- Added the config option `hasMissTime`, controls whether you have miss time, which is a 1.9 feature which adds 10 ticks of attack delay upon missing

- Added the config option `iFramesBasedOnWeapon`, controls if the I-Frames an opponent recieves will match the ticks it takes for a fast attack with your weapon.

- Added the config option `missedAttackRecovery`, controls whether you can attack in just 4 ticks after missing

- Added the config option `percentageDamageEffects`, controls whether strength and weakness are percentages

- Added the config option `resetOnItemChange`, controls whether your attacks are reset when you change items

- Added the config option `snowballKB`, controls whether snowballs do KB to players

- Added the config option `strengthAppliesToEnchants`, controls whether strength or weakness apply on top of enchantment damage

- Added the config option `sweepWithSweeping`, controls whether you can sweep with just the sweeping enchantment

- Added the config option `swingThroughGrass`, controls whether you can attack through grass or other no collision and no occlusion blocks

- Added the config option `tridentVoidReturn`, controls whether tridents will return from the void with Loyalty

- Added the config option `vanillaSweep`, controls whether sweeping will have its vanilla formula

- Added the config option `weaponTypesEnabled`, controls whether weapon types are used

- Added the config option `shieldDelay`, controls how long your shield has to be up before it functions (in ticks)

- Added the config option `healingTime`, controls how fast you regenerate without `fastHealing` (in seconds)

- Added the config option `instantTippedArrowEffectMultiplier`, controls the multiplier by which a tipped arrow with an instantaneous potion's effect is applied.

- Added the config option `armorPiercingDisablesShields`, off by default, allows items with armour piercing to disable shields at expense of dealing normal damage through armour

- Added the config option `canInteractWhenCrouchShield`, controls whether you can attack while crouch-shielding

- Added the config option `disableDuringShieldDelay`, controls whether you can be disabled while your shield is on delay

- Added the config option `sweepingNegatedForTamed`, controls whether your own tamed animals can be hit with sweeping

- Added the config option `attackDecayMinCharge`, controls the minimum charge required for the damage to start increasing when `attackDecay` is enabled

- Added the config option `attackDecayMaxCharge`, controls the charge to reach maximum damage when `attackDecay` is enabled

- Added the config option `attackDecayMinPercentage`, controls the minimum percentage of damage you can deal when `attackDecay` is enabled

- Added the config option `attackDecayMaxPercentage`, controls the maximum percentage of damage you can deal when when `attackDecay` is enabled

- Added the config option `breachArmorPiercing`, controls how much armour piercing Breach grants per level

- Added the config option `thrownTridentDamage`, controls the base damage tridents do when thrown

- Added the config option `arrowDisableMode` which defines if, and in which circumstances, arrows can disable shields

- Added the config option `armourPiercingMode`, control how armour piercing applies to armour

- Removed the config option `swordProtectionEfficacy`, obsolete

- Added the items config setting `can_sweep` for both singular items and weapon types, controls whether you can sweep with this item by default

- Added the default blocking type `current_shield`, provides 1.9 shield functionality

- Refactored items config, now defines a default set of weapon types, blocking types, and tiers, which can be expanded by mods and ensured that they will never be removed.

- Added tiers to the items config
  * Details can be found in the wiki

- Added the ability to add custom weapon types
  * Weapon types can also be configured multiple at once, like items

- Added `sword_enchants_from_enchanting` field to both weapon type and item configs, controls whether an item can have sword enchants from an enchanting table

- Removed `is_percentage` from blocking types

- Added `durability` to item configuration, allows for you to control an item's durability

- Added `tier` to item configuration, allows for you to apply a custom tier to an item. Diamond is assumed by default if an item doesn't have a tier but has functionality that relies on it

- Tiered shields now always have double the durability of other items representing that tier

- Added `armor_calculation` to the items config

- Added `armor`, `armor_toughness`, and `armor_knockback_resistance` to the items config for adjusting item armor stats

- Added `repair_ingredient` to the items config for changing the repair ingredient of an item

- Added `tool_tag` to the items config for changing the tag which a tool will be able to mine (e.g. `#minecraft:mineable/pickaxe)

- Added the blocking type `shield_no_banner`, which is the same as the default shield but is not better when it has a banner
***
#### Fixes:
***
- Fixed sweeping during miss after dying

- Fixed attack speed with vanilla stats

- Fixed shielding by default with mounts

- Fixed potion eating interruption (somehow?)

- Fixed several incompatibilities

- Fixed axes being able to receive sword enchantments from the enchanting table by default

- Fixed [MC-109101](https://bugs.mojang.com/browse/MC-109101) not being fixed in Combatify

- Fixed hoe not working with custom tiers

- Fixed server crash

- Fixed Swing Through Grass performance

- Fixed syncing shield-crouching with the server
