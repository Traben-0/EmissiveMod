[**ETF Changelog:**]


- forge 1.21 & 1.20.6 fixed
- changed gui icon
- added checks to ensure random property suffixes are always valid integers above 0
- fixed crash when rendering entity nameplates and having renderlayer overrides affect entity shadows
- fixed the skin pixel transparency override setting in skin features not affecting all of the pixels correctly
- reworked and improved the `nbt` property to improve both performance and log printing functionality
  - if your nbt test starts with `print_all:` the entire entity client-side nbt will be printed with nice formatting
  - if your nbt test starts with `print:` that specific nbt value will be printed every time it tests, as well as its result
  - (these will go before tests like `raw:`, `exists:` or `range:`, e.g.  `print_all:exists:false`)
- made many minor source code adjustments, no functionality changes, no API changes

[6.1.4]
- fixed crash with minecraft china edition


[6.1.3]
- added additional null checks to fix some odd mod crashes
- fixed the config breaking with some mods
- fixed player skin tool pixel select image being sideways in 1.21


[6.1.2]
- fixed vanilla paintings `textures/painting/aztec.png` & `textures/painting/aztec2.png` not working correctly. (`aztec` now requires a properties file to variate and will not variate with the presence of `aztec2`)


[6.1]

- massive source code rework to move all Minecraft versions into the one source branch, this will have no impact to end users but will massively speed up backporting and deployment of mod updates
- added crowdin translation support
- added skin option to preserve transparency in extra skin pixels
- fixed #265 crash when block entity state is null
- fixed enchanted elytra not rendering emissive textures
- fixed shoulder parrots
- fixed emissive armor and trims breaking with iris
- fixed the custom emissive suffix file declaration getting overridden by higher packs *(using `_e` is still highly recommended)*
- fixed ImmediatelyFast compat for mod update v1.2.16, ETF still retains compat for the older versions
- fixed a missing texture issue with the amendments mod's jukeboxes when playing disc 13 *(this may fix other missing texture issues with modded block entities)*

[6.0.1]

- fixed the transparent skin settings
- changed the transparent skin settings into a single option, it is set to only allow for skins using ETF features by
  default, but can be enabled for all skins.
- ETF player skin features now support legacy format skins and will auto convert them to the new format when saved
- the ETF skin feature tool now has a setting to prevent ETF from adding the example template to your skin, thus no
  longer overwriting any extraneous pixels in your skin
    - the skin tool now prompts users to set this setting when the skin tool first tries to apply one of the templates
      in a way that would override any extraneous skin pixels
    - the templates have been broken up into several smaller partial templates to only try and apply to skins explicitly
      using them
- added gaps into the jacket options text for readability
- fixed coat length setting not being bound between 1 and 8

[6.0]

- added support for the iris distant horizons beta
- fixed emissive textures breaking in the gui with ImmediatelyFast installed
- fixed the `blocks` property not correctly reading/expecting block states
- fixed the `name` property not checking for single names with spaces in them comprised of the entire test string
- completely redid the config gui to be data driven and more user-friendly
    - ETF, EMF, and ESF will now share the same config gui collectively called "Entity Features settings"
    - added a new `per entity` settings screen that allows you to override the settings for specific entity types
    - added a `Random properties` settings screen that allows you to enable/disable, modify, and read the documentation
      of specific random properties. This will also list any properties added via ETF's API.
    - improved many gui elements adding sliders, changed setting highlighting, and text displays for more user-friendly
      settings
- fixed the `name` property not reading player usernames
- added the following random properties: (primarily for use in player EMF packs)
    - `isCreative` - true if the entity is in creative mode
    - `isTeammate` - true if the entity is a teammate
    - `isClientPlayer` - true if the entity is the client player
    - `variant` - moved from EMF into ETF
    - `modLoaded` - works like `name` but matches against the mod id's loaded by the modloader
- the `blocks` property now updates over time by default (OptiFine parity)
- fixed the `nbt` property still including the char type when testing nbt numbers i.e `1b` instead of just `1`
- fixed many minor issues
- fixed `blocks` property and added `blockSpawned` property to be identical but spawn condition locked by default

