package traben.entity_texture_features.features.player;

import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.config.screens.skin.ETFConfigScreenSkinTool;
import traben.entity_texture_features.features.ETFManager;
import traben.entity_texture_features.features.texture_handlers.ETFTexture;
import traben.entity_texture_features.utils.ETFUtils2;
import com.mojang.blaze3d.platform.NativeImage;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
            #if MC > MC_21_2
import traben.entity_texture_features.ETFException;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.nio.file.Path;
            #else
import net.minecraft.client.renderer.texture.HttpTexture;
            #endif

@SuppressWarnings("EnhancedSwitchMigration")
public class ETFPlayerTexture {

    public static final String SKIN_NAMESPACE = "etf_skin";

    public static NativeImage clientPlayerOriginalSkinImageForTool = null;
    public static boolean remappingETFSkin = false;
    public ResourceLocation baseEnchantIdentifier = null;
    public ResourceLocation baseEnchantBlinkIdentifier = null;
    public ResourceLocation baseEnchantBlink2Identifier = null;
    //    public Identifier etfCapeIdentifier = null;
    public ResourceLocation texturedNoseIdentifier = null;
    public ResourceLocation texturedNoseIdentifierEmissive = null;
    public ResourceLocation texturedNoseIdentifierEnchanted = null;
    public boolean hasVillagerNose = false;
    public boolean hasFeatures = false;
    public int coatStyle = 0;
    public int coatLength = 1;
    public int blinkType = 0;
    //public boolean THIS_SKIN_IS_IN_EDITOR = false;
    public int blinkHeight = 1;
    //    public ETFTexture etfCape = null;
    public boolean hasEmissives = false;
    public boolean hasEnchant = false;
    //provides emissive patching and blinking functionality
    //all ETFPlayerTexture needs to do is build those textures and register them before this ETFTexture is made, and it will auto locate and apply them
    public ETFTexture etfTextureOfFinalBaseSkin;
    //    public ETFConfigScreenSkinTool.CapeType capeType = ETFConfigScreenSkinTool.CapeType.NONE;
    public ETFConfigScreenSkinTool.NoseType noseType = ETFConfigScreenSkinTool.NoseType.NONE;
    public ETFPlayerEntity player;
    public boolean wasForcedSolid = false;
    //private boolean allowThisETFBaseSkin = true;
    ResourceLocation coatIdentifier = null;
    ResourceLocation coatEmissiveIdentifier = null;
    ResourceLocation coatEnchantedIdentifier = null;
    boolean hasFatCoat = false;
    private boolean isTextureReady = false;
    //private boolean hasVanillaCape = false;
    private NativeImage originalSkin;
    //    private NativeImage originalCape;
//    private int[] enchantCapeBounds = null;
//    private int[] emissiveCapeBounds = null;
//    private Identifier etfCapeEmissiveIdentifier = null;
//    private Identifier etfCapeEnchantedIdentifier = null;
    private ResourceLocation normalVanillaSkinIdentifier = null;

    public ETFPlayerTexture(ETFPlayerEntity player, ResourceLocation rendererGivenSkin) {
        //initiate texture download as we need unprocessed texture from the skin server
        this.player = player;
        this.normalVanillaSkinIdentifier = rendererGivenSkin;
        //triggerSkinDownload();
        if (player instanceof Player) {
            //normal player entity
            checkTexture(false);
        } else {
            //create a player texture for a player head block
            //this can have a historic skin and thus should not be driven by the actual players uuid
            //use the historic skin resource and ensure we do not bother to load a current skin for the player
            try {

                #if MC < MC_21_4
                HttpTexture skin =
                #if MC > MC_20_1
                        (HttpTexture) Minecraft.getInstance().getSkinManager().skinTextures.textureManager.getTexture(rendererGivenSkin, null);
                #else
                        (HttpTexture) Minecraft.getInstance().getSkinManager().textureManager.getTexture(rendererGivenSkin, null);
                #endif
                assert skin.file != null;
                FileInputStream fileInputStream = new FileInputStream(skin.file);
                NativeImage vanilla = NativeImage.read(fileInputStream);
                //System.out.println((vanilla != null) +" skin");
                fileInputStream.close();
                originalSkin = ETFUtils2.emptyNativeImage(64, 64);
                originalSkin.copyFrom(vanilla);
                vanilla.close();
                #else

                NativeImage vanilla = getSkinOfPlayer(player, rendererGivenSkin);

                originalSkin = ETFUtils2.emptyNativeImage(64, 64);
                originalSkin.copyFrom(vanilla);
                vanilla.close();
                #endif

                //originalSkin = ETFUtils2.getNativeImageElseNull(rendererGivenSkin);
                checkTexture(true);
            } catch (Exception e) {
                //e.printStackTrace();
                skinFailed("player head block failure");
            }
        }
    }

    #if MC > MC_21_2
    //mostly a copy of new skin downloading code, except we want to grab the nativeImage of the skin itself without any edits
    public static @NotNull NativeImage getSkinOfPlayer(final ETFPlayerEntity clientPlayer, @Nullable ResourceLocation rendererGivenSkin) throws IOException {
        //todo renderer given skin will need to be reconsidered how to use in 1.21.4+

        String url;
        GameProfile gameProfile;
        if(clientPlayer instanceof AbstractClientPlayer abstractClientPlayer){
            url = abstractClientPlayer.getSkin().textureUrl();
            gameProfile = abstractClientPlayer.getGameProfile();
        }else if (clientPlayer instanceof SkullBlockEntity skull && skull.getOwnerProfile() != null) {
            gameProfile = skull.getOwnerProfile().gameProfile();
            url = Minecraft.getInstance().getSkinManager().getInsecureSkin(gameProfile).textureUrl();
        } else {
            throw new ETFException("Invalid player type to get skin for ETF: " + clientPlayer.getClass());
        }

        var minecraftProfileTexture = Minecraft.getInstance().getSkinManager().sessionService.getTextures(gameProfile).skin();
        if (minecraftProfileTexture == null) throw new ETFException("No profile texture found for player: " + gameProfile.getName());

        String string = Hashing.sha1().hashUnencodedChars(minecraftProfileTexture.getHash()).toString();

        try {
            Path path = Minecraft.getInstance().getSkinManager().skinTextures.
                        root.resolve(string.length() > 2 ? string.substring(0, 2) : "xx").resolve(string);

            return SkinTextureDownloader.downloadSkin(path, url);
        } catch (IOException e) {
            if (rendererGivenSkin != null) {
                var texture = Minecraft.getInstance().getTextureManager().getTexture(rendererGivenSkin);
                if(texture instanceof DynamicTexture dynamicTexture){
                    return Objects.requireNonNull(dynamicTexture.getPixels());
                }
            }
            throw e;
        }
    }
    #endif

    //THIS REPRESENTS A NON FEATURED SKIN
    // must still create an object as the identifier is important to detect skin changes from other mods
    private ETFPlayerTexture(ResourceLocation rendererGivenSkin) {
        this.player = null;
        this.normalVanillaSkinIdentifier = rendererGivenSkin;
    }

    public ETFPlayerTexture() {
        //THIS_SKIN_IS_IN_EDITOR = true;
        //exists only for tool
        this.player = (ETFPlayerEntity) Minecraft.getInstance().player;
        assert player != null;
        if (player.etf$getEntity() != null) {
            assert Minecraft.getInstance().player != null;
            NativeImage skin =
            #if MC > MC_20_1
                    ETFUtils2.getNativeImageElseNull(Minecraft.getInstance().player.getSkin().texture());
            #else
                    ETFUtils2.getNativeImageElseNull(Minecraft.getInstance().player.getSkinTextureLocation());
            #endif
            if (skin != null) {
                clientPlayerOriginalSkinImageForTool = skin;
                changeSkinToThisForTool(skin);
                return;
            }
        }

        ETFUtils2.logError("ETFPlayerTexture went wrong");
    }

    @Nullable
    private static NativeImage returnMatchPixels(NativeImage baseSkin, int[] boundsToCheck) {
        return returnMatchPixels(baseSkin, boundsToCheck, null);
    }

