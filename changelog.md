#### Changes:
***
- Updated to Defaulted 1.2
- Tiered Shields' item json now provides their texture to the special model generator rather than their tier
- Improved JSON readability and functionality for `combatify:weapon_types/hoe` and `combatify:weapon_types/spear`
- JavaScript now supports defining variables before functions, `EntityWrapper<E>` now provides `getUUID()`, `resetAttackStrengthTicker(boolean hit)` moved down to `LivingEntityWrapper<LE>`, which now provides `resetAttackStrengthTicker(boolean hit, boolean force)`, `swingInHand(String hand)`, and `swingInHand(String hand, boolean force)`, `PlayerWrapper<P>` now provides `isAttackAvailable(float baseTime)`, `isAttackAvailable(float baseTime, ItemStackWrapper stack)`, `stabAttack(String slot, EntityWrapper<?> entityWrapper, float damage, boolean dealDamage, boolean dealKnockback, boolean dismountTarget)`, and `lungeForwardMaybe()`
***
#### Config Changes:
***
- Nothing to see here
***
#### Fixes:
***
- No longer registers unnecessary entity model layers