[5.2.1]

- fixed `Image is not allocated.` crash related to armor rendering
- added a printout to the right click debug to tell users where the `.properties` and variant files can go, useful for
  modded entities.
- slightly tweaked sleeping detection for blinking
- fixed a crash related to unexpected null values #234
- fixed _eye textures not variating correctly since v5.0
- fixed the nose button getting stuck in a loop in the skin settings
- fixed a crash when removing skin features in the skin tool
- added an emissive setting to disable Armor & Trims as it has been known to have issues with modded armors and requires
  future reworking

[5.2]
with the new optimizations in how properties work now I don't need to worry too much about adding too many of them,
that being said, I've added a bunch of new properties mostly introducing some external non-entity values.
Such as irl time & date, and language based localization.
Allowing seasonal entity variation, or language variations for any text in your textures.

- fixed player emissives breaking with bright render mode
- tweaked mob spawner code
- added the `isSpawner` property which is true if the entity is a miniature mob inside a spawner block
- the `color` property now also reads modded entity colors if the entity extends VariantHolder<T> with T being either
  DyeColor or an Optional<DyeColor>
- the `profession` property should work for all modded mobs implementing VillagerDataContainer
- added the following irl time & date properties `hour`,`minute`,`second`,`month`,`year`,`monthDay`,`weekDay`,`yearDay`.
  they are all numeric integers and support ranges e.g. `"0 2 4-7"`.
  `hour`,`minute`,`second` will update over time, the others will only be set when the entity spawns.
  `hour` is in 24-hour format, 0 - 23.
  `weekDay` starts with sunday which is 1 up to saturday at 7.
  `month` starts with january which is 0 up to december at 11.
  `yearDay` is 1 - 366.
  `monthDay` is 1 - 31.
  april fools is thus `month=3, monthDay=1`,The Christmas to new-years period is `month=11, monthDay=25-31`
- added the `dimension` property, which takes in a list of strings, or regex: or pattern:, and will be true if matching
  the entities current dimension.
  Vanilla dimensions are `overworld`, `the_nether`, `the_end`.
  Modded dimension names are entirely up to whatever the mod maker set them to be, expect `mod_name:dimension_name`.
  If you start the property text with `print:` it will work as normal but will also print the found dimension, to help
  with discovering modded dimension names.
- added the `language` property, which takes in a list of strings, or regex: or pattern:, and will match to the users
  chosen in-game language to facilitate the localization of textures if desired. default is `en_us` and corresponds to
  the
  `??_??` format language code of the games language files.
  Helpful if your texture or model includes text for some reason.
- added the `light` property, which takes a list of integers and support ranges e.g. `"0 2 4-7"`, and matches it to the
  light level of the block the entity is standing on `0 - 15`, `-1` will be returned if there is some error.
  This property updates over time. An example use could be differentiating aggressive or passive spiders.

[5.1.2]

- fixed a rare config crash

[5.1.1]

- fixed 3d skin layers mod & etf skin feature, compat for the 1.6 update *(also removed log warning spam for future
  changes to 3d skin layers)*.
- restructured the config
- generified the emissive and enchanted pixel rendering methods
- the mini entities in mob spawners UUID's should now always use the following format in nbt *("[I;?,?,12345,12345]")*
  making them identifiable for randomisation, the blocks property should also return the actual mob spawner block.

[5.1]

- mob spawner entities can now variate their textures again.
- moved some temp rework code into more compatible mixins for mod compatibility
- more stable implementation of the 5.0 rework changes for emissives
- random properties now also allow ranges with the format <high>-<low> instead of only <low>-<high> *(OptiFine parity)*
- fixed a crash in the skin tool
- fixed a shoulder parrot crash

[5.0]

ETF 5.0 is a massive rework of the code, ETF is now almost an entirely different mod in its application from what v4.5
was.
It should now universally affect every entity and entity render feature.