    // returns a native image with only pixels that match those contained in the boundsToCheck region of baseSkin
    // if second is not null it will return an altered version of that instead of baseSkin
    // will also return null if there is nothing to check or no matching pixels
    @Nullable
    private static NativeImage returnMatchPixels(NativeImage baseSkin, int[] boundsToCheck, @Nullable NativeImage second) {
        if (baseSkin == null || boundsToCheck == null) return null;

        boolean hasSecondImageToBeUsedAsBase = second != null;
        Set<Integer> matchColors = new HashSet<>();
        for (int x = boundsToCheck[0]; x <= boundsToCheck[2]; x++) {
            for (int y = boundsToCheck[1]; y <= boundsToCheck[3]; y++) {
                if (baseSkin.getLuminanceOrAlpha(x, y) != 0) {
                    matchColors.add(ETFUtils2.getPixel(baseSkin, x, y));
                }
            }
        }
        if (matchColors.isEmpty()) {
            return null;
        } else {
            NativeImage texture;
            if (!hasSecondImageToBeUsedAsBase) {
                texture = new NativeImage(baseSkin.getWidth(), baseSkin.getHeight(), false);
                texture.copyFrom(baseSkin);
            } else {
                texture = new NativeImage(second.getWidth(), second.getHeight(), false);
                texture.copyFrom(second);
            }
            for (int x = 0; x < texture.getWidth(); x++) {
                for (int y = 0; y < texture.getHeight(); y++) {
                    if (!matchColors.contains(ETFUtils2.getPixel(texture, x, y))) {
                         ETFUtils2.setPixel(texture,x, y, 0);
                    }
                }
            }
            return returnNullIfEmptyImage(texture);
        }

    }

//    private static NativeImage returnCustomTexturedCape(NativeImage skin) {
//        NativeImage cape = ETFUtils2.emptyNativeImage(64, 32);
//        NativeImage elytra = ETFUtils2.getNativeImageElseNull(new Identifier("textures/entity/elytra.png"));
//        if (elytra == null || elytra.getWidth() != 64 || elytra.getHeight() != 32) {
//            elytra = ETFUtils2.getNativeImageElseNull(new Identifier(MOD_ID, "textures/capes/default_elytra.png"));
//        }//not else
//        if (elytra != null) {
//            cape.copyFrom(elytra);
//        }
//        copyToPixels(skin, cape, getSkinPixelBounds("cape1"), 1, 1);
//        copyToPixels(skin, cape, getSkinPixelBounds("cape1"), 12, 1);
//        copyToPixels(skin, cape, getSkinPixelBounds("cape2"), 1, 5);
//        copyToPixels(skin, cape, getSkinPixelBounds("cape2"), 12, 5);
//        copyToPixels(skin, cape, getSkinPixelBounds("cape3"), 1, 9);
//        copyToPixels(skin, cape, getSkinPixelBounds("cape3"), 12, 9);
//        copyToPixels(skin, cape, getSkinPixelBounds("cape4"), 1, 13);
//        copyToPixels(skin, cape, getSkinPixelBounds("cape4"), 12, 13);
//        copyToPixels(skin, cape, getSkinPixelBounds("cape5.1"), 9, 1);
//        copyToPixels(skin, cape, getSkinPixelBounds("cape5.1"), 20, 1);
//        copyToPixels(skin, cape, getSkinPixelBounds("cape5.2"), 9, 5);
//        copyToPixels(skin, cape, getSkinPixelBounds("cape5.2"), 20, 5);
//        copyToPixels(skin, cape, getSkinPixelBounds("cape5.3"), 9, 9);
//        copyToPixels(skin, cape, getSkinPixelBounds("cape5.3"), 20, 9);
//        copyToPixels(skin, cape, getSkinPixelBounds("cape5.4"), 9, 13);
//        copyToPixels(skin, cape, getSkinPixelBounds("cape5.4"), 20, 13);
//
//        copyToPixels(cape, cape, getSkinPixelBounds("capeVertL"), 0, 1);
//        copyToPixels(cape, cape, getSkinPixelBounds("capeVertR"), 11, 1);
//        copyToPixels(cape, cape, getSkinPixelBounds("capeHorizL"), 1, 0);
//        copyToPixels(cape, cape, getSkinPixelBounds("capeHorizR"), 11, 0);
//
//        return cape;
//    }

    @Nullable
    private static NativeImage returnNullIfEmptyImage(NativeImage imageToCheck) {
        boolean foundAPixel = false;
        upper:
        for (int x = 0; x < imageToCheck.getWidth(); x++) {
            for (int y = 0; y < imageToCheck.getHeight(); y++) {
                if (ETFUtils2.getPixel(imageToCheck, x, y) != 0) {
                    foundAPixel = true;
                    break upper;
                }
            }
        }
        return foundAPixel ? imageToCheck : null;
    }

    private static int[] getSkinPixelBounds(String choiceKey) {
        return switch (choiceKey) {
            case "marker1" -> new int[]{56, 16, 63, 23};
            case "marker2" -> new int[]{56, 24, 63, 31};
            case "marker3" -> new int[]{56, 32, 63, 39};
            case "marker4" -> new int[]{56, 40, 63, 47};
            case "optimizedEyeSmall" -> new int[]{12, 16, 19, 16};
            case "optimizedEye2High" -> new int[]{12, 16, 19, 17};
            case "optimizedEye2High_second" -> new int[]{12, 18, 19, 19};
            case "optimizedEye4High" -> new int[]{12, 16, 19, 19};
            case "optimizedEye4High_second" -> new int[]{36, 16, 43, 19};
            case "face1" -> new int[]{0, 0, 7, 7};
            case "face2" -> new int[]{24, 0, 31, 7};
            case "face3" -> new int[]{32, 0, 39, 7};
            case "face4" -> new int[]{56, 0, 63, 7};
            case "cape1" -> new int[]{12, 32, 19, 35};
            case "cape2" -> new int[]{36, 32, 43, 35};
            case "cape3" -> new int[]{12, 48, 19, 51};
            case "cape4" -> new int[]{28, 48, 35, 51};
            case "cape5" -> new int[]{44, 48, 51, 51};
            case "cape5.1" -> new int[]{44, 48, 45, 51};
            case "cape5.2" -> new int[]{46, 48, 47, 51};
            case "cape5.3" -> new int[]{48, 48, 49, 51};
            case "cape5.4" -> new int[]{50, 48, 51, 51};
            case "capeVertL" -> new int[]{1, 1, 1, 16};
            case "capeVertR" -> new int[]{10, 1, 10, 16};
            case "capeHorizL" -> new int[]{1, 1, 10, 1};
            case "capeHorizR" -> new int[]{1, 16, 10, 16};
            default -> new int[]{0, 0, 0, 0};
        };
    }

