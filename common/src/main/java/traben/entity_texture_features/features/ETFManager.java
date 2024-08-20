package traben.entity_texture_features.features;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.ETFApi;
import traben.entity_texture_features.config.ETFConfig;
import traben.entity_texture_features.config.screens.skin.ETFConfigScreenSkinTool;
import traben.entity_texture_features.features.player.ETFPlayerEntity;
import traben.entity_texture_features.features.player.ETFPlayerTexture;
import traben.entity_texture_features.features.texture_handlers.ETFDirectory;
import traben.entity_texture_features.features.texture_handlers.ETFTexture;
import traben.entity_texture_features.features.texture_handlers.ETFTextureVariator;
import traben.entity_texture_features.utils.ETFEntity;
import traben.entity_texture_features.utils.ETFLruCache;
import traben.entity_texture_features.utils.ETFUtils2;
import traben.entity_texture_features.utils.EntityIntLRU;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import static traben.entity_texture_features.ETF.MOD_ID;
import static traben.entity_texture_features.features.player.ETFPlayerTexture.SKIN_NAMESPACE;


public class ETFManager {

    private static final ETFTexture ETF_ERROR_TEXTURE = getErrorETFTexture();
    private static ETFManager instance;
    public final ObjectOpenHashSet<String> EMISSIVE_SUFFIX_LIST = new ObjectOpenHashSet<>();
    public final ETFLruCache<UUID, ETFPlayerTexture> PLAYER_TEXTURE_MAP = new ETFLruCache<>();
    public final ArrayList<String> KNOWN_RESOURCEPACK_ORDER = new ArrayList<>();
    public final ObjectOpenHashSet<EntityType<?>> ENTITY_TYPE_IGNORE_PARTICLES = new ObjectOpenHashSet<>();
    //this is a cache of all known ETFTexture versions of any existing resource-pack texture, used to prevent remaking objects
    public final Object2ReferenceOpenHashMap<@NotNull ResourceLocation, @Nullable ETFTexture> ETF_TEXTURE_CACHE = new Object2ReferenceOpenHashMap<>();
    public final EntityIntLRU LAST_SUFFIX_OF_ENTITY = new EntityIntLRU();
    public final EntityIntLRU LAST_RULE_INDEX_OF_ENTITY = new EntityIntLRU();
    public final Object2ReferenceOpenHashMap<@NotNull ResourceLocation, @NotNull ETFDirectory> ETF_DIRECTORY_CACHE = new Object2ReferenceOpenHashMap<>();// = new Object2ReferenceOpenHashMap<>();
    private final Object2ObjectOpenHashMap<ResourceLocation, ETFTextureVariator> VARIATOR_MAP = new Object2ObjectOpenHashMap<>();
    public UUID ENTITY_DEBUG = null;
    public Boolean mooshroomBrownCustomShroomExists = null;
    public Boolean mooshroomRedCustomShroomExists = null;
    public ETFTexture redMooshroomAlt = null;
    public ETFTexture brownMooshroomAlt = null;

    private ETFManager() {
        for (PackResources pack :
                Minecraft.getInstance().getResourceManager().listPacks().toList()) {
            KNOWN_RESOURCEPACK_ORDER.add(pack.packId());
        }
        try {
            List<Properties> props = new ArrayList<>();
            String[] paths = {"optifine/emissive.properties", "textures/emissive.properties", "etf/emissive.properties"};
            for (String path :
                    paths) {
                //retrieve all layered resources
                var prop = ETFUtils2.readAndReturnAllLayeredPropertiesElseNull(ETFUtils2.res(path));
                if (prop != null)
                    props.addAll(prop);
            }
            for (Properties prop :
                    props) {
                //not an optifine property that I know of but this has come up in a few packs, so I am supporting it
                String[] keys = {"entities.suffix.emissive", "suffix.emissive"};
                for (String key : keys) {
                    String value = prop.getProperty(key);
                    if (value != null) EMISSIVE_SUFFIX_LIST.add(value);
                }
            }
            if (ETF.config().getConfig().alwaysCheckVanillaEmissiveSuffix) {
                EMISSIVE_SUFFIX_LIST.add("_e");
            }

            if (EMISSIVE_SUFFIX_LIST.isEmpty()) {
                ETFUtils2.logMessage("no emissive suffixes found: default emissive suffix '_e' used");
                EMISSIVE_SUFFIX_LIST.add("_e");
            } else {
                ETFUtils2.logMessage("emissive suffixes loaded: " + EMISSIVE_SUFFIX_LIST);
            }
        } catch (Exception e) {
            ETFUtils2.logError("emissive suffixes could not be read: default emissive suffix '_e' used");
            EMISSIVE_SUFFIX_LIST.add("_e");
        }


    }

