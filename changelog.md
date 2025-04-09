### Changes:
***
- Version bump
***
#### Config Changes:
***
- Removed all hunger system configs in replacement of a single `foodImpl` config option, defining a JS file to use for food implementations.
- Removed `critControls` to instead use `critImpl`, similar to `foodImpl` in purpose.
- Armor calculations are now controlled using a JS file.
***
#### Fixes:
***
- Fixed trident void return
