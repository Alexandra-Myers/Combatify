### Changes:
***
- Items Config Alterations.
  * The items config will now be named `combatify-items-v3` to limit the amount of changes to existing configurations that need to be made, and to ensure v2 configurations will remain functional.
  * Now has a config GUI
- Now depending on Defaulted for default item components.
- Added a set of datapacks to replace otherwise lost config options.
- Added vanilla server support.
- Updated Swing Through Grass implementation.
- Blocking implementations now delegated to `combatify:blocker`.
- Added `defaulted:combat_test_stats_generator` Patch Generator to Defaulted.
- Weapon Types are now entirely an internal utility.
- Removed `combatify:wooden_shield`, now uses Defaulted to modify the vanilla Shield.
- Removed `requires_sword_blocking` Blocking Condition, as the config option no longer exists.
- Added tags `combatify:weapon_type/<weapon_type>` for all vanilla weapon types, containing all correctly tiered vanilla items.
  * For modded items, give them a tier using Defaulted and then add them to the correct weapon type tag (or manually create a weapon type for them), they are not adapted by default.
- Added default component patches `combatify:drinkables`, `combatify:potion_size`, and `combatify:snowball_size` to the mod data (outside of built-in datapacks)
***
#### Config Changes:
***
- Removed most of the Items Config's Items block, and completely removed `tiers` and `weapon_types`.
- Removed `weaponTypesEnabled`, `swordBlocking`, and `ctsAttackBalancing`, replaced with built-in datapacks.
- `fistDamage` changed from boolean to double, now directly controlled by the user.
- Added `tierDamageNerf`, controls whether or not the nerf of tier attack damage bonus from the Combat Tests should apply.
- Removed all hunger system configs in replacement of a single `foodImpl` config option, defining a JS file to use for food implementations.
- Removed `critControls` to instead use `critImpl`, similar to `foodImpl` in purpose.
- Armor calculations are now controlled using a JS file.
***
#### Fixes:
***
- No longer incompatible with OwO Lib.