    public static ETFManager getInstance() {
        if (instance == null)
            instance = new ETFManager();
        return instance;
    }

    public static void resetInstance() {
        ETF.config().loadFromFile();

        //instance based format solves the issue of hashmaps and arrays being clearing while also being accessed
        //as now those rare transitional (reading during clearing) occurrences will simply read from the previous instance of manager
        instance = new ETFManager();
    }

    public static @NotNull ETFTexture getErrorETFTexture() {
        ETFUtils2.registerNativeImageToIdentifier(ETFUtils2.emptyNativeImage(), ETFUtils2.res(MOD_ID, "error.png"));
        return new ETFTexture(ETFUtils2.res(MOD_ID, "error.png")/*, false*/ );//, ETFTexture.TextureSource.GENERIC_DEBUG);
    }

    public static ETFConfig.EmissiveRenderModes getEmissiveMode() {
        var mode = ETF.config().getConfig().getEmissiveRenderMode();
        if (mode == ETFConfig.EmissiveRenderModes.BRIGHT
                && ETFRenderContext.getCurrentEntity() != null
                && !ETFRenderContext.getCurrentEntity().etf$canBeBright()) {
            return ETFConfig.EmissiveRenderModes.DULL;
        }
        return mode;
    }

    public String getGeneralPrintout() {
        return "§aGeneral: §r" +
                "\n§2 - amount of 'base' textures: §r" + VARIATOR_MAP.size() +
                "\n§2 - total textures including variants: §r" + ETF_TEXTURE_CACHE.size()
                ;
    }

    public void doTheBigBoyPrintoutKronk() {
        StringBuilder out = new StringBuilder();

        out.append("\n||||||||||||||-ETF EVERYTHING LOG START-|||||||||||||||")
                .append("\n----------------------------------------")
                .append("\n-----------General stats-------------")
                .append("\n----------------------------------------")
                .append("\n known emissive suffixes: \n - ").append(EMISSIVE_SUFFIX_LIST)
                .append("\n player textures: \n - ").append(PLAYER_TEXTURE_MAP.size())
//                .append("\n image files read: \n - ").append(KNOWN_NATIVE_IMAGES.size())
//                .append("\n - known resource-pack order: §r\n   ").append(KNOWN_RESOURCEPACK_ORDER.size())
                .append("\n----------------------------------------")
                .append("\n----------Texture totals----------------")
                .append("\n----------------------------------------")
                .append("\n amount of textures that have or can variate: \n - ").append(VARIATOR_MAP.size())
                .append("\n amount of textures seen by ETF total (not including emissives): \n - ").append(ETF_TEXTURE_CACHE.size());

        StringBuilder textureLoopVariates = new StringBuilder();
        StringBuilder textureLoopNormal = new StringBuilder();
        int totalEmissive = 0;
        int totalEnchant = 0;
        for (ETFTexture texture :
                ETF_TEXTURE_CACHE.values()) {
            if (texture != null) {
                if (texture.isEmissive()) totalEmissive++;
                if (texture.isEnchanted()) totalEnchant++;
                if (VARIATOR_MAP.containsKey(texture.thisIdentifier)) {
                    textureLoopVariates.append("\n - ").append(
                            VARIATOR_MAP.get(texture.thisIdentifier).getPrintout().replaceAll("\n", "\n      "));
                }
                textureLoopNormal.append("\n - ").append(texture);

            }
        }
        out.append("\n total emissives: \n - ").append(totalEmissive)
                .append("\n total enchanted: \n - ").append(totalEnchant)
                .append("\n----------------------------------------")
                .append("\n----------ALL texture groups-------------")
                .append("\n----------------------------------------")
                .append("\n (Note: all of these can be varied via random entity rules)")
                .append(textureLoopVariates.toString().replaceAll("§.", ""))
                .append("\n----------------------------------------")
                .append("\n----------ALL Textures Seen-------------")
                .append("\n----------------------------------------")
                .append("\n (Note: these are not all variable by random entity rules, but can usually be emissive)")
                .append(textureLoopNormal)
                .append("\n----------------------------------------");


        out.append("\n----------------------------------------")
                .append("\n||||||||||||||-ETF EVERYTHING LOG END-|||||||||||||||");

        ETFUtils2.logMessage(out.toString());
    }