    private static NativeImage returnOptimizedBlinkFace(NativeImage baseSkin, int[] eyeBounds, int eyeHeightFromTopDown) {
        return returnOptimizedBlinkFace(baseSkin, eyeBounds, eyeHeightFromTopDown, null);
    }

//    private static int countTransparentInBox(NativeImage img, int x1, int y1, int x2, int y2) {
//        int counter = 0;
//        for (int x = x1; x <= x2; x++) {
//            for (int y = y1; y <= y2; y++) {
//                //ranges from  0 to 127  then wraps around negatively -127 to -1  totalling 0 to 255
//                int i = img.getOpacity(x, y);
//                if (i < 0) {
//                    i += 256;
//                }
//                //adjusted to 0 to 256
//                counter += i;
//
//            }
//        }
//        return counter;
//    }

//    private static void setNotTransparentInBox(NativeImage img, int x1, int y1, int x2, int y2) {
//        for (int x = x1; x <= x2; x++) {
//            for (int y = y1; y <= y2; y++) {
//                //ranges from  0 to 127  then wraps around negatively -127 to -1  totalling 0 to 255
//                if (img.getOpacity(x, y) != -1) {
//                    int col = img.getColor(x, y);
//                    //set colour to not be transparent
//                    img.setColor(x, y, ColorHelper.Argb.getArgb(
//                            -1,
//                            ColorHelper.Argb.getRed(col),
//                            ColorHelper.Argb.getGreen(col),
//                            ColorHelper.Argb.getBlue(col)
//                    ));
//                }
//            }
//        }
//    }

//    private static void parseSkinTransparency(NativeImage skin, boolean forceSolidSkin) {
//        if (forceSolidSkin || !ETF.config().getConfig().skinFeaturesEnableTransparency) {
//            forceSolidLowerSkin(skin);
//            return;
//        }
//        if (!ETF.config().getConfig().skinFeaturesEnableFullTransparency) {
//            int countTransparent = 0;
//            //map of bottom skin layer
//            countTransparent += countTransparentInBox(skin, 8, 0, 23, 15);
//            countTransparent += countTransparentInBox(skin, 0, 20, 55, 31);
//            countTransparent += countTransparentInBox(skin, 0, 8, 7, 15);
//            countTransparent += countTransparentInBox(skin, 24, 8, 31, 15);
//            countTransparent += countTransparentInBox(skin, 0, 16, 11, 19);
//            countTransparent += countTransparentInBox(skin, 20, 16, 35, 19);
//            countTransparent += countTransparentInBox(skin, 44, 16, 51, 19);
//            countTransparent += countTransparentInBox(skin, 20, 48, 27, 51);
//            countTransparent += countTransparentInBox(skin, 36, 48, 43, 51);
//            countTransparent += countTransparentInBox(skin, 16, 52, 47, 63);
//            //do not allow skins under 40% ish total opacity
//            //1648 is total pixels that are not allowed transparent by vanilla
//            int average = (countTransparent / 1648); // should be 0 to 256
//            //System.out.println("average ="+average);
//            boolean isSkinMoreThan40PercentOpaque = average >= 100;
//            if (!isSkinMoreThan40PercentOpaque) {
//                forceSolidLowerSkin(skin);
//            }
//        }
//    }

    private static NativeImage returnOptimizedBlinkFace(NativeImage baseSkin, int[] eyeBounds, int eyeHeightFromTopDown, int[] secondLayerBounds) {
        NativeImage texture = new NativeImage(64, 64, false);
        texture.copyFrom(baseSkin);
        //copy face
        copyToPixels(baseSkin, texture, eyeBounds, 8, 8 + (eyeHeightFromTopDown - 1));
        //copy face overlay
        if (secondLayerBounds != null) {
            copyToPixels(baseSkin, texture, secondLayerBounds, 40, 8 + (eyeHeightFromTopDown - 1));
        }
        return texture;
    }
//    private static boolean isSkinNotTooTransparent(NativeImage skin) {
//        if (ETFConfig.getInstance().skinFeaturesEnableFullTransparency) {
//            return true;
//        } else {
//            int countTransparent = 0;
//            //map of bottom skin layer
//            countTransparent += countTransparentInBox(skin, 8, 0, 23, 15);
//            countTransparent += countTransparentInBox(skin, 0, 20, 55, 31);
//            countTransparent += countTransparentInBox(skin, 0, 8, 7, 15);
//            countTransparent += countTransparentInBox(skin, 24, 8, 31, 15);
//            countTransparent += countTransparentInBox(skin, 0, 16, 11, 19);
//            countTransparent += countTransparentInBox(skin, 20, 16, 35, 19);
//            countTransparent += countTransparentInBox(skin, 44, 16, 51, 19);
//            countTransparent += countTransparentInBox(skin, 20, 48, 27, 51);
//            countTransparent += countTransparentInBox(skin, 36, 48, 43, 51);
//            countTransparent += countTransparentInBox(skin, 16, 52, 47, 63);
//            //do not allow skins under 40% ish total opacity
//            //1648 is total pixels that are not allowed transparent by vanilla
//            int average = (countTransparent / 1648); // should be 0 to 256
//            //System.out.println("average ="+average);
//            return average >= 100;
//        }
//    }

    private static void forceSolidLowerSkin(NativeImage skin) {
        try {
            stripAlphaInclusive(skin, 8, 0, 23, 15);
            stripAlphaInclusive(skin, 0, 20, 55, 31);
            stripAlphaInclusive(skin, 0, 8, 7, 15);
            stripAlphaInclusive(skin, 24, 8, 31, 15);
            stripAlphaInclusive(skin, 0, 16, 11, 19);
            stripAlphaInclusive(skin, 20, 16, 35, 19);
            stripAlphaInclusive(skin, 44, 16, 51, 19);
            stripAlphaInclusive(skin, 20, 48, 27, 51);
            stripAlphaInclusive(skin, 36, 48, 43, 51);
            stripAlphaInclusive(skin, 16, 52, 47, 63);
        } catch (Exception ignored) {
        }
    }

    private static NativeImage getCoatTexture(NativeImage skin, int lengthOfCoat, boolean ignoreTopTexture) {

        NativeImage coat = new NativeImage(64, 64, false);
        coat.fillRect(0, 0, 64, 64, 0);

        //top
        if (!ignoreTopTexture) {
            copyToPixels(skin, coat, 4, 32, 7, 35 + lengthOfCoat, 20, 32);
            copyToPixels(skin, coat, 4, 48, 7, 51 + lengthOfCoat, 24, 32);
        }
        //sides
        copyToPixels(skin, coat, 0, 36, 7, 36 + lengthOfCoat, 16, 36);
        copyToPixels(skin, coat, 12, 36, 15, 36 + lengthOfCoat, 36, 36);
        copyToPixels(skin, coat, 4, 52, 15, 52 + lengthOfCoat, 24, 36);
//        //ENCHANT AND EMISSIVES
//        copyToPixels(skin, coat, 56, 16, 63, 47, 0, 0);
        return coat;

    }

    private static void copyToPixels(NativeImage source, NativeImage dest, int[] bounds, int copyToX, int CopyToY) {
        copyToPixels(source, dest, bounds[0], bounds[1], bounds[2], bounds[3], copyToX, CopyToY);
    }

    private static void copyToPixels(NativeImage source, NativeImage dest, int x1, int y1, int x2, int y2, int copyToX, int copyToY) {
        int copyToXRelative = copyToX - x1;
        int copyToYRelative = copyToY - y1;
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                ETFUtils2.setPixel(dest, x + copyToXRelative, y + copyToYRelative, ETFUtils2.getPixel(source, x, y));
            }
        }
    }

    private static void deletePixels(NativeImage source, int x1, int y1, int x2, int y2) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                ETFUtils2.setPixel(source, x, y, 0);
            }
        }
    }

    public static int getSkinPixelColourToNumber(int color) {
        //            pink   cyan     red       green      brown    blue     orange     yellow
        //colours = -65281, -256, -16776961, -16711936, -16760705, -65536, -16744449, -14483457
        return switch (color) {
            case -65281 -> 1;
            case -256 -> 2;
            case -16776961 -> 3;
            case -16711936 -> 4;
            case -16760705 -> 5;
            case -65536 -> 6;
            case -16744449 -> 7;
            case -14483457 -> 8;
            case -12362096 -> 666; //villager nose color
            default -> color;
        };
    }

    public static int getSkinNumberToPixelColour(int color) {
        //            pink   cyan     red       green      brown    blue     orange     yellow
        //colours = -65281, -256, -16776961, -16711936, -16760705, -65536, -16744449, -14483457
        return switch (color) {
            case 1 -> -65281;
            case 2 -> -256;
            case 3 -> -16776961;
            case 4 -> -16711936;
            case 5 -> -16760705;
            case 6 -> -65536;
            case 7 -> -16744449;
            case 8 -> -14483457;
            case 666 -> -12362096; //villager nose color
            default -> color;
        };
    }

//    public boolean hasCustomCape() {
//        return etfCape != null;
//    }

    private static void stripAlphaInclusive(NativeImage image, int x1, int y1, int x2, int y2) {
        for (int i = x1; i <= x2; ++i) {
            for (int j = y1; j <= y2; ++j) {
                ETFUtils2.setPixel(image, i, j, ETFUtils2.getPixel(image, i, j) | -16777216);
            }
        }

    }

    public boolean isCorrectObjectForThisSkin(ResourceLocation check) {
        return check.equals(normalVanillaSkinIdentifier);
    }

    @Nullable
    public ResourceLocation getBaseTextureIdentifierOrNullForVanilla(Player player) {
        return getBaseTextureIdentifierOrNullForVanilla((ETFPlayerEntity) player);
    }

    @Nullable
    public ResourceLocation getBaseTextureIdentifierOrNullForVanilla(ETFPlayerEntity player) {
        this.player = player;//refresh player data
        if (etfTextureOfFinalBaseSkin != null && (canUseFeaturesForThisPlayer())) {
            return etfTextureOfFinalBaseSkin.getTextureIdentifier(player);
        }
        return null;
    }

