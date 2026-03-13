#### Changes:
***
- Updated to Atlas Core 1.1.3.
- Added builtin datapack `combatify:weapon_tweaks`, provides a Trident recipe, changes Cleaving to match Sharpness's damage in exchange for better disable time, buffs base disable time for the axe, makes spear work more like a normal cts weapon, and adjusts the mace to have less base damage.
- Removed builtin JS food & crit impls.
- Resorted config categories away from file type to instead properly categorise them.
  - This will not affect existing configs, as the new Atlas Core update allows for this to be an easy transition.
***
#### Config Changes:
***
- `foodImpl`'s format has been changed, now being an object with multiple fields.
  - `type`: One of `minecraft:combat_test_8c`, `combatify:combat_test_9a`, and `minecraft:javascript`.
  - For `minecraft:combat_test_8c` only:
    - `cts_saturation_cap`: boolean
    - `cts_healing`: boolean
  - For `minecraft:combat_test_8c` and `combatify:combat_test_9a`:
    - `minimum_sprint_level`: int range 0-20
    - `minimum_healing_level`: int range 0-20
    - `minimum_fast_healing_level`: int range 0-21, 21 will be interpreted as no fast healing
    - `fast_heal_seconds`: non-negative float
    - `heal_seconds`: non-negative float
    - `starvation_seconds`: non-negative float
  - For `minecraft:javascript` only:
    - `script`: string filename (no extension)
- `critImpl`'s format has been changed, now being an object with multiple fields.
  - `type`: One of `minecraft:combat_test_8c`, `combatify:charged_crits`, and `minecraft:javascript`.
  - For `minecraft:combat_test_8c` and `combatify:charged_crits`:
    - `allows_sprint`: boolean
  - For `minecraft:combat_test_8c` only:
    - `minimum_charge`: float range -1-2, if negative will be inferred as not applicable
    - `crit_multiplier`: non-negative float
  - For `combatify:charged_crits` only:
    - `minimum_base_charge`: float range -1-2, if negative will be inferred as not applicable
    - `minimum_full_charge`: float range -1-2, if negative will be inferred as not applicable
    - `crit_multiplier`: non-negative float
    - `charged_crit_multiplier`: non-negative float
  - For `minecraft:javascript` only:
    - `script`: string filename (no extension)
***
#### Fixes:
***
- Nothing to see here.
