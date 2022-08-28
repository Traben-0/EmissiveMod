# 💡Emissive Textures Guide:
<img src="emissives.png" alt="img" width="650"/>

---
### This mod is completely Optifine format compatible!!!!

- ETF supports all optifine Emissive Texture Resource-packs
- A custom emissive texture suffix may be set just like optifine but with a few optional changes,
  see [[ETF example] emissive.properties ](emissive.properties)
- please try and use only the default emissive suffix "_e" it makes things easier for everyone :)

---
## The Guide:

- Emissive textures allow parts of a mob / entity's texture to glow as in the above image

- An emissive texture is a copy of the regular mob texture with only the parts you want to be glowing
- to render correctly the emissive texture should be the exact same size as the original mob's texture, otherwise you
  may get z-fighting with shaders enabled
- Emissive textures must be in the same folder as the texture it is glowing over and must be named like (
  TextureName#.png)
  *(with "#" being the suffix set by the topmost optifine format resource-pack, it will otherwise default to "_e"
  meaning the file name should be (TextureName_e.png)*

- Elytra & Armour emissives have CIT Resewn mod support and will apply based on the CIT texture
- Tridents support emissive textures customized via the special case rules in the Random / Custom mob Guide

- Block entities like: Chests, shulker boxes, Beds, Bells, Enchanting table & Lectern books, all support emissive
  textures with ETF, other blocks will need the in-development Continuity mod's emissive blocks feature
- Enhanced Block Entities (A popular block entity lag fixing mod) will break ETF's support for: Chests, shulker boxes,
  Beds, Bells, Enchanting table & Lectern books.

- Player Skins support emissive textures, see [Player Skin Features Guide](SKINS.md)

### Emissive textures can render in two different ways set by the config or mod-menu options the two images below show the two rendering modes:

<table>
<tr>
<td>
<img src="emissiveDull.png" alt="img" width="350"/>
</td>
<td>

This image shows an example of the Default *"Dull"* rendering mode

- is like Optifine emissives
- are not overly bright in sunlight
- has directional light shading *(some sides are shaded differently)*
- has an upper brightness limit more inline with typical entity rendering
- Block entities will always use this mode unless iris is installed *(due to rendering issues in vanilla)*
- Is expected to be more stable with certain shaders

</td>
</tr>
</table>
<table>
<tr>
<td>
<img src="emissiveBright.png" alt="img" width="350"/>
</td>
<td>

This image shows an example of the optional *"Bright"* rendering mode

- is brighter than Optifine emissives
- are noticeably bright in sunlight and can look out of style with vanilla
- typically, has more bloom with shaders
- has global illumination and does not shade differently over the model
- brighter than Default *"Dull"* mode

</td>
</tr>
</table>



---

## Examples

<img src="format_example.png" alt="img" width="650"/>

- The example image above shows red glowing eyes for the texture *"zombie3.png"*
- Emissives are applied after randomised textures they must contain the same number system as the random files they
  apply too  
  *(e.g "zombie_e.png" will not apply to "zombie3.png" but "zombie3_e.png" will)*