//    @Nullable
//    public Identifier getBaseTextureEnchantIdentifierOrNullForNone() {
//        if (hasEnchant && canUseFeaturesForThisPlayer() && etfTextureOfFinalBaseSkin != null) {
//            switch (etfTextureOfFinalBaseSkin.currentTextureState) {
//                case NORMAL, NORMAL_PATCHED:
//                    return baseEnchantIdentifier;
//                case BLINK, BLINK_PATCHED:
//                    return baseEnchantBlinkIdentifier;
//                case BLINK2, BLINK2_PATCHED:
//                    return baseEnchantBlink2Identifier;
//                default:
//                    return null;
//            }
//        }
//        return null;
//    }

//    public void renderFeatures(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, PlayerEntityModel<AbstractClientPlayerEntity> model) {
//        if (canUseFeaturesForThisPlayer()) {
//            if (etfCapeIdentifier != null) {
//                // VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(etfCapeIdentifier));
//                // model.renderCape(matrixStack, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
//
//                if (etfCapeEmissiveIdentifier != null) {
//                    VertexConsumer emissiveVert = vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(etfCapeEmissiveIdentifier));
//                    model.renderCape(matrixStack, emissiveVert, ETFClientCommon.EMISSIVE_FEATURE_LIGHT_VALUE, OverlayTexture.DEFAULT_UV);
//                }
//                if (etfCapeEnchantedIdentifier != null) {
//                    VertexConsumer enchantVert = ItemRenderer.getArmorGlintConsumer(vertexConsumerProvider, RenderLayer.getArmorCutoutNoCull(etfCapeEnchantedIdentifier), false, true);
//                    model.renderCape(matrixStack, enchantVert, light, OverlayTexture.DEFAULT_UV);
//                }
//
//            }
//        }
//    }

    @Nullable
    public ResourceLocation getBaseHeadTextureIdentifierOrNullForVanilla() {
        if (etfTextureOfFinalBaseSkin != null && (canUseFeaturesForThisPlayer())) {
            return etfTextureOfFinalBaseSkin.getTextureIdentifier(null);
        }
        return null;
    }


//    private void initiateThirdPartyCapeDownload(String capeUrl) {
//        CompletableFuture.runAsync(() -> {
//            HttpURLConnection httpURLConnection;
//            try {
//                httpURLConnection = (HttpURLConnection) (new URL(capeUrl)).openConnection(MinecraftClient.getInstance().getNetworkProxy());
//                httpURLConnection.setDoInput(true);
//                httpURLConnection.setDoOutput(false);
//                httpURLConnection.connect();
//                if (httpURLConnection.getResponseCode() / 100 == 2) {
//                    InputStream inputStream = httpURLConnection.getInputStream();
//
//                    MinecraftClient.getInstance().execute(() -> {
//                        try {
//                            NativeImage one = NativeImage.read(inputStream);
//                            this.receiveThirdPartyCape(one);
//                        } catch (Exception e) {
//                            ETFUtils2.logError("ThirdPartyCapeDownload failed for player:" + player.etf$getName().getString() + "retrying again later " + e);
//                            e.printStackTrace();
//                            //this.skinFailed(false);
//                        }
//                    });
//                }
//            } catch (Exception var6) {
//                ETFUtils2.logError("ThirdPartyCapeDownload2 failed for player:" + player.etf$getName().getString() + "retrying again later" + var6);
//                //this.skinFailed(false);
//            }
//        }, Util.getMainWorkerExecutor());
//    }

//    public void receiveThirdPartyCape(@NotNull NativeImage capeImage) {
//        //optifine resizes them for space cause expensive servers I guess
//
//        Identifier newCapeId = new Identifier(SKIN_NAMESPACE, player.etf$getUuid().toString().replaceAll("/[^a-z]/g", "") + "_cape_third_party.png");
//        //boolean changed = false;
//        NativeImage resizedImage;
//
//        if (capeImage.getWidth() % capeImage.getHeight() != 0) {
//            //resize optifine image
//            int newWidth = 64;
//            while (newWidth < capeImage.getWidth()) {
//                newWidth = newWidth + newWidth;
//            }
//            int newHeight = newWidth / 2;
//            try {
//                resizedImage = ETFUtils2.emptyNativeImage(newWidth, newHeight);
//                for (int x = 0; x < capeImage.getWidth(); x++) {
//                    for (int y = 0; y < capeImage.getHeight(); y++) {
//                        resizedImage.setColor(x, y, capeImage.getColor(x, y));
//                    }
//                }
//                //ETFUtils2.registerNativeImageToIdentifier(resizedImage, newCapeId);
//                //capeImage = resizedImage;
//                //changed = true;
//                checkThirdPartyCapeFeaturesAndFinalize(resizedImage, newCapeId);
//            } catch (Exception e) {
//                ETFUtils2.logError("optifine cape resize failed");
//            }
//        } else {
//            checkThirdPartyCapeFeaturesAndFinalize(capeImage, newCapeId);
//        }
//    }

