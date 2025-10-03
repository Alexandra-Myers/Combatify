### Changes:
***
- Split Recovery out from Combatify Extras, into new datapack `combatify:shield_enchantments`
  - Two new shield enchants, Thorned (basically Thorns but for a shield), and Curse of Delay (quintuples shield disable time)
- Added `combatify:copper_age_rebalance` datapack, introduces rebalances for the copper (1.21.9 only), gold, and iron tiers to make them all more viable (especially gold) but still outclassed by higher tiers in general
- Snowball stack size and (including eggs) cooldown patches now use tags `combatify:becomes_normal_stack_size` and `combatify:projectiles_with_cooldowns`
- Fast drinkable items are now determined by the tag `combatify:fast_drinkables`
***
#### Config Changes:
***
- Added `sweepConditionsMatchMiss`, allowing sweep attacks when hitting an entity to happen midair unless you use a knockback attack or critical attack
- Added `mobsUsePlayerAttributes`, allowing mobs to have the functions of attack speed and attack reach (with different default values than players)
- Added `aimAssistTicks` (0 by default), controls for how many ticks a picked entity will be retained for, allowing for easier hits, as in CTS 5
- Removed `ctsKB` and `midairKB` consolidated into `knockbackMode` as `cts_8c` and `midair`
  - Now also has the options `vanilla` for vanilla kb, `old` for 1.8 kb, and `cts_5` for knockback from the fifth combat test
- `fishingRodKB` now uses accurate velocity for rods from 1.8
- Mobs now only use shields if they know you can hit them.
- Mobs minimum distance to start sprinting is now based on difficulty (5 blocks on hard, 6 on normal, and 8 on easy)
- Tiered shields have been buffed, now giving 0.3 knockback resistance for base and copper, 0.4 for iron, gold, and diamond, and 0.5 for netherite
  - Netherite Shields now have 1 base protection
  - Now chainmail armor is accompanied by a wooden shield for mobs
- Knives have been nerfed to 2.5 reach
- Retained attacks in vanilla now run missed attacks when they cannot hit a target
***
#### Fixes:
***
- Fixed incompatibilities caused by the knockback delay fix
- Fixed mobs sprinting to flee the sun not working in production
- Fixed Copper Shields not showing base protection in their tooltip
- Fixed datapack errors relating to 1.21.5's `combatify:blocks_attacks` component
- Fixed shield crouch use anim
- Fixed bugged arm model when skeletons use shields
- Fixed 1.21 drinking time and egg/snowball cooldowns
- Fixed dropping an item in vanilla triggering a sweep attack