- the `blocks` property now again additionally checks the block below block entities differing from OptiFine's behaviour
- all entities have enchanted overlay support via "_enchant.png" just like emissives with "_e.png"
- much improved and more helpful right click debug printout
- all possible texture variants are now preloaded on the first instance of a texture being used.
- texture variation is now implemented at the creation of entity render layers allowing all entity textures and features
  to be modified by ETF
- emissive rendering now occurs within the normal rendering of all models
- fixed entity spawn conditions not saving for all non update-able properties (e.g. the heights property would not
  always save the entities original spawn height in v4.6)
- fixed the ETF & EMF config screen getting faded black in recent versions
- removed iris z-fighting fixes for everything except armor emissives as they don't appear to be needed anymore
- several changes and optimizations that are not worth listing.
- Added a new config screen category `Debug settings`. the debug right click option is now here. the log texture
  creation setting is now here.
- added a new debug button in the debug settings screen that will print **ALL** currently cached ETF texture data to the
  log, details of every known texture and every variant,
  as well as telling you which textures can and cannot be varied. for example elytra.png shows up in the list of
  textures that can be varied with random entity rules ;)
- removed the ETF cape skin feature, it was fun working with putting a cape inside the skin texture, but it added quite
  a lot
  of overhead, when the minecraftcapes.net mod is just more effective. With this removed I plan to add support for
  player skin feature emissives in whatever cape texture is being used at a future time. Which will be more freeing than
  only applying skin feature emissives to etf loaded capes
- fixed for sodium 0.5.4

removed features due to them now being possible, and preferred, via `EMF`

- removed elytra thickness tweak feature
- removed async elytra feature
- removed lectern book unique texture feature
- removed piglin ear model visibility feature

[4.6.1]

Several fixes, primarily for the random property system rewrite in 4.6.0

- fixed api methods not being static
- fixed texture names ending with numbers not correctly using a "." to separate the variant *(e.g big_chungus_0.png
  should variate with big_chungus_0.2.png)*
- fixed `health` property not working at all in 4.6
- fixed `health` property percentage mode values not being rounded as integers *(an entity with health percentage 3.5%
  would not trigger 0-3% or 4-6% checks, which they do in OptiFine)*
- fixed range property values not working correctly *(e.g 12-33 )*
- further api additions for EMF
- removed some log spam related to player head blocks
-

[4.6.0]

- rewrote the Random Property reading code to be more object-oriented, stable, and simplified.
  This also allows other mods to add new random properties for easy use in ETF & EMF.
  Random property code now only stores the entities initial conditions if they have been tested by that random property
  already, reducing memory usage.
- fixed the `name` property not respecting text formatting codes e.g. "name=\u00a74\u00a7oName"
- fixed crashes during random entity property testing in ETF and EMF, all random property testing should be stable even
  with unexpected failures.
- a lot of source code shuffling around and the inclusion of JavaDocs for API relevant classes.

[4.5.1]

- fixed painting variants only working in the vanilla directory and not the optifine/etf folders
- reverted some changes to `pattern` string comparisons as the OptiFine doc was incorrect compared to actual OptiFine
  behaviour
- fixed a crash related to `pattern` string comparison handling changes in 4.5.0

[4.5]

- added more code to support EMF
- added a new logo
- added support for random & emissive painting textures
- added support for emissive armor trims the same format as OptiFine *(note this seems to break if iris and 3d skin
  layers are both installed, no idea why, it also fixes itself if a mob in the background is wearing the same trim, I'm
  tired of pulling my hair out about this so this is just how it will be for now)*
- added support for armor trim overrides *(e.g. the texture "textures\trims\models\armor\coast_redstone.png" will
  override the autogenerated trim if present)*
