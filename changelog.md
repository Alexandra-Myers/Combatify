### Changes:
***
- Non-player cooldowns.

- Beginning of the items config refactor

- AppleSkin support

- New enchantment effect component: `combatify:shield_effectiveness`, modifies the effectiveness of the blocking type.

- Added data components `combatify:blocking_level` and `combatify:piercing_level`. The former controls modifiers to the base blocking protection for the type, effective for all non-vanilla types.
   * `combatify:blocking_level` is only used for unmodified blocking protection on the item. If the item's `damage_protection` is set, that exact value will be used, ignoring the component.

- Removed integrated CookeyMod, now an optional dependency

- The `minecraft:use_cooldown` component will apply in the few cases mobs use items.

- For Beta 3 onwards, the items config will now be named `combatify-items-v2` to limit the amount of changes to existing configurations that need to be made, and to ensure v1 configurations will remain functional.

- Other new enchantment effect component: `combatify:post_block_effects`, not much to say other than it is the effects to be triggered after an attack is blocked.

- Other new data components: `combatify:blocker` and `combatify:can_sweep`, relatively straight-forward to understand what they do.
***
#### Config Changes:
***
- Removed the config option `breachArmorPiercing`, now replaced with calculating based on the `minecraft:armor_effectiveness` component.

- Added the config option `mobsCanGuard`, enables mobs to use shields under certain conditions.

- Added the config option `mobsCanSprint`, adds sprinting to certain mobs, under certain conditions, + sprint hits.

- Added the `entities` block to the items config. Controls aspects of certain entities, currently `attack_interval`, `shield_disable_time`, and `is_misc_entity`.

- Items config refactor, internally now maintains all individual changes, and tags can now be used as a discriminator for items and entities (and multiple tags, if you fancy).

- New blocking type factory: `combatify:original_sword`, uses sword mechanics from 1.7 (and 1.8, to my knowledge).

- Replaced `is_enchantable` and `enchantment_level` in the items block in the items config with `enchantable` which uses the `minecraft:enchantable` component.

- Modified `cooldown` to be encoded the same as vanilla's `minecraft:use_cooldown` component.

- Removed `cooldown_after`, completely pointless in every way.

- Prior `repair_ingredient` entries in the tiers and items block are now `repair_items` and are now tags instead of ingredients.

- Split `mining_level` in the tier codec into `weapon_level` and `blocking_level` respectively.
   * `weapon_level` controls the level as used for weapon type calculations, whereas `blocking_level` directly modifies the default `combatify:blocking_level` component.

- Added the optional field `blocking_level` to the `blocking_information` codec. This takes precedence over the tier's `blocking_level` when used.

- `blocking_type` in both weapon type and item configurations now encodes the default `combatify:blocker` for the items, rather than encoding a direct blocking type.
***
#### Fixes:
***
- Shield durability is now taken in most cases where it was not before

- Fixed third-person shield blocking animation

- Probably fixed bugs related to blocking; at the very least, I do not experience them, but I do not know if they were there before either.