//    private void checkThirdPartyCapeFeaturesAndFinalize(NativeImage capeImage, Identifier etfCapeIdentifier) {
//
//        Identifier etfCapeEmissiveIdentifier = null;
//        Identifier etfCapeEnchantedIdentifier = null;
//
//        NativeImage checkCapeEmissive = returnMatchPixels(originalSkin, emissiveCapeBounds, capeImage);
//        //UUID_PLAYER_HAS_EMISSIVE_CAPE.put(id, checkCape != null);
//        if (checkCapeEmissive != null) {
//            Identifier newCapeEmissive = new Identifier(SKIN_NAMESPACE, player.etf$getUuid() + "_cape_third_party_e.png");
//            ETFUtils2.registerNativeImageToIdentifier(checkCapeEmissive, newCapeEmissive);
//            etfCapeEmissiveIdentifier = newCapeEmissive;
//        }
//        NativeImage checkCapeEnchant = returnMatchPixels(originalSkin, enchantCapeBounds, capeImage);
//        //UUID_PLAYER_HAS_EMISSIVE_CAPE.put(id, checkCape != null);
//        if (checkCapeEnchant != null) {
//            Identifier newCapeEnchanted = new Identifier(SKIN_NAMESPACE, player.etf$getUuid() + "_cape_third_party_enchant.png");
//            ETFUtils2.registerNativeImageToIdentifier(checkCapeEnchant, newCapeEnchanted);
//            etfCapeEnchantedIdentifier = newCapeEnchanted;
//        }
//        ETFUtils2.registerNativeImageToIdentifier(capeImage, etfCapeIdentifier);
////        etfCape = new ETFTexture(etfCapeIdentifier,null,null,
////                etfCapeEmissiveIdentifier,null,null,
////                etfCapeEnchantedIdentifier,null,null,
////                null,null,null);
//    }

    @Nullable
    public ResourceLocation getBaseTextureEmissiveIdentifierOrNullForNone() {
        if (hasEmissives && canUseFeaturesForThisPlayer() && etfTextureOfFinalBaseSkin != null) {
            return etfTextureOfFinalBaseSkin.getEmissiveIdentifierOfCurrentState();
        }
        return null;
    }

    public boolean canUseFeaturesForThisPlayer() {
        return isTextureReady
                && hasFeatures
                && (//not on enemy team or doesn't matter
                ETF.config().getConfig().enableEnemyTeamPlayersSkinFeatures
                        || (player.etf$isTeammate(Minecraft.getInstance().player)
                        || player.etf$getScoreboardTeam() == null));
    }

    private void skinFailed(String reason) {
        if (!(Minecraft.getInstance().screen instanceof ETFConfigScreenSkinTool)) {
            ETFManager.getInstance().PLAYER_TEXTURE_MAP.put(player.etf$getUuid(), new ETFPlayerTexture(normalVanillaSkinIdentifier));
        } else {
            ETFUtils2.logError("something went wrong applying skin in tool, or skin features are not added: "+ reason);
        }

    }

    public void checkTexture(boolean skipSkinLoad) {
        if (!skipSkinLoad) {
            try {
                #if MC < MC_21_4
                HttpTexture skin =
                #if MC > MC_20_1
                        (HttpTexture) Minecraft.getInstance().getSkinManager().skinTextures.textureManager.getTexture(normalVanillaSkinIdentifier, null);
                #else
                        (HttpTexture) Minecraft.getInstance().getSkinManager().textureManager.getTexture(normalVanillaSkinIdentifier, null);
                #endif
                assert skin.file != null;
                FileInputStream fileInputStream = new FileInputStream(skin.file);

                NativeImage img = NativeImage.read(fileInputStream);
                remappingETFSkin = true;
                originalSkin = skin.processLegacySkin(img);
                remappingETFSkin = false;
                fileInputStream.close();
                #else

                NativeImage img = getSkinOfPlayer(player, null);
                remappingETFSkin = true;
                originalSkin = SkinTextureDownloader.processLegacySkin(img, "ETF pre test, skin check");
                remappingETFSkin = false;
                #endif

                if (Minecraft.getInstance().player != null && player.etf$getUuid().equals(Minecraft.getInstance().player.getUUID())) {
                    clientPlayerOriginalSkinImageForTool = originalSkin;
                }

            } catch (Exception e) {
                skinFailed("skin pre load failure: "+ e.getMessage());
                return;
            }
        }

        UUID id = player.etf$getUuid();

        NativeImage modifiedSkin = ETFUtils2.emptyNativeImage(originalSkin.getWidth(), originalSkin.getHeight());
        modifiedSkin.copyFrom(originalSkin);

        if (originalSkin != null) {
            if (ETFUtils2.getPixel(originalSkin, 1, 16) == -16776961 &&
                    ETFUtils2.getPixel(originalSkin, 0, 16) == -16777089 &&
                    ETFUtils2.getPixel(originalSkin, 0, 17) == -16776961 &&
                    ETFUtils2.getPixel(originalSkin, 2, 16) == -16711936 &&
                    ETFUtils2.getPixel(originalSkin, 3, 16) == -16744704 &&
                    ETFUtils2.getPixel(originalSkin, 3, 17) == -16711936 &&
                    ETFUtils2.getPixel(originalSkin, 0, 18) == -65536 &&
                    ETFUtils2.getPixel(originalSkin, 0, 19) == -8454144 &&
                    ETFUtils2.getPixel(originalSkin, 1, 19) == -65536 &&
                    ETFUtils2.getPixel(originalSkin, 3, 18) == -1 &&
                    ETFUtils2.getPixel(originalSkin, 2, 19) == -1 &&
                    ETFUtils2.getPixel(originalSkin, 3, 18) == -1
            ) {


                hasFeatures = true;
                ETFUtils2.logMessage("Found Player {" + player.etf$getName().getString() + "} with ETF texture features in skin.", false);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                //locate and convert choices to ints
                int[] choiceBoxChoices = {
                        getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 52, 16)),//blink
                        getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 52, 17)),//jacket choice
                        getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 52, 18)),//jacket height
                        getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 52, 19)),//blink height
                        getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 53, 16)),//cape choice
                        getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 53, 17)),//nose choice
                        getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 53, 18)),//no transparency choice
//                        getSkinPixelColourToNumber(originalSkin.getColor(53, 18)),
//                        getSkinPixelColourToNumber(originalSkin.getColor(53, 19)),
//                        getSkinPixelColourToNumber(originalSkin.getColor(54, 16)),
//                        getSkinPixelColourToNumber(originalSkin.getColor(54, 17)),
//                        getSkinPixelColourToNumber(originalSkin.getColor(54, 18)),
//                        getSkinPixelColourToNumber(originalSkin.getColor(54, 19)),
//                        getSkinPixelColourToNumber(originalSkin.getColor(55, 16)),
//                        getSkinPixelColourToNumber(originalSkin.getColor(55, 17)),
//                        getSkinPixelColourToNumber(originalSkin.getColor(55, 18)),
//                        getSkinPixelColourToNumber(originalSkin.getColor(55, 19))
                };
                if (choiceBoxChoices[2] < 1 || choiceBoxChoices[2] > 8) {
                    choiceBoxChoices[2] = 1;
                }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                //deprecated old villager nose method
                boolean noseUpper = (getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 43, 13)) == 666
                        && getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 44, 13)) == 666
                        && getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 43, 14)) == 666
                        && getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 44, 14)) == 666
                        && getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 43, 15)) == 666
                        && getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 44, 15)) == 666);
                boolean noseLower = (getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 11, 13)) == 666
                        && getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 12, 13)) == 666
                        && getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 11, 14)) == 666
                        && getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 12, 14)) == 666
                        && getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 11, 15)) == 666
                        && getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 12, 15)) == 666);
                hasVillagerNose = noseLower || noseUpper;
                boolean removeNosePixels = noseUpper;
                if (noseUpper) {
                    deletePixels(modifiedSkin, 43, 13, 44, 15);
                }
                //////////////////////////////////////////
                NativeImage noseTexture = null;
                int noseChoice = choiceBoxChoices[5];
                if (noseChoice >= 1 && noseChoice <= 9) {
                    if (noseChoice == 1 || noseChoice == 7 || noseChoice == 8 || noseChoice == 9) {
                        hasVillagerNose = true;
                        noseType = ETFConfigScreenSkinTool.NoseType.NONE.getByColorId(noseChoice);
                        if (noseChoice > 7) {
                            removeNosePixels = true;
                            deletePixels(modifiedSkin, 43, 13, 44, 15);
                        }
                    } else {
                        noseTexture = ETFUtils2.emptyNativeImage(8, 8);
                        int[] bounds;
                        switch (noseChoice) {
                            case 3:
                                noseType = ETFConfigScreenSkinTool.NoseType.TEXTURED_2;
                                bounds = getSkinPixelBounds("cape2");
                                break;
                            case 4:
                                noseType = ETFConfigScreenSkinTool.NoseType.TEXTURED_3;
                                bounds = getSkinPixelBounds("cape3");
                                break;
                            case 5:
                                noseType = ETFConfigScreenSkinTool.NoseType.TEXTURED_4;
                                bounds = getSkinPixelBounds("cape4");
                                break;
                            case 6:
                                noseType = ETFConfigScreenSkinTool.NoseType.TEXTURED_5;
                                bounds = getSkinPixelBounds("cape5");
                                break;
                            default:
                                noseType = ETFConfigScreenSkinTool.NoseType.TEXTURED_1;
                                bounds = getSkinPixelBounds("cape1");
                                break;
                        }

                        int noseY = 0;
                        for (int x = bounds[0]; x <= bounds[2]; x++) {
                            int noseX = 0;
                            for (int y = bounds[1]; y <= bounds[3]; y++) {
                                ETFUtils2.setPixel(noseTexture, noseX, noseY, ETFUtils2.getPixel(originalSkin, x, y));
                                noseX++;
                            }
                            noseY++;
                        }
                        //copy flip to other side
                        for (int x = 4; x < 8; x++) {
                            for (int y = 0; y < 8; y++) {
                                ETFUtils2.setPixel(noseTexture, x, y, ETFUtils2.getPixel(noseTexture, 7 - x, y));
                            }
                        }
                        //correct vertical alignment
                        for (int x = 0; x < 8; x++) {
                            for (int y = 0; y < 4; y++) {
                                int lowerColour = ETFUtils2.getPixel(noseTexture, x, y + 4);
                                ETFUtils2.setPixel(noseTexture, x, y + 4, ETFUtils2.getPixel(noseTexture, x, y));
                                ETFUtils2.setPixel(noseTexture, x, y, lowerColour);
                            }
                        }

                        texturedNoseIdentifier = ETFUtils2.res(SKIN_NAMESPACE, id + "_nose.png");
                        ETFUtils2.registerNativeImageToIdentifier(noseTexture, texturedNoseIdentifier);
                    }
                }


                //check for coat bottom
                //pink to copy coat    light blue to remove from legs
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                NativeImage coatSkin = null;
                int controllerCoat = choiceBoxChoices[1];
                if (controllerCoat >= 1 && controllerCoat <= 8) {
                    coatStyle = controllerCoat;
                    int lengthOfCoat = choiceBoxChoices[2] - 1;
                    coatLength = lengthOfCoat + 1;
                    coatIdentifier = ETFUtils2.res(SKIN_NAMESPACE, id + "_coat.png");
                    coatSkin = getCoatTexture(originalSkin, lengthOfCoat, controllerCoat >= 5);
                    ETFUtils2.registerNativeImageToIdentifier(coatSkin, coatIdentifier);
                    //UUID_PLAYER_HAS_COAT.put(id, true);
                    if (controllerCoat == 2 || controllerCoat == 4 || controllerCoat == 6 || controllerCoat == 8) {
                        //delete original pixel from skin
                        deletePixels(modifiedSkin, 4, 32, 7, 35);
                        deletePixels(modifiedSkin, 4, 48, 7, 51);
                        deletePixels(modifiedSkin, 0, 36, 15, 36 + lengthOfCoat);
                        deletePixels(modifiedSkin, 0, 52, 15, 52 + lengthOfCoat);
                    }
                    //red or green make fat coat
                    hasFatCoat = controllerCoat == 3 || controllerCoat == 4 || controllerCoat == 7 || controllerCoat == 8;
                } else {
                    coatIdentifier = null;
                }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                wasForcedSolid = choiceBoxChoices[6] == 1;

                if (wasForcedSolid) {
                    forceSolidLowerSkin(modifiedSkin);
                }
