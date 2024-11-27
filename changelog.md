### Changes:
***
- Allow any server to enable/disable bedrock bridging for the client on will

- Renamed Defender to Recovery

- Atlas Core updated to 1.1.1
***
#### Config Changes:
***
- Added the config option `ctsSaturationCap`, enables the feature of CTS where saturation is not capped at the food level, but instead the food's saturation is the lowest your saturation can go.

- Added the config option `starvingTime`, controls the speed of starvation

- Added the config option `fastHealingTime`, controls the speed of 1.9 fast healing

- Added the config option `hasteFix`, corrects attack speed modifiers to be the same as if they were based on the base value minus 1.5

- Replaced the config option `eatingInterruption` with `eatingInterruptionMode`, controls how eating interruption is applied

- Replaced the config option `saturationHealing` with `healingMode`, controls how healing works, `fastHealing` can be applied independent of the option here

- Consolidated `bowUncertainty` and `crossbowUncertainty` into `projectileUncertainty`, now holding both options into itself

- Consolidated `snowballDamage`, `eggDamage`, `windChargeDamage`, and `thrownTridentDamage` into `projectileDamage`, now holding said options into itself
***
#### Fixes:
***
- Fixed mining not triggering missed attack recovery

- Fixed Item Config not working

- Fixed Trident spin attack dealing damage based on the item in your main hand

- Fixed `thrownTridentDamage` not applying to spin attack

- Fixed a bug which causes damage to not be modified correctly using custom armour calculations