- added support for end crystal random and emissive textures
- added an option to enable extra warden textures like the heart to apply to the entire model
- textures ending with numbers now use the separator "." like OptiFine for variants *(e.g "mob4.png" now variates with "
  mob4.2.png")*
- Animatica textures are now detected and prevent certain ETF actions that could break these textures *(MoreMcmeta was
  already supported)*
- added new ETF skin feature to allow/prevent transparency for your skin specifically, plus general improvements to skin
  transparency handling.
- added new ETF skin feature variant of the villager nose setting that can use a custom texture set in the skin, instead
  of the default villager.

- updated the `minecraftcapes.net` api url when used in skin features
- improved the handling of "_eyes" textures
- tweaked the skin tool failure dialogue to be more informative and helpful
- tweaked warning messages and added some
- tweaked the resource-pack screen etf button

- fixed `biome` property breaking when using "CamelCase" instead of "snake_case"
- fixed `size` property being off by 1 when compared to OptiFine
- fixed `name` property not working for players usernames
- fixed custom ETF cape textures not working with `physics mod` capes
- fixed `3D skin layers mod` emissive body pixels with skin features
- tweaked some `en_us` translations
- fixed 2 forge crashes
- fixed `pattern` & `ipattern` to correctly match the OptiFine behaviour

[4.4.4]

- forge crash fix

[4.4.3]

- added support for creeper energy swirl texture variation and emissives, why emissive texture support you ask? because
  it lets you set a static texture for the overlay that doesn't spin but still glows
- Added an option to disable the compatibility patch applied to the `3d skin layers` to enable skin features to work *(
  this only ever needs to be disabled if it conflicts with some other mods version of a `3d skin layers` compatibility
  patch, or if a future `3d skin layers` mod update breaks this)*
- players skulls now correctly reflect the texture they are meant to display, instead of the players current skin if
  online
- Fixed crash caused by unexpected values in property ranges
- Tweaks to player skin settings screen
- Tweaks to classes used by EMF
- api additions

[V4.4.0]

- full `NBT` property parity with OptiFine, ETF now parses the examples in this link correctly
  *(https://optifine.readthedocs.io/syntax.html#nbt)*
- added ETF only `NBT` "raw:" syntax variant "print_raw:" which will act just like "raw:" but will also print what that
  raw value is for testing purposes
- added ETF only `NBT` property syntax which will print the entire entities NBT data to the log, not just the specific
  compound, if the `NBT` property text starts with "print:", this will not affect the function of text after the "
  print:"
- vanilla texture variants like `wolf_tame.png` will fall back to `wolf.properties` if `wolf_tame.properties` doesn't
  exist *(this is true for wolf, bee, ghast & strider texture variants)* *(only for vanilla textures)*
- the `Blocks` property now utilizes the full OptiFine blocks
  syntax `[namespace:]name[:property1=value1,...:property2=value1,...]`
  allowing matches such as `"blocks=oak_stairs:facing=east,west:half=bottom"`
- made `Name`, `Names`, `Biomes` and `Teams` properties more robust and all 3 can now utilise Regex and Pattern not
  just `Names`
- added support for emissive textures on mob head blocks
- added support for emissive and enchanted skin features on player head blocks *(will only work for a player online that
  you have seen at least once that session, I may expand this in future)*
- resolved injection warning on mod load
- improved resource reloading mixin
- altered the livingEntityMixin structure to accommodate plans for `EMF`
- some API additions for `EMF`

[V4.3.5]

- forge version updated to 1.19.4
- fixed crash in skin tool when selecting pixels
- fixed armor stands with nbt "Marker:1b" not working
- improved compat with modded custom boat type textures

[V4.3.3]

- api tweaks
- fixed distanceTo property not working in 4.3.2
- fixed a boat texture crash

[V4.3.2]

- added the `NBT` OptiFine texture property, which can read any NBT value of an entity that is available to the client
- added wolf collar support
- API updated for other mods to utilise ETFs random texture .properties file loading *(will be used in EMF)*
- added an option to enable transparent skins for all players, even ones not using ETF skin features.
- added checks to catch some nullPointer crashes
- fixed an issue requiring other clients to have ETF installed when joining an Essentials mod hosted game
- reworked the handling of all entities internally by ETF, The ETFPlaceHolderEntity EntityType has been removed.
- fixed a crash caused by modded entities with large numbers as texture file name making etf think it is a variant .png
- added config setting to disabled using variants in the vanilla directories *(making only optifine and etf folders
  work)* this is specifically added for certain mods that have their mob textures named like "mob2.png" that are
  detected
  as random mobs by etf
- `Illegal path override = All` config setting will no longer allow empty paths
- added brazilian portuguese translations
- updated chinese translations
- fixed a crash when uploading skin changes on the forge version

[V4.3.1]

- updated russian translation to 4.3 thanks to @Felix14-v2
- fixed a button translation
- added compatibility warning message for Quark as it's [Variant Animal Textures] setting must be disabled for ETF's to
  work
- resolved an issue with additional textures like enderman_eyes.png or sheep_fur.png resetting after a short time if the
  base texture had .properties but the additional texture didn't
- added shulker bullet texture support

[V4.3.0]

*Update summary*

ETF 4.3 brings with it many fixes and stability issues as well as compatibility features and some long time missing
features.
Most notably ETF's skin features now have full compatibility with the `3D Skin Layers mod` and any `skin changing mod`
as well as fixing a notorious `Tinkers construct` crash on forge.

Added:

- added Minecart, Boat, Evoker Fang, and Llama carpet texture support
- added an option 'enabled by default' to set ETF to not tamper with any textures that have PBR textures attached, to
  preserve PBR functionality, only if shader mods are present
- added an option 'enabled by default' to set ETF to not tamper with any textures that has `moremcmeta mod` animations,
  to preserve animation functionality, only if `moremcmeta` is present

Changed:

- changed handling of block entity features, should clear up several issues and be very stable compared to before
- skin features rework:
    1. should now be compatible with most skin changer mods *e.g. `diguise-heads`, `impersonate`, or `fabric tailor`*
    2. much faster implementation
    3. updates the clients skin in game after uploading, so you don't need to restart *(doesn't update other players,
       that still requires a restart)*
- changed the `[Allow broken texture paths]` setting, it can now be set to `off`|`entities only`|`all resources`, *all
  resources is not recommended*
- made a few large source code refactors, no functionality change

Fixed:

- fixed `Tinkers construct` crash
- fixed `3D skin layers mod` compatibility with ETF skin features
- fixed skin changing mod compatibility *(e.g. `disguise-heads`, `impersonate`, `fabric tailor`)*
- fixed updatable properties for additional mob textures, like sheep_fur.png and villager types, not updating if the
  base texture doesn't
- fixed tooltip lines ignoring line breaks in 1.19.3
- fixed an issue with the `[Allow broken texture paths]` setting preventing resource packs with broken paths from
  loading
  correctly in 1.19.3
- fixed an issue with chest emissive textures

[V4.2.0]

*Update summary:*

ETF v4.2 has added many additional non OptiFine texture properties to further vary textures with, as well as adding
properties
that can vary the way the entity itself is rendered, such as brightness level, ambient particles and translucent
rendering.

4.2 also includes many bug fixes and suggestions that have popped up since I left on holidays.
I hope you enjoy :), and thank you for over 2 million downloads!!!