//                if (ETFConfig.getInstance().skinFeaturesEnableTransparency) {
//                    if (isSkinNotTooTransparent(originalSkin)) {
//                        allowThisETFBaseSkin = true;
//                    } else {
//                        ETFUtils2.logMessage("Skin was too transparent or had other problems", false);
//                        allowThisETFBaseSkin = false;
//                    }
//                }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                //create and register blink textures this will allow the ETFTexture to build these automatically
                NativeImage blinkSkinFile = null;
                NativeImage blinkSkinFile2 = null;
                ResourceLocation blinkIdentifier = ETFUtils2.res(SKIN_NAMESPACE, id + "_blink.png");
                ResourceLocation blink2Identifier = ETFUtils2.res(SKIN_NAMESPACE, id + "_blink2.png");

                int blinkChoice = choiceBoxChoices[0];
                this.blinkType = blinkChoice;
                if (blinkChoice >= 1 && blinkChoice <= 5) {

                    //check if lazy blink
                    if (blinkChoice <= 2) {
                        if (removeNosePixels) {
                            deletePixels(modifiedSkin, 35, 5, 36, 7);
                        }
                        //blink 1 frame if either pink or blue optional
                        blinkSkinFile = returnOptimizedBlinkFace(modifiedSkin, getSkinPixelBounds("face1"), 1, getSkinPixelBounds("face3"));
//                        parseSkinTransparency(blinkSkinFile, wasForcedSolid);
                        ETFUtils2.registerNativeImageToIdentifier(blinkSkinFile, blinkIdentifier);

                        //blink is 2 frames with blue optional
                        if (blinkChoice == 2) {
                            if (removeNosePixels) {
                                deletePixels(modifiedSkin, 59, 5, 60, 7);
                            }
                            blinkSkinFile2 = returnOptimizedBlinkFace(modifiedSkin, getSkinPixelBounds("face2"), 1, getSkinPixelBounds("face4"));
//                            parseSkinTransparency(blinkSkinFile2, wasForcedSolid);
                            ETFUtils2.registerNativeImageToIdentifier(blinkSkinFile2, blink2Identifier);
                        }
                    } else {//optimized blink
                        int eyeHeightTopDown = choiceBoxChoices[3];
                        this.blinkHeight = eyeHeightTopDown;
                        if (eyeHeightTopDown > 8 || eyeHeightTopDown < 1) {
                            eyeHeightTopDown = 1;
                        }
                        //optimized 1p high eyes
                        if (blinkChoice == 3) {
                            blinkSkinFile = returnOptimizedBlinkFace(modifiedSkin, getSkinPixelBounds("optimizedEyeSmall"), eyeHeightTopDown);
//                            parseSkinTransparency(blinkSkinFile, wasForcedSolid);
                            ETFUtils2.registerNativeImageToIdentifier(blinkSkinFile, blinkIdentifier);

                        } else if (blinkChoice == 4) {
                            blinkSkinFile = returnOptimizedBlinkFace(modifiedSkin, getSkinPixelBounds("optimizedEye2High"), eyeHeightTopDown);
                            blinkSkinFile2 = returnOptimizedBlinkFace(modifiedSkin, getSkinPixelBounds("optimizedEye2High_second"), eyeHeightTopDown);
//                            parseSkinTransparency(blinkSkinFile, wasForcedSolid);
//                            parseSkinTransparency(blinkSkinFile2, wasForcedSolid);

                            ETFUtils2.registerNativeImageToIdentifier(blinkSkinFile, blinkIdentifier);
                            ETFUtils2.registerNativeImageToIdentifier(blinkSkinFile2, blink2Identifier);
                        } else /*if( blinkChoice == 5)*/ {
                            blinkSkinFile = returnOptimizedBlinkFace(modifiedSkin, getSkinPixelBounds("optimizedEye4High"), eyeHeightTopDown);
                            blinkSkinFile2 = returnOptimizedBlinkFace(modifiedSkin, getSkinPixelBounds("optimizedEye4High_second"), eyeHeightTopDown);
//                            parseSkinTransparency(blinkSkinFile, wasForcedSolid);
//                            parseSkinTransparency(blinkSkinFile2, wasForcedSolid);
                            ETFUtils2.registerNativeImageToIdentifier(blinkSkinFile, blinkIdentifier);
                            ETFUtils2.registerNativeImageToIdentifier(blinkSkinFile2, blink2Identifier);
                        }
                    }
                }
                if (blinkSkinFile == null) blinkIdentifier = null;
                if (blinkSkinFile2 == null) blink2Identifier = null;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//                Identifier etfCapeIdentifier = null;
//                Identifier etfCapeEmissiveIdentifier = null;
//                Identifier etfCapeEnchantedIdentifier = null;
//
//                //check for cape recolor
//                int capeChoice1 = choiceBoxChoices[4];
//                // custom cape data experiment
//                // https://drive.google.com/uc?export=download&id=1rn1swLadqdMiLirz9Nrae0_VHFrTaJQe
//                //downloadImageFromUrl(player, "https://drive.google.com/uc?export=download&id=1rn1swLadqdMiLirz9Nrae0_VHFrTaJQe", "etf$CAPE",null,true);
//                if ((capeChoice1 >= 1 && capeChoice1 <= 4)) {
//                    switch (capeChoice1) {
//                        case 1 -> { //custom in skin
//                            capeType = ETFConfigScreenSkinTool.CapeType.CUSTOM;
//                            modifiedCape.copyFrom(returnCustomTexturedCape(originalSkin));
//                        }
//                        case 2 -> {
//                            capeType = ETFConfigScreenSkinTool.CapeType.MINECRAFT_CAPES_NET;
//                            modifiedCape = null;
//                            // minecraft capes mod
//                            //https://minecraftcapes.net/profile/fd22e573178c415a94fee476b328abfd/cape/
//                            initiateThirdPartyCapeDownload("https://api.minecraftcapes.net/profile/" + player.etf$getUuidAsString().replace("-", "") + "/cape/");
//
//                        }
//                        case 3 -> {
//                            capeType = ETFConfigScreenSkinTool.CapeType.OPTIFINE;
//                            modifiedCape = null;
//                            //  https://optifine.net/capes/Benjamin.png
//                            initiateThirdPartyCapeDownload("https://optifine.net/capes/" + player.etf$getName().getString() + ".png");
//
//                        }
//                        case 4 -> {
//                            capeType = ETFConfigScreenSkinTool.CapeType.ETF;
//                            NativeImage cape = ETFUtils2.getNativeImageElseNull(new Identifier(MOD_ID, "textures/capes/etf.png"));
//                            if (cape != null && !ETFUtils2.isNativeImageEmpty(modifiedCape)) {
//                                modifiedCape.copyFrom(cape);
//                            }
//                        }
//                        default -> {
//                            // cape = getNativeImageFromID(new Identifier("etf:capes/blank.png"));
//                        }
//                    }
//                }
//                if (modifiedCape != null && !ETFUtils2.isNativeImageEmpty(modifiedCape)) {
//                    //if ((capeChoice1 >= 1 && capeChoice1 <= 4) || capeChoice1 == 666) {//custom chosen
//                    etfCapeIdentifier = new Identifier(SKIN_NAMESPACE, id.toString().replaceAll("/[^a-z]/g", "") + "_cape.png");
//                    ETFUtils2.registerNativeImageToIdentifier(modifiedCape, etfCapeIdentifier);
//
//                    //UUID_PLAYER_HAS_CUSTOM_CAPE.put(id, true);
//                    // }
//                }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                //check for marker choices
                //  1 = Emissives,  2 = Enchanted
                List<Integer> markerChoices = List.of(getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 1, 17)),
                        getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 1, 18)),
                        getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 2, 17)),
                        getSkinPixelColourToNumber(ETFUtils2.getPixel(originalSkin, 2, 18)));
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                //emissives
                NativeImage emissiveImage = null;
                NativeImage emissiveBlinkImage = null;
                NativeImage emissiveBlink2Image = null;
                ResourceLocation emissiveIdentifier = null;
                ResourceLocation blinkEmissiveIdentifier = null;//new Identifier( SKIN_NAMESPACE , id + "_blink_e.png");
                ResourceLocation blink2EmissiveIdentifier = null;//new Identifier( SKIN_NAMESPACE , id + "_blink2_e.png");
