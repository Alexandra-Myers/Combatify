### Changes:
***
- Port to 1.21

- Removal of Polymer as an optional dependency

- Added built-in datapacks for the custom content to prevent error spamming in console (and to allow Defender to keep existing)

- Added `Shield Disable Time` attribute, controls how much extra Shield Disable Time is applied when disabling an opponent's shield

- Added `Shield Disable Reduction` attribute, controls how much Shield Disable Time is negated by the user

- Removed the extra defense granted by Defender, and it can now only be applied to items in the tag `combatify:enchantable/shield`

- Buffed the `Shield Disable Reduction` of Defender back to 1s

- Nerfed the Mace's Attack Speed to 1 by default
***
#### Config Changes:
***
- Removed `vanillaSweep`, `defender`, `cleavingDisableTime`, and `defenderDisableReduction` due to no longer being needed, now having other equivalents

- Removed all instances of `has_sword_enchants` and `sword_enchants_from_enchanting` as options in the items config for above reasons

- Note: The Defender enchantment is included as part of the built-in datapack Combatify Extras now

- Changed the wooden, golden, and diamond shield recipes (Note: recipes for all custom content are in Combatify Extras, although the Wooden Shield's new recipe is in Wooden Shield Recipe, as per logic would dictate.)
***
#### Fixes:
***
- Fixed log spamming when custom content is disabled