Added:

- added texture property `angry`, used like other OptiFine properties, can be `true|false`. works only for Endermen,
  Blazes, Guardians, Vindicators and Evokers. Triggers when these mobs display their client side 'angered or attacking
  state' *(open mouth for endermen & blazes igniting)*
- added texture property `moving`, used like other OptiFine properties, can be `true|false` to vary texture based on
  whether a mob is moving or not.
- added texture property `items`, used like other OptiFine properties, can be either:
    1. `none`*(true if mob is holding or wearing no items)*
    2. `any`*(true if mob is holding or wearing any items)*
    3. `holding`*(true if mob is holding any items)*
    4. `wearing`*(true if mob is wearing any items)*
    5. a list of item names like `minecraft:book` or `cool_mod:sunglasses`, separated by spaces.
- added a new property type "Entity Property" to tweak entity rendering within the .properties file, they are not
  numbered like the other OptiFine properties e.g `skins.1`
- added entity property `vanillaBrightnessOverride`: can be set as a number from `0-15`, this overrides the brightness
  of the mob, it can be used to reduce the brightness of mobs like Blazes and Allays, or increase the brightness of
  others.
- added entity property `suppressParticles`: if set to `true` will remove ambient particles from mobs *(currently only
  Blazes and Glow Squids)*