//                NativeImage emissiveCape = null;
                hasEmissives = markerChoices.contains(1);
                if (hasEmissives) {
                    int[] boxChosenBounds = getSkinPixelBounds("marker" + (markerChoices.indexOf(1) + 1));
//                    emissiveCapeBounds = boxChosenBounds;
                    emissiveImage = returnMatchPixels(modifiedSkin, boxChosenBounds);

                    if (emissiveImage != null) {
                        emissiveIdentifier = ETFUtils2.res(SKIN_NAMESPACE, id + "_e.png");
                        ETFUtils2.registerNativeImageToIdentifier(emissiveImage, emissiveIdentifier);
                        if (blinkSkinFile != null) {
                            emissiveBlinkImage = returnMatchPixels(blinkSkinFile, boxChosenBounds);
                            if (emissiveBlinkImage != null) {
                                blinkEmissiveIdentifier = ETFUtils2.res(SKIN_NAMESPACE, id + "_blink_e.png");
                                ETFUtils2.registerNativeImageToIdentifier(emissiveBlinkImage, blinkEmissiveIdentifier);
                            }
                            //registerNativeImageToIdentifier(Objects.requireNonNullElseGet(checkBlink, ETFUtils2::emptyNativeImage), SKIN_NAMESPACE + id + "_blink_e.png");
                        }
                        if (blinkSkinFile2 != null) {
                            emissiveBlink2Image = returnMatchPixels(blinkSkinFile2, boxChosenBounds);
                            if (emissiveBlink2Image != null) {
                                blink2EmissiveIdentifier = ETFUtils2.res(SKIN_NAMESPACE, id + "_blink2_e.png");
                                ETFUtils2.registerNativeImageToIdentifier(emissiveBlink2Image, blink2EmissiveIdentifier);
                            }
                            //registerNativeImageToIdentifier(Objects.requireNonNullElseGet(checkBlink, ETFUtils2::emptyNativeImage), SKIN_NAMESPACE + id + "_blink2_e.png");
                        }
                        if (coatSkin != null) {
                            NativeImage checkCoat = returnMatchPixels(modifiedSkin, boxChosenBounds, coatSkin);

                            //UUID_PLAYER_HAS_EMISSIVE_COAT.put(id, checkCoat != null);
                            if (checkCoat != null) {
                                coatEmissiveIdentifier = ETFUtils2.res(SKIN_NAMESPACE, id + "_coat_e.png");
                                ETFUtils2.registerNativeImageToIdentifier(checkCoat, coatEmissiveIdentifier);
                            }
                        }
//                        if (modifiedCape != null) {
//
//                            emissiveCape = returnMatchPixels(modifiedSkin, boxChosenBounds, modifiedCape);
//                            //UUID_PLAYER_HAS_EMISSIVE_CAPE.put(id, checkCape != null);
//                            if (emissiveCape != null) {
//                                etfCapeEmissiveIdentifier = new Identifier(SKIN_NAMESPACE, id + "_cape_e.png");
//                                ETFUtils2.registerNativeImageToIdentifier(emissiveCape, etfCapeEmissiveIdentifier);
//                            }
//                        }
                        if (noseTexture != null) {
                            NativeImage checkNose = returnMatchPixels(modifiedSkin, boxChosenBounds, noseTexture);
                            //UUID_PLAYER_HAS_EMISSIVE_CAPE.put(id, checkCape != null);
                            if (checkNose != null) {
                                texturedNoseIdentifierEmissive = ETFUtils2.res(SKIN_NAMESPACE, id + "_nose_e.png");
                                ETFUtils2.registerNativeImageToIdentifier(checkNose, texturedNoseIdentifierEmissive);
                            }
                        }
                    } else {
                        hasEmissives = false;
                    }
                }
//                if (capeType == ETFConfigScreenSkinTool.CapeType.ETF) {
//                    etfCapeEmissiveIdentifier = new Identifier(MOD_ID, "textures/capes/etf_e.png");
//                }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                //enchant
                hasEnchant = markerChoices.contains(2);
                if (hasEnchant) {
                    int[] boxChosenBounds = getSkinPixelBounds("marker" + (markerChoices.indexOf(2) + 1));
//                    enchantCapeBounds = boxChosenBounds;
                    NativeImage check = returnMatchPixels(modifiedSkin, boxChosenBounds);
                    if (check != null) {
                        baseEnchantIdentifier = ETFUtils2.res(SKIN_NAMESPACE, id + "_enchant.png");
                        ETFUtils2.registerNativeImageToIdentifier(check, baseEnchantIdentifier);
                        if (blinkSkinFile != null) {
                            NativeImage checkBlink = returnMatchPixels(blinkSkinFile, boxChosenBounds);
                            if (checkBlink != null) {
                                baseEnchantBlinkIdentifier = ETFUtils2.res(SKIN_NAMESPACE, id + "_blink_enchant.png");
                                ETFUtils2.registerNativeImageToIdentifier(checkBlink, baseEnchantBlinkIdentifier);
                            }
                            //registerNativeImageToIdentifier(Objects.requireNonNullElseGet(checkBlink, ETFUtils2::emptyNativeImage), SKIN_NAMESPACE + id + "_blink_e.png");
                        }
                        if (blinkSkinFile2 != null) {
                            NativeImage checkBlink = returnMatchPixels(blinkSkinFile2, boxChosenBounds);
                            if (checkBlink != null) {
                                baseEnchantBlink2Identifier = ETFUtils2.res(SKIN_NAMESPACE, id + "_blink2_enchant.png");
                                ETFUtils2.registerNativeImageToIdentifier(checkBlink, baseEnchantBlink2Identifier);
                            }
                            //registerNativeImageToIdentifier(Objects.requireNonNullElseGet(checkBlink, ETFUtils2::emptyNativeImage), SKIN_NAMESPACE + id + "_blink2_e.png");
                        }
                        if (coatSkin != null) {
                            NativeImage checkCoat = returnMatchPixels(modifiedSkin, boxChosenBounds, coatSkin);

                            //UUID_PLAYER_HAS_EMISSIVE_COAT.put(id, checkCoat != null);
                            if (checkCoat != null) {
                                coatEnchantedIdentifier = ETFUtils2.res(SKIN_NAMESPACE, id + "_coat_enchant.png");
                                ETFUtils2.registerNativeImageToIdentifier(checkCoat, coatEnchantedIdentifier);
                            }
                        }
//                        if (modifiedCape != null) {
//
//                            NativeImage checkCape = returnMatchPixels(modifiedSkin, boxChosenBounds, modifiedCape);
//                            //UUID_PLAYER_HAS_EMISSIVE_CAPE.put(id, checkCape != null);
//                            if (checkCape != null) {
//
//                                etfCapeEnchantedIdentifier = new Identifier(SKIN_NAMESPACE, id + "_cape_enchant.png");
//                                ETFUtils2.registerNativeImageToIdentifier(checkCape, etfCapeEnchantedIdentifier);
//                            }
//                        }
                        if (noseTexture != null) {
                            NativeImage checkNose = returnMatchPixels(modifiedSkin, boxChosenBounds, noseTexture);
                            //UUID_PLAYER_HAS_EMISSIVE_CAPE.put(id, checkCape != null);
                            if (checkNose != null) {
                                texturedNoseIdentifierEnchanted = ETFUtils2.res(SKIN_NAMESPACE, id + "_nose_enchant.png");
                                ETFUtils2.registerNativeImageToIdentifier(checkNose, texturedNoseIdentifierEnchanted);
                            }
                        }
                    } else {
                        hasEnchant = false;
                    }
                }

