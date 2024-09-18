package traben.entity_texture_features.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.config.ETFConfigWarning;
import traben.entity_texture_features.config.ETFConfigWarnings;
import traben.entity_texture_features.features.ETFManager;
import traben.entity_texture_features.features.ETFRenderContext;
import traben.entity_texture_features.features.texture_handlers.ETFTexture;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.io.InputStream;
import java.util.*;

import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.MutableComponent;
#if MC > MC_20_2
import net.minecraft.network.chat.contents.PlainTextContents;
#else
import net.minecraft.network.chat.contents.LiteralContents;
#endif
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;


public abstract class ETFUtils2 {

    public static @NotNull ResourceLocation res(String fullPath){
        #if MC >= MC_21
        return ResourceLocation.parse(fullPath);
        #else 
        return new ResourceLocation(fullPath);
        #endif
    }

    public static @NotNull ResourceLocation res(String namespace, String path){
        #if MC >= MC_21
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
        #else 
        return new ResourceLocation(namespace, path);
        #endif
    }

    public static ResourceLocation getETFVariantNotNullForInjector(ResourceLocation identifier) {
        //do not modify texture
        if (identifier == null
                || ETFRenderContext.getCurrentEntity() == null
                || !ETFRenderContext.isAllowedToRenderLayerTextureModify())
            return identifier;

        //get etf modified texture
        ETFTexture etfTexture = ETFManager.getInstance().getETFTextureVariant(identifier, ETFRenderContext.getCurrentEntity());
        if (ETFRenderContext.isAllowedToPatch()) {
            etfTexture.assertPatchedTextures();
        }
        ResourceLocation modified = etfTexture.getTextureIdentifier(ETFRenderContext.getCurrentEntity());

        //check not null just to be safe, it shouldn't be however
        //noinspection ConstantValue
        return modified == null ? identifier : modified;
    }

    public static boolean renderEmissive(ETFTexture texture, MultiBufferSource provider, RenderMethodForOverlay renderer) {
        if (!ETF.config().getConfig().canDoEmissiveTextures()) return false;
        ResourceLocation emissive = texture.getEmissiveIdentifierOfCurrentState();
        if (emissive != null) {
            boolean wasAllowed = ETFRenderContext.isAllowedToRenderLayerTextureModify();
            ETFRenderContext.preventRenderLayerTextureModify();

            VertexConsumer emissiveConsumer = provider.getBuffer(
                    ETFRenderContext.canRenderInBrightMode() ?
                            RenderType.beaconBeam(emissive, true) :
                            ETFRenderContext.shouldEmissiveUseCullingLayer() ?
                                    RenderType.entityTranslucentCull(emissive) :
                                    RenderType.entityTranslucent(emissive));

            if (wasAllowed) ETFRenderContext.allowRenderLayerTextureModify();

            ETFRenderContext.startSpecialRenderOverlayPhase();
            renderer.render(emissiveConsumer, ETF.EMISSIVE_FEATURE_LIGHT_VALUE);
            ETFRenderContext.endSpecialRenderOverlayPhase();
            return true;
        }
        return false;
    }