    public void grabSpecialProperties(Properties props, ETFEntity entity) {
        if (entity == null) return;

        if (props.containsKey("vanillaBrightnessOverride")) {
            String value = props.getProperty("vanillaBrightnessOverride").trim();

            try {
                int tryNumber = Integer.parseInt(value.replaceAll("\\D", ""));
                if (tryNumber >= 16) tryNumber = 15;
                if (tryNumber < 0) tryNumber = 0;
                ETF.config().getConfig().entityLightOverrides.put(entity.etf$getEntityKey(), tryNumber);
            } catch (NumberFormatException ignored) {
            }
        }

        if (props.containsKey("suppressParticles")
                && "true".equals(props.getProperty("suppressParticles"))) {
            ENTITY_TYPE_IGNORE_PARTICLES.add(entity.etf$getType());
        }

        if (props.containsKey("entityRenderLayerOverride")) {
            String layer = props.getProperty("entityRenderLayerOverride");
            switch (layer) {
                case "translucent":
                    ETF.config().getConfig().entityRenderLayerOverrides.put(entity.etf$getEntityKey(), ETFConfig.RenderLayerOverride.TRANSLUCENT);
                    break;
                case "translucent_cull":
                    ETF.config().getConfig().entityRenderLayerOverrides.put(entity.etf$getEntityKey(), ETFConfig.RenderLayerOverride.TRANSLUCENT_CULL);
                    break;
                case "end_portal":
                    ETF.config().getConfig().entityRenderLayerOverrides.put(entity.etf$getEntityKey(), ETFConfig.RenderLayerOverride.END);
                    break;
                case "outline":
                    ETF.config().getConfig().entityRenderLayerOverrides.put(entity.etf$getEntityKey(), ETFConfig.RenderLayerOverride.OUTLINE);
                    break;
            }
        }
    }


    public void markEntityForDebugPrint(UUID uuid) {
        if (ETF.config().getConfig().debugLoggingMode != ETFConfig.DebugLogMode.None) {
            ENTITY_DEBUG = uuid;
        }
    }

    @NotNull
    public ETFTexture getETFTextureNoVariation(ResourceLocation vanillaIdentifier) {
        return getOrCreateETFTexture(vanillaIdentifier);
    }

    @NotNull
    public ETFTexture getETFTextureVariant(@NotNull ResourceLocation vanillaIdentifier, @Nullable ETFEntity entity) {
        if (entity == null
                || entity.etf$getUuid() == ETFApi.ETF_GENERIC_UUID
                || (entity.etf$getBlockPos().equals(Vec3i.ZERO) && entity.etf$getUuid().getLeastSignificantBits() != ETFApi.ETF_SPAWNER_MARKER)) {
            return getETFTextureNoVariation(vanillaIdentifier);
        }
        if (!VARIATOR_MAP.containsKey(vanillaIdentifier)) {
            if (SKIN_NAMESPACE.equals(vanillaIdentifier.getNamespace())) {
                return getETFTextureNoVariation(vanillaIdentifier);
            } else {
                VARIATOR_MAP.put(vanillaIdentifier, ETFTextureVariator.of(vanillaIdentifier));
                if (ETF.config().getConfig().logTextureDataInitialization) {
                    ETFUtils2.logMessage("Amount of 'base' textures: " + VARIATOR_MAP.size());
                    ETFUtils2.logMessage("Total textures including variants: " + ETF_TEXTURE_CACHE.size());
                }
            }
        }
        return VARIATOR_MAP.get(vanillaIdentifier).getVariantOf(entity);
    }


    @NotNull
    private ETFTexture getOrCreateETFTexture(ResourceLocation ofIdentifier) {
        return ETF_TEXTURE_CACHE.computeIfAbsent(ofIdentifier, (k)-> new ETFTexture((ResourceLocation) k));
    }

    @Nullable
    public ETFPlayerTexture getPlayerTexture(Player player, ResourceLocation rendererGivenSkin) {
        return getPlayerTexture((ETFPlayerEntity) player, rendererGivenSkin);
    }

    @Nullable
    public ETFPlayerTexture getPlayerTexture(ETFPlayerEntity player, ResourceLocation rendererGivenSkin) {
        try {
            UUID id = player.etf$getUuid();
            if (PLAYER_TEXTURE_MAP.containsKey(id)) {
                ETFPlayerTexture possibleSkin = PLAYER_TEXTURE_MAP.get(id);
                if (possibleSkin == null ||
                        (possibleSkin.player == null && possibleSkin.isCorrectObjectForThisSkin(rendererGivenSkin))) {
                    return null;
                } else if (possibleSkin.isCorrectObjectForThisSkin(rendererGivenSkin)
                        || Minecraft.getInstance().screen instanceof ETFConfigScreenSkinTool) {
                    return possibleSkin;
                }
            }
            PLAYER_TEXTURE_MAP.put(id, null);//incase of crash
            ETFPlayerTexture etfPlayerTexture = new ETFPlayerTexture(player, rendererGivenSkin);
            PLAYER_TEXTURE_MAP.put(id, etfPlayerTexture);
            return etfPlayerTexture;
        } catch (Exception e) {
            return null;
        }
    }


    public enum TextureSource {
        ENTITY,
        BLOCK_ENTITY,
        ENTITY_FEATURE
    }


}