//                parseSkinTransparency(modifiedSkin, wasForcedSolid);


                ResourceLocation modifiedSkinBlinkPatchedIdentifier = null;
                ResourceLocation modifiedSkinPatchedIdentifier = null;
                ResourceLocation modifiedSkinBlink2PatchedIdentifier = null;

//                Identifier modifiedCapePatchedIdentifier = etfCapeIdentifier;
                if (hasEmissives) {
                    if (emissiveImage != null) {
                        modifiedSkinPatchedIdentifier = ETFUtils2.res(SKIN_NAMESPACE, id + "_e_patched.png");
                        ETFTexture.patchTextureToRemoveZFightingWithOtherTexture(modifiedSkin, emissiveImage);

                        ETFUtils2.registerNativeImageToIdentifier(modifiedSkin, modifiedSkinPatchedIdentifier);
                        ETFManager.getInstance().ETF_TEXTURE_CACHE.put(modifiedSkinPatchedIdentifier,
                                ETFTexture.manual(modifiedSkinPatchedIdentifier, emissiveIdentifier, baseEnchantIdentifier));
                        if (blinkSkinFile != null) {
                            modifiedSkinBlinkPatchedIdentifier = ETFUtils2.res(SKIN_NAMESPACE, id + "_blink_e_patched.png");
                            ETFTexture.patchTextureToRemoveZFightingWithOtherTexture(blinkSkinFile, emissiveBlinkImage);
                            ETFUtils2.registerNativeImageToIdentifier(blinkSkinFile, modifiedSkinBlinkPatchedIdentifier);
                            ETFManager.getInstance().ETF_TEXTURE_CACHE.put(modifiedSkinBlinkPatchedIdentifier,
                                    ETFTexture.manual(modifiedSkinBlinkPatchedIdentifier, blinkEmissiveIdentifier, baseEnchantBlinkIdentifier));
                        }
                        if (blinkSkinFile2 != null) {
                            modifiedSkinBlink2PatchedIdentifier = ETFUtils2.res(SKIN_NAMESPACE, id + "_blink2_e_patched.png");
                            ETFTexture.patchTextureToRemoveZFightingWithOtherTexture(blinkSkinFile2, emissiveBlink2Image);
                            ETFUtils2.registerNativeImageToIdentifier(blinkSkinFile2, modifiedSkinBlink2PatchedIdentifier);
                            ETFManager.getInstance().ETF_TEXTURE_CACHE.put(modifiedSkinBlink2PatchedIdentifier,
                                    ETFTexture.manual(modifiedSkinBlink2PatchedIdentifier, blink2EmissiveIdentifier, baseEnchantBlink2Identifier));
                        }
                    }
//                    if (emissiveCape != null) {
//                        modifiedCapePatchedIdentifier = new Identifier(SKIN_NAMESPACE, id + "_cape_e_patched.png");
//                        ETFTexture.patchTextureToRemoveZFightingWithOtherTexture(modifiedCape, emissiveCape);
//                        ETFUtils2.registerNativeImageToIdentifier(modifiedCape, modifiedCapePatchedIdentifier);
//                    }
                }


                ResourceLocation modifiedSkinIdentifier = ETFUtils2.res(SKIN_NAMESPACE, id + ".png");
                ETFUtils2.registerNativeImageToIdentifier(modifiedSkin, modifiedSkinIdentifier);

                //create etf texture with player initiator
                etfTextureOfFinalBaseSkin = ETFTexture.manual(modifiedSkinIdentifier,
                        blinkIdentifier,
                        blink2Identifier,
                        emissiveIdentifier,
                        blinkEmissiveIdentifier,
                        blink2EmissiveIdentifier,
                        baseEnchantIdentifier,
                        baseEnchantBlinkIdentifier,
                        baseEnchantBlink2Identifier,
                        modifiedSkinPatchedIdentifier,
                        modifiedSkinBlinkPatchedIdentifier,
                        modifiedSkinBlink2PatchedIdentifier);

                if (normalVanillaSkinIdentifier != null)
                    ETFManager.getInstance().ETF_TEXTURE_CACHE.put(normalVanillaSkinIdentifier, etfTextureOfFinalBaseSkin);


                //if vanilla cape and there is no enchant or emissive
                //then just clear it from etf to defer to cape mods
//                if (capeType == ETFConfigScreenSkinTool.CapeType.NONE
//                        && modifiedCapePatchedIdentifier != null
//                        && etfCapeEnchantedIdentifier == null
//                        && etfCapeEmissiveIdentifier == null) {
//                    etfCape = null;
//                }else{
//                    etfCape = new ETFTexture(modifiedCapePatchedIdentifier,null,null,
//                            etfCapeEmissiveIdentifier,null,null,
//                            etfCapeEnchantedIdentifier,null,null,
//                            null,null,null);
//                }


//                if (modifiedCape != null) {
//                    modifiedCape.close();
//                }
//                modifiedSkin.close();
            } else {

//                //check if they want to try load transparent skin anyway
//                if (ETF.config().getConfig().tryETFTransparencyForAllSkins) {
//                    //parseSkinTransparency(originalSkin,wasForcedSolid);
//                    Identifier skinIdentifier = new Identifier(SKIN_NAMESPACE, id + ".png");
//                    ETFUtils2.registerNativeImageToIdentifier(originalSkin, skinIdentifier);
//                    etfTextureOfFinalBaseSkin = new ETFTexture(skinIdentifier, null, null, null, null, null, null, null, null, null, null, null);
//
//                } else {
                skinFailed("no marker");
//                }

                // System.out.println("asdasd");

            }
        } else {
            //System.out.println("asdasdffsdfsdsd");
            skinFailed("null skin");
        }
        isTextureReady = true;
    }

    public void changeSkinToThisForTool(NativeImage image) {
//        this.etfCape = null;
        this.baseEnchantBlinkIdentifier = null;
        this.baseEnchantIdentifier = null;
        this.coatEmissiveIdentifier = null;
        this.coatEnchantedIdentifier = null;
        this.baseEnchantBlink2Identifier = null;
        this.etfTextureOfFinalBaseSkin = null;
        this.coatIdentifier = null;
        this.hasEmissives = false;
        this.hasEnchant = false;
        this.hasFatCoat = false;
        this.hasFeatures = false;
        this.hasVillagerNose = false;
        // this.hasVanillaCape = false;
        this.isTextureReady = false;

        this.coatStyle = 0;
        this.coatLength = 1;
        this.blinkHeight = 1;
        this.blinkType = 0;
//        this.capeType = ETFConfigScreenSkinTool.CapeType.NONE;
        this.texturedNoseIdentifier = null;
        this.texturedNoseIdentifierEmissive = null;
        this.texturedNoseIdentifierEnchanted = null;
        this.noseType = ETFConfigScreenSkinTool.NoseType.NONE;

        //this.THIS_SKIN_IS_IN_EDITOR = true;
        this.originalSkin = image;
        checkTexture(true);

        if (etfTextureOfFinalBaseSkin != null)
            etfTextureOfFinalBaseSkin.setGUIBlink();
    }

}
