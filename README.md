![Combatify](https://www.bisecthosting.com/images/CF/Atlas_Combat/BH_Atlas_Combat_Header.webp)

***

[![Get a Bisect Hosting Server with 25% off using code atlasdev!](https://www.bisecthosting.com/images/CF/Atlas_Combat/BH_Atlas_Combat_Promo.webp)](https://bisecthosting.com/atlasdev)

***

![Description](https://www.bisecthosting.com/images/CF/Atlas_Combat/BH_Atlas_Combat_Description.webp)

***

**This mod is a port of Mojang's Combat Test 8c to modern versions of MC, with an aim to be as accurate as possible without compromising any usability for the user.**

**This mod completely overhauls the current combat system in favor of CTS, with customizability for servers and clients so that you can get the best experience possible.**

**As well, we aim for a great deal of mod support (as much as can be reasonably expected) to give plenty of options to the end user.**

***

![CTS 8c Features](https://www.bisecthosting.com/images/CF/Atlas_Combat/BH_Atlas_Combat_Features.webp)

***

### Attacks and Basic Combat Changes
1. Attack speed, reach, damage are different for different weapons
2. Attack speed alterations
    - Charged hits (200%) give 1 extra block of reach
        - You won't get the reach bonus while sneaking
    - Weak attacks no longer exist, and you’re forced to wait for weapons to charge to 100% to hit
    - Missing only puts a 4 tick delay until the next attack regardless of weapon.
    - Invulnerability time is 10 ticks unless the attacker's weapon's attack speed is faster
    - The attack timer only resets by performing an attack, no longer affected by switching items

3. Added "Auto-Attack" performed by holding left click
    - Auto attacks are 1 tick slower from optimal fast-hit timing

4. Reintroduced upwards knockback when hitting players in the air

5. Changed the swing animation to emphasize the rhythm of the attacks
6. Critical Hit alterations
    - Players can now crit and sprint hit at the same time
    - Weapon enchantments are now included in the base damage when calculating crits and potion effects

7. Sweeping-Edge alterations
    - Sweep-attacks exist with sweeping enchantment only
    - Nerfed Sweeping-Edge enchantment to 25/33/37.5% percent (was 50/66/75%)

8. Shield alterations
    - Shields protect up to 5 damage for melee attacks, 100% against projectiles
    - Bannered shields protect up to 10 damage for melee attacks
    - Axes disable shields for 1.6 seconds by default
    - Shields activate when sneaking, and you can hit opponents while sneak shielding
    - Option to disable the use-shield-on-crouch in accessibility menu
    - Shields protective arc decreased to 100 degrees
    - Shields now add a 50% knockback resistance when active
    - Shields protect against 100% explosion damage
    - Shields are now instant
9. New axe-exclusive enchantment: Cleaving
    - Cleaving enchantment disables shields for 0.5 more seconds per level, maximum level 3
    - Cleaving enchantment gives axes (+2/+3/+4) extra damage
    - Axes now get Cleaving instead of Sharpness from enchantment table
10. Axes and Hoes now take 1 durability for attacking
11. Strength I/II now adds +20%/+40% (was +3/+6 damage)

12. Tridents with impaling now deal enchantment damage to all mobs that are in water or rain


### Projectiles
1. Projectiles don't trigger invulnerability ticks
2. Arrow uncertainty value decreased from 1.0 to 0.25
3. Instantaneous effects on tipped arrows are now scaled by 1/8, just like the duration of other effects

4. Trident alterations
    - Tridents can be shot from dispensers
    - Tridents with Loyalty which fall into the void will return to their owner

5. Snowball alterations
    - Snowballs now stack to 64
    - Snowballs and eggs have a 4-tick cooldown
    - Snowballs are not rendered the first 2 ticks (hack to prevent screen flickering)
    - Made it possible to hit players with snowballs


### Hunger System
1. Eating gets reset when you get hit
2. Natural healing works down to 6 food points, previously 18
3. Natural healing is faster (every 2 seconds, was 4)

4. Natural healing now drains food points directly with a 50% chance to not consume a food point. Saturation is not used when healing damage, and is only relevant as a "pause" until food drains (as originally intended)

5. Liquid food (stews, honey, milk, potions) can now be consumed in 20 ticks

6. Healing potions heal 6 points per level, and harming potions harm 6 points per level (4 previously)

***

![Combatify Exclusive Features](https://www.bisecthosting.com/images/CF/Atlas_Combat/BH_Atlas_Combat_Exclusive.webp)

***

1. Sword Blocking: Config option that defines whether swords should be able to block.

2. Midair KB: Config option that defines if you should be able to knock opponents further into the air.

3. Fishing Rod KB: Config option that brings back the 1.8 Fishing Rods in a revised form.

4. Coyote Time V2: Config option that brings back the Coyote Time from CTS 5 but with changes to make it more acceptable.

5. Saturation Healing: Config option that restores the 1.9 Saturation Healing, making saturation act as a buffer until you lose hunger when healing. This does NOT bring back the Saturation Healing from 1.11 which speeds up healing with high saturation.

6. Fast Healing: Config option that restores the 1.11 Saturation Fast Healing.

***

[![Get a Bisect Hosting Server with 25% off using code atlasdev!](https://www.bisecthosting.com/images/CF/Atlas_Combat/BH_Atlas_Combat_Promo.webp)](https://bisecthosting.com/atlasdev)

***

## License

This mod is licensed under the [GPL-3.0 License](./LICENSE).