- added entity property `showHiddenModelParts`: if set to `true` will enable the rendering of model parts normally
  hidden in vanilla *(currently only zombie piglin right ears)*
- added entity property `entityRenderLayerOverride`, shader compatibility will vary, the possible values for this
  property are:
    1. `translucent` *(allows partial transparency in entity rendering)*
    2. `translucent_cull` *(allows partial transparency in entity rendering & culls model faces)*
    3. `end_portal` *(looks like the end portal effect, added for fun cause it works)*
    4. `outline` *(renders the entities outline through walls)*
- extended shader support code to include the Iris forge port `Oculus`, this should improve z-fighting and support the
  new compatible emissive render mode.
- added `Compatible` emissive rendering mode. This mode uses the `Bright` emissive rendering mode normally and
  automatically changes to, the more shader compatible, `Default / Dull` emissive rendering mode when shaders are
  enabled. To have the best of both worlds.
- added compatibility warning to disable skin features with the `impersonate` mod present
- added emissive and random texture support to saddles *(supports pigs, horse-mobs and striders)*
- added further support for some older OptiFine format biome names using `PascalCase` to be auto converted to the
  modern `snake_case` biome names *(e.g. `MushroomFields` becomes the correct `mushroom_fields` automatically)*

changed:

- updates russian translations thanks to @Felix14-v2
- improved block entity code, this should improve compat with armor stand affecting mods (like quark) as armor stands
  are no longer used as a substitute entity
- tweaked button scaling to center themselves in larger gui scales
- tweaked some gui button positions
- removed compatibility warning that disabled block entity features with the `quark` mod present, issue has been fixed.

fixed:
several additions listed above fix several issues however some more minor fixes include:

- fixed quark incompatibility with ETF block entity features
- fixed ETF settings button appearing on data pack selection screen
- fixed additional textures *(e.g. sheep fur or villager types)* having their variant overridden by the mobs base
  texture even if they have their own .properties file
- fixed compatibility with the disguised heads mod and skins with etf features not changing *(skin features will not
  display on disguised players :/)*
- fixed player skin enchanted visuals being brighter than vanilla
- fixed keyboard navigation in the ETF settings gui *(currently just `ESC` key to go back)*

[V4.1.1]

- added more screens to better separate button groups
- added an option in gui to disable resource-pack screen button
- tweaked the warning screen format
- tweaked many button positions in the GUI for consistency
- tweaked translations
- russian translation updated for the 4.1.0 GUI thanks to @Felix14-v2
- optimized texture sizes in jar thanks to @robotkoer
- fixed parrots not being random or emissive on player shoulders

[V4.1.0]

*Update summary:*

ETF 4.1 mostly brings the new skin feature tool, with a redesigned GUI to fit, as well as several
new texture properties to further customize your mob textures beyond what OptiFine offers

And of course many fixes, enjoy :)

- redesigned the settings GUI, cloth config is no longer required on Forge *(Openable from modmenu or a new button in
  the resource-pack screen)*
- added a GUI for selecting player features and saving or uploading a player feature ready skin with all your choices *(
  openable when in-game from the new settings GUI)*
- added new mob texture properties: *([Documentation Here.](readMeAssets/random_entities.properties))*
    1. "speed" texture property to vary textures by their top speed *(useful for horses, and all mobs)*
    2. "jumpStrength" texture property to vary textures by their jump strength *(useful for horses)*
    3. "maxHealth" texture property to vary textures by their max health *(useful for horses, and all mobs)*
    4. "llamaInventory" texture property to vary llama textures by their carry capacity
    5. "hiddenGene" texture property to vary panda textures by their hidden gene
    6. "playerCreated" texture property to vary iron golem textures by whether they were created by a player
    7. "screamingGoat" texture property to vary goat textures by whether they are the screaming variant
    8. "distanceFromPlayer" texture property to vary mob textures by their distance from the player
    9. "creeperCharged" texture property to vary creeper textures by whether the creeper has been charged by lightning
    10. "statusEffect" texture property to vary textures by status effect or Brown MooShroom suspicious stew variants
