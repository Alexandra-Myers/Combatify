#### Changes:
***
- Tiered Shields have been redesigned, with some notable changes:
  - Iron, Gold, and Diamond Shields now take Blocks of Coal, Blackstone, and Obsidian respectively to craft
  - Iron, Gold, and Diamond Shields now provide 0.5 Shield Strength
  - Copper Shields now take 1.1x disable time
  - Iron, Gold, and Copper Shields now have higher base durability
  - Golden Shields now take half the durability and have 0.8x disable time
- `combatify:extended_blocking_data` component had its field `considers_banner` replaced with `banner_damage_reductions`
  - Has the same format as the damage reductions on `minecraft:blocks_attacks`, however will be applied in addition when the shield has a banner
- On 1.21.1, `combatify:blocker` has been altered to suit the new necessary functionalities for the tiered shields
  - Added the field `item_damage`, matching the equivalent on the `minecraft:blocks_attacks` component in vanilla
  - Added the field `disable_cooldown_scale`, scales the amount of disable time this blocking item will take
***
#### Config Changes:
***
- Nothing to see here
***
#### Fixes:
***
- Fixed pack format for built-in datapacks (no longer shows unsupported)
- Fixed creative block breaking being slow on 1.21.11
- Fixed durability taken by shields having an improper threshold
