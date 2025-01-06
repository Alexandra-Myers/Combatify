![Combatify](https://www.bisecthosting.com/images/CF/Combatify/BH_C_header.webp)

***

[![Get a Bisect Hosting Server with 25% off using code atlasdev!](https://www.bisecthosting.com/images/CF/Combatify/BH_C_promo.webp)](https://alexandra-myers.github.io/Promolink)

***

[![Atlas Support Discord](https://www.bisecthosting.com/images/CF/Combatify/BH_C_support.webp)](https://discord.gg/WanSPUmRDG)

***

![Description](https://www.bisecthosting.com/images/CF/Combatify/BH_C_description.webp)

***

**This mod is a port of Mojang's Combat Test 8c to modern versions of MC, with an aim to be as accurate as possible without compromising any usability for the user.**

**This mod completely overhauls the current combat system in favor of CTS, with customizability for servers and clients so that you can get the best experience possible.**

**As well, we aim for a great deal of mod support (as much as can be reasonably expected) to give plenty of options to the end user.**

***

![CTS 8c Features](https://www.bisecthosting.com/images/CF/Combatify/BH_C_features.webp)

***

<div align="center">
  
### Attacks and Basic Combat Changes

</div>

1. Attack speed, reach, damage are different for different weapons
2. Attack damage of the fist increased to 2
2. Now possible to attack through non-solid blocks without breaking them
2. Attack speed alterations
    - Charged hits (200%) give 1 extra block of reach
        - You won't get the reach bonus while sneaking
    - Missing only puts a 4 tick delay until the next attack regardless of weapon.
    - Invulnerability time is 10 ticks unless the attacker's weapon's attack speed is faster
    - The attack timer only resets by performing an attack, no longer affected by switching items

3. Added "Auto-Attack" performed by holding left click
    - Auto attacks are 1 tick slower from optimal fast-hit timing
4. Added "Grace Period", grants leeway in attack timing by providing a window (80% - 100%) where the attack is queued
    - Attacks can no longer be triggered before 100% intentionally
    - Grace Period hits will be delayed until a tick slower than optimal fast-hit timing

4. Reintroduced upwards knockback when hitting players in the air

5. Removed attack reach increase in Creative Mode

5. Changed the swing animation to emphasize the rhythm of the attacks
6. Critical Hit alterations
    - Players can now crit and sprint hit at the same time
    - Weapon enchantments are now included in the base damage when calculating crits and potion effects
7. If an entity's largest dimension for their hitbox (width or height) is less than the minimum hitbox size, it gets expanded by enough to turn the largest dimension into that size

7. Sweeping-Edge alterations
    - Sweep-attacks exist with sweeping enchantment only
    - Nerfed Sweeping-Edge enchantment to 25/33/37.5% percent (was 50/66/75%)

8. Shield alterations
    - Shields protect up to 5 damage for melee attacks, 100% against projectiles
    - Bannered shields protect up to 10 damage for melee attacks
    - Axes disable shields for 1.6 seconds by default
    - Shields activate when sneaking, and you can hit opponents while sneak shielding
    - Option to disable the use-shield-on-crouch in accessibility menu
    - Shields protective arc decreased to 130 degrees
    - Shields now add a 50% knockback resistance when active, 80% for bannered
    - Shields protect against 100% explosion damage
    - Shields are now instant
9. New axe-exclusive enchantment: Cleaving
    - Cleaving enchantment disables shields for 0.5 more seconds per level, maximum level 3
    - Cleaving enchantment gives axes (+2/+3/+4) extra damage
    - Axes now get Cleaving instead of Sharpness from enchantment table
10. Axes and Hoes now take 1 durability for attacking
11. Strength I/II now adds +20%/+40% (was +3/+6 damage) (Matched for Weakness)

12. Tridents with impaling now deal enchantment damage to all mobs that are in water or rain


<div align="center">
  
### Projectiles

</div>

1. Projectiles don't trigger invulnerability ticks
2. Arrow uncertainty value decreased from 1.0 to 0.25
3. Added bow fatigue
    - Bow Fatigue increases the uncertainty over time starting once you hold it for over 3 seconds
    - This is shown via the item shaking more aggressively in the player's hand
4. Changed how the user's momentum is passed onto projectiles
    - Now ignores the vertical momentum of the player entirely
3. Instantaneous effects on tipped arrows are now scaled by 1/8, just like the duration of other effects

4. Trident alterations
    - Tridents can be shot from dispensers
    - Tridents with Loyalty which fall into the void will return to their owner

5. Snowball alterations
    - Snowballs now stack to 64
    - Snowballs and eggs have a 4-tick cooldown
    - Made it possible to hit players with snowballs


<div align="center">
  
### Hunger

</div>

1. Eating gets reset when you get hit
2. Natural healing is faster (every 2 seconds, was 8)
3. Starvation is faster (every 2 seconds, was 8)

4. Natural healing works down to 6 food points, previously 18
5. Natural healing now drains food points directly with a 50% chance to not consume a food point. Saturation is not used when healing damage, and is only relevant as a "pause" until food drains (as originally intended)
6. Saturation is no longer added by food; if current saturation is higher than the amount for the food, nothing happens, otherwise, your saturation is set to the amount for the food.

7. Liquid food (stews, honey, milk, potions) can now be consumed in 20 ticks
8. Healing potions heal 6 points per level, and harming potions harm 6 points per level (4 previously)

***

![Combatify Exclusive Features](https://www.bisecthosting.com/images/CF/Combatify/BH_C_exclusive.webp)

***

1. Sword Blocking: Config option that defines whether swords should be able to block.

2. Midair KB: Config option that defines if you should be able to knock opponents further into the air.

3. Fishing Rod KB: Config option that brings back the 1.8 Fishing Rods in a revised form.

4. Healing Mode: Controls how healing is implemented.

5. Fast Healing: Config option that restores the 1.9 Saturation Fast Healing.

6. Mobs Can Guard: Allows mobs to use a shield in their off-hand under certain conditions. Also provides spawning with a shield for supported mobs.

7. Mobs Can Sprint: Lets certain mobs begin to sprint based on distance and other factors. Designed for melee mobs.

***

[![Get a Bisect Hosting Server with 25% off using code atlasdev!](https://www.bisecthosting.com/images/CF/Combatify/BH_C_promo.webp)](https://alexandra-myers.github.io/Promolink)

***

## License

This mod is licensed under the [GPL-3.0 License](./LICENSE).