- added more crash prevention, most features should simply not work, and log an error, instead of immediately crashing
- added a new skin feature option "nose" available from the new skin tool GUI
- added an extra skin feature cape option (ETF) available from the new skin tool GUI
- added config option "hideConfigButton" to disable ETF config button in the resource-pack screen *(only available in
  the config.json file)*


- changed the iris pbr fix setting into the emissive fix settings screen with several options to reflect the more
  general fix
  possibilities *(fixes for animation mods, iris PBR and a few others)*
- updated russian translation thanks to @Felix14-v2
- tweaked the debug printout


- fixed an ArrayIndexOutOfBoundsException crash related to caches
- fixed an issue with additional textures *(drowned_outer_layer & sheep_fur & more)* not loading right when in the
  optifine or ETF folders
- fixed elytra_left.png not working and other asymmetrical elytra issues
- fixed forge using the */defaultconfigs/* folder instead of the */config/* folder
- fixed vanilla capes *(like migrator capes)* not using emissive and enchanted data from skin features
- fixed third party capes not loading correctly*(OptiFine and minecraftcapes.net)*
- fixed color property being broken for light_blue/blue & light_gray/gray colors
- fixed a crash when reading corrupt or broken config files


- and many more minor tweaks and fixes :)

[V4.0.2]

- fixed ArrayIndexOutOfBoundsException crash
- fixed excessive lag with shields and tridents in the rewrite
- added russian translation thanks to @Felix14-v2
- added more translation support where it was missing

[V4.0.1]

- fixed cape mod compatibility
- added quark compatibility warning that disables incompatible features

[V4.0]

*changes since last stable release*

ETF's source code has been almost entirely rewritten with a focus on optimization.

Some scenarios exhibit up to 11 times less processing time usage, and in general the mod is much more stable and
efficient

The source code has also been ported to a single codebase for forge and fabric using architechtury to streamline same
time updating of both.

Groundwork has been laid to more easily backport the mod to 1.16, 1.17 & 1.18 these will come later

- added legacy optifine biome name support e.g. "ForestHills" *(it is only mapped to current best fit, it is up to RP
  creators to keep their things updated)*
- added option to disable ETF texture patching to allow iris PBR to function *(this implementation may or may not be
  final)* *(expect possible z-fighting with etf emissive textures when using certain shaders)*
- added: additional mob textures like "sheep_fur.png" can now optionally utilize a "sheep_fur.properties" file to have
  settings different to the 'base' texture, if this properties file is not present ETF will try and use the same variant
  number as the 'base' texture the mob is using, failing all of these it will default to the regular vanilla texture for
  this variant
- added: config option "advanced_IncreaseCacheSizeModifier" which will only show in the config file, this should only be
  increased in the event of an extremely modded instance having over 2000 entities loaded on the client (the amount ETF
  now keeps track of at any given time), to prevent them being removed from ETF's memory.
- added: option to have a different texture on the left elytra wing using *"elytra_left.png"* *(compatible with
  CITResewn)*

- fixed blocks property not also checking the block spawned inside correctly
- fixed: issue with capes, and other skin features, having incorrect enchanted pixels
- fixed: armor and elytra emissives rendering behind textures, now works with iris pbr fix
- fixed: skin feature capes and issues preventing vanilla cape rendering in rewrite
- fixed: bed textures getting stuck to a co-ord position between different color beds
- fixed: shulker box textures getting stuck to a co-ord position between different color shulker boxes
- fixed: a minor issue with 2 frame blinking not registering the correct texture
- fixed: armor emissive bright mode bug
- fixed: shulker box emissive bright mode bug
- fixed: elytra emissive bright mode bug

- changed: custom potion effects have been removed as due to limits of client/server data transparency there is no
  reliable way to have it update during-game *(potion info is **only** sent to clients in the mobs first seen/spawn
  packet :/ )*
- changed: textures with property files that do not read correctly and cause problems will now fall back to the vanilla
  texture instead of randomly picking and causing confusion
- changed: block entity features will not render if the texture is animated as it is not supported *(no need to change
  settings)*

[V3.1.4]

- fixed a logic mistake causing high lag when additional mob textures *(like sheep_fur.png)* did not have the same or
  higher number of variations as the base texture
- the blocks property now also check the block the mob spawned inside *(allowing things like water, cave_air, flowers,
  torches, etc. to be used, and also fixes issues with soul-sand and mud not reading correctly)*
- added an option to enable / disable block entity emissive and custom textures as they currently do not support vanilla
  animated textures and may want to be disabled by such users
- added some missed translation support *(Still only english atm :/)*

[V3.1.3]

- sheep wool support added *(wool doesn't apply dye color to the emissive texture, use custom texture colour properties
  to set an altered coloured texture instead if required. A sheep using sheep2.png will try and use the equivalent
  wool2.png and will use the default otherwise )*
- fixed a crash due to an incorrect texture path for custom capes skin feature
- tweaked property file loading to be more consistent with resource-pack order *(using folders like "etf/random" & the
  vanilla locations will no longer accidentally override or be overridden by packs out of order)*
- iron golem cracking texture support added *(an iron_golem using texture2.png will try and use "crack_texture2.png" and
  will use the default if it doesn't exist)*

[V3.1.2]

- fixed a crash related to player skins that could occur when leaving a server

[V3.1.1]

- fixed other Mod integrations for 1.19 *(Mod menu, iris, etc)*
- elytra size fix now set disabled by default

[V3.1.0]

- updated to 1.19 *(for now 1.18.2 will remain supported with feature updates, but 1.18.1 & 1.18.0 will no longer be
  supported moving forwards, due to very annoying workarounds required)*
- support added for the wardens many texture layers

[V3.0.4]

- chest & shulker custom texture caching is more consistent
- texture cache data now also resets on game disconnect

[V3.0.3]

- fixed names property not working with chests (they will still not function on servers unless a mod is used to send
  blockEntity name data to clients)

[V3.0.2]

- fixed certain blocks not reading correctly for the Blocks texture property

[V3.0.1]

- fixed emissive armour & works with CIT Resewn

[V3.0.0]

Changed:

- the shader z-fighting fix no longer expands the model leading to weird rendering, emissives should no longer z-fight
  for most mobs while using shaders.(option has been removed)
- illegal identifier fix changed to only affect image files, cause of litematica conflicts
- Optifine parity: Name property now works identical to optifine
- ETF supports a new Names (plural) property using previous ETF name behaviour
- names property will also match against the whole line ((names.1=john smith) will check the names {"john", "smith" & "
  john smith"})

Added:

- Optifine parity: Size & Color texture properties
- Ender Dragon random / custom and emissive texture support
- drowned outer layers support random and emissive textures using the vanilla format
- choice between brighter or optifine-like(default) emissive rendering modes (bright usually provides bigger bloom with
  shaders and is noticeably brighter in sunlight)
- block entity emissive support for: chests, shulker boxes, beds, bells, enchanting table book & lectern book
- block entity random/custom texture support for: chests, shulker boxes & beds
- custom lectern book texture support with "minecraft/textures/entity/lectern_book.png"
- emissive shields
- debug logging config option to enable logging relevant ETF mob data when right-clicked
- custom horse armour & markings support, "horse2.png will try to use horse_armor2.png & horse_marking2.png"
- added function to ease access to skin features (a setting in the config/modmenu settings can be enabled to apply a
  skin feature guide image to a copy of your skin)
- supported emissive and enchanted capes to match skin when set in skin feature choices.
- parrots showing custom & emissive textures on shoulders
- warnings in mod menu config to inform of and prevent mod incompatibilities, as well as an option to ignore these
  warnings and override them
- translation support

Fixed:

- memory leak / usage issue in V2.6.0 - was related to entity glowing eyes code
- litematica compatibility
- blocks property not working for certain blocks with extra data
- villager profession property not working with namespaces
- Mooshroom mushrooms not being overridden correctly
- various minor issues