    public static boolean renderEnchanted(ETFTexture texture, MultiBufferSource provider, int light, RenderMethodForOverlay renderer) {
        //attempt enchanted render
        ResourceLocation enchanted = texture.getEnchantIdentifierOfCurrentState();
        if (enchanted != null) {
            boolean wasAllowed = ETFRenderContext.isAllowedToRenderLayerTextureModify();
            ETFRenderContext.preventRenderLayerTextureModify();
            VertexConsumer enchantedVertex = ItemRenderer.getArmorFoilBuffer(provider, RenderType.armorCutoutNoCull(enchanted), #if MC < MC_21 false, #endif true);
            if (wasAllowed) ETFRenderContext.allowRenderLayerTextureModify();

            ETFRenderContext.startSpecialRenderOverlayPhase();
            renderer.render(enchantedVertex, light);
            ETFRenderContext.endSpecialRenderOverlayPhase();
            return true;
        }
        return false;
    }

    @Nullable
    public static ResourceLocation addVariantNumberSuffix(@NotNull ResourceLocation identifier, int variant) {
        var changed = ETFUtils2.res(addVariantNumberSuffix(identifier.toString(), variant));
        return identifier.equals(changed) ? null : changed;
    }

    @NotNull
    public static String addVariantNumberSuffix(String identifierString, int variant) {
        if (variant < 2) return identifierString;

        String file = identifierString.endsWith(".png") ? "png" : identifierString.substring(identifierString.lastIndexOf('.') + 1);

        if (identifierString.matches("\\D+\\d+\\." + file)) {
            return identifierString.replace("." + file, "." + variant + "." + file);
        }
        return identifierString.replace("." + file, variant + "." + file);
    }

    @Nullable
    public static ResourceLocation replaceIdentifier(ResourceLocation id, String regex, String replace) {
        if (id == null) return null;
        try {
            return ETFUtils2.res(id.getNamespace(), id.getPath().replaceFirst(regex, replace));
        } catch (ResourceLocationException idFail) {
            ETFUtils2.logError(ETF.getTextFromTranslation("config.entity_texture_features.illegal_path_recommendation").getString() + "\n" + idFail);
        } catch (Exception ignored) {}
        return null;
    }

    @Nullable
    public static String returnNameOfHighestPackFromTheseMultiple(String[] packNameList) {
        ArrayList<String> packNames = new ArrayList<>(Arrays.asList(packNameList));
        //loop through and remove the one from the lowest pack of the first 2 entries
        //this iterates over the whole array
        final ArrayList<String> knownResourcepackOrder = ETFManager.getInstance().KNOWN_RESOURCEPACK_ORDER;
        while (packNames.size() > 1) {
            packNames.remove(knownResourcepackOrder.indexOf(packNames.get(0)) >= knownResourcepackOrder.indexOf(packNames.get(1)) ? 1 : 0);
        }
        //here the array is down to 1 entry which should be the one in the highest pack
        return packNames.get(0);
    }

    @Nullable
    public static String returnNameOfHighestPackFromTheseTwo(@Nullable String pack1, @Nullable String pack2) {
        if (pack1 == null) return null;
        if (pack1.equals(pack2) || pack2 == null) return pack1;

        return ETFManager.getInstance().KNOWN_RESOURCEPACK_ORDER.indexOf(pack1) >= ETFManager.getInstance().KNOWN_RESOURCEPACK_ORDER.indexOf(pack2) ? pack1 : pack2;
    }

    @Nullable
    public static Properties readAndReturnPropertiesElseNull(ResourceLocation path) {
        Properties props = new Properties();
        try (InputStream in = Minecraft.getInstance().getResourceManager().getResource(path).get().open()) {
            props.load(in);
            return props;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static List<Properties> readAndReturnAllLayeredPropertiesElseNull(ResourceLocation path) {
        List<Properties> props = new ArrayList<>();
        try {
            var resources = Minecraft.getInstance().getResourceManager().getResourceStack(path);
            for (Resource resource : resources) {
                if (resource == null) continue;
                try (InputStream in = resource.open()) {
                    Properties prop = new Properties();
                    prop.load(in);
                    if (!prop.isEmpty()) {
                        props.add(prop);
                    }
                } catch (Exception ignored) {}
            }
            return props.isEmpty() ? null : props;
        } catch (Exception e) {
            return null;
        }
    }

    public static NativeImage getNativeImageElseNull(@Nullable ResourceLocation identifier) {

        try {
            //try catch is intended
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(identifier);
            if (resource.isPresent()) {
                try (InputStream in = resource.get().open()) {
                    return NativeImage.read(in);
                } catch (Exception e) {
                    return null;
                }
            } else {
                AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(identifier);
                if (texture instanceof DynamicTexture nativeImageBackedTexture) {
                    var image2 = nativeImageBackedTexture.getPixels();
                    if (image2 == null) return null;
                    NativeImage image3 = new NativeImage(image2.getWidth(), image2.getHeight(), false);
                    image3.copyFrom(image2);
                    return image3;
                }
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    //improvements to logging by @Maximum#8760
    public static void logMessage(String obj) {
        logMessage(obj, false);
    }

    public static void logMessage(String obj, boolean inChat) {
        if (!obj.endsWith(".")) obj = obj + ".";
        if (inChat) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.displayClientMessage(MutableComponent.create(
                        new #if MC > MC_20_2 PlainTextContents.LiteralContents #else LiteralContents #endif
                                ("§a[INFO]§r [ETF]: " + obj))/*.formatted(Formatting.GRAY, Formatting.ITALIC)*/ , false);
            } else {
                ETF.LOGGER.info("[ETF]: {}", obj);
            }
        } else {
            ETF.LOGGER.info("[ETF]: {}", obj);
        }
    }

    //improvements to logging by @Maximum#8760
    public static void logWarn(String obj) {
        logWarn(obj, false);
    }

    public static void logWarn(String obj, boolean inChat) {
        if (!obj.endsWith(".")) obj = obj + ".";
        if (inChat) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.displayClientMessage(MutableComponent.create(
                        new #if MC > MC_20_2 PlainTextContents.LiteralContents #else LiteralContents #endif
                                ("§e[WARN]§r [Entity Texture Features]: " + obj)).withStyle(ChatFormatting.YELLOW), false);
            } else {
                ETF.LOGGER.warn("[ETF]: {}", obj);
            }
        } else {
            ETF.LOGGER.warn("[ETF]: {}", obj);
        }
    }

    //improvements to logging by @Maximum#8760
    public static void logError(String obj) {
        logError(obj, false);
    }

    public static void logError(String obj, boolean inChat) {
        if (!obj.endsWith(".")) obj = obj + ".";
        if (inChat) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.displayClientMessage(MutableComponent.create(
                        new #if MC > MC_20_2 PlainTextContents.LiteralContents #else LiteralContents #endif
                                ("§4[ERROR]§r [Entity Texture Features]: " + obj)).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
            } else {
                ETF.LOGGER.error("[ETF]: {}", obj);
            }
        } else {
            ETF.LOGGER.error("[ETF]: {}", obj);
        }
    }

    public static NativeImage emptyNativeImage() {
        return emptyNativeImage(64, 64);
    }

    public static NativeImage emptyNativeImage(int Width, int Height) {
        NativeImage empty = new NativeImage(Width, Height, false);
        empty.fillRect(0, 0, Width, Height, 0);
        return empty;
    }

    public static boolean registerNativeImageToIdentifier(NativeImage image, ResourceLocation identifier) {
        if (image == null || identifier == null) {
            logError("registering native image failed: " + image + ", " + identifier);
            return false;
        }
        try {
            NativeImage closableImage = new NativeImage(image.getWidth(), image.getHeight(), true);
            closableImage.copyFrom(image);

            Minecraft.getInstance().getTextureManager().release(identifier);

            DynamicTexture closableBackedTexture = new DynamicTexture(closableImage);
            Minecraft.getInstance().getTextureManager().register(identifier, closableBackedTexture);

            return true;
        } catch (Exception e) {
            logError("registering native image failed: " + e);
            return false;
        }

    }

    public static void checkModCompatibility() {
        for (ETFConfigWarning warning :
                ETFConfigWarnings.getRegisteredWarnings()) {
            warning.testWarningAndApplyFixIfEnabled();
        }
    }


    public interface RenderMethodForOverlay {
        void render(VertexConsumer consumer, int light);
    }

}
