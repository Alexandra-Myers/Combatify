### Changes:
***
- Added a client option to restore vanilla arm height augmentation

- Polymer is now an optional dependency once again, required for server-side only support
***
#### Config Changes:
***
- Added the config option `axesAreWeapons`, controls the durability taken by axes when attacking.

- Added the config option `critControls`, controls various aspects about critical hits.

- Consolidated all Attack Decay related configs into `attackDecay`.
  * Note: If updating from a previous version, delete the old `attackDecay` from the config file.

- Split the min and max percentages for attack decay into one for base damage and one for enchants

- Removed the config option `sprintCritsEnabled`, replaced with its equivalent in `critControls`
***
#### Fixes:
***
- Fixed attack decay and how it is applied

- Config options which used to require a restart unnecessarily now listen for changes and update in-game

- It is now, once again, actually possible to connect from a vanilla client with `letVanillaConnect` enabled

- Fixed... many incompatibilities, probably I haven't seen any that were caused by the previous versions of this code yet
