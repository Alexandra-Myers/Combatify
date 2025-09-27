### Changes:
***
- Added separate datapacks for the two mace weapon types.
- Added a "Projectile Charge Indicator", showing when a projectile can be shot from weapons which use a charge.
- Sodium Video Settings Support.
- Updated all custom shield textures to better align with the Copper Shield added in 1.21.9
***
#### Config Changes:
***
- Added the config option `disableLoyaltyOnHitEntity`, disabling the Trident immediately returning to the holder when hitting a entity with Loyalty, instead dropping to the ground first.
- Added the config option `delayEntityUpdates`, delaying the entity trackers syncing to the players until after the entity has ticked, fixing MC-297196.
- When `mobsCanSprint` is on, mobs fleeing from the sun will now sprint towards safety.
***
#### Fixes:
***
- Fixed issues with crits when `strengthAppliesToEnchants` is off
- Fixed sweeping dealing full damage
- Swing Through Grass no longer sometimes breaks blocks when not in range.
- Optimised JavaScript implementation by, on average, around 1000x.
- Fixed several bugs caused by backporting to 1.21.1 rather than updating it directly.
