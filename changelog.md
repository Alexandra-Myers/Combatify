### Changes:
***
- Non-player cooldowns.

- Beginning of the items config refactor
***
#### Config Changes:
***
- Removed the config option `breachArmorPiercing`, now replaced with calculating based on the `minecraft:armor_effectiveness` component.

- Added the config option `mobsCanGuard`, enables mobs to use shields under certain conditions.

- Added the config option `mobsCanSprint`, adds sprinting to certain mobs, under certain conditions, + sprint hits.

- Added the `entities` block to the items config. Controls aspects of certain entities, currently `attack_interval`, `shield_disable_time`, and `is_misc_entity`.

- Items config refactor, internally now maintains all individual changes, and tags can now be used as a discriminator for items and entities (and multiple tags, if you fancy)
***
#### Fixes:
***
- Shield durability is now taken in most cases where it was not before
