package traben.entity_texture_features;


import com.demonwav.mcdev.annotations.Translatable;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import traben.entity_texture_features.config.ETFConfig;
import traben.entity_texture_features.config.ETFConfigWarning;
import traben.entity_texture_features.config.ETFConfigWarnings;
import traben.entity_texture_features.config.screens.ETFConfigScreenMain;
import traben.entity_texture_features.config.screens.ETFConfigScreenWarnings;
import traben.tconfig.TConfigHandler;
import traben.entity_texture_features.utils.ETFUtils2;

import java.util.List;
import java.util.Random;
import java.util.Set;


public class ETF {

    public static final String MOD_ID = "entity_texture_features";
    public final static Logger LOGGER = LoggerFactory.getLogger("Entity Texture Features");
    public static final int EMISSIVE_FEATURE_LIGHT_VALUE = LightTexture.FULL_BRIGHT + 2;
    public static TConfigHandler<ETFConfigScreenWarnings.WarningConfig> warningConfigHandler = null;
    public static boolean IRIS_DETECTED = false;
    public static boolean SKIN_LAYERS_DETECTED = false;
    public static Set<TConfigHandler<?>> configHandlers = null;
    private static TConfigHandler<ETFConfig> CONFIG = null;

    public static TConfigHandler<ETFConfig> config() {
        if (CONFIG == null) {
            CONFIG = new TConfigHandler<>(ETFConfig::new, MOD_ID, "ETF_load");
        }
        return CONFIG;
    }

    public static void start() {
        //set true config and load from file
        CONFIG = new TConfigHandler<>(ETFConfig::new, MOD_ID, "ETF");
        registerConfigHandler(CONFIG);

        //check only once
        SKIN_LAYERS_DETECTED = isThisModLoaded("skinlayers3d");
        IRIS_DETECTED = isThisModLoaded("iris") || isThisModLoaded("oculus");

        LOGGER.info("Loading Entity Texture Features, " + randomQuip());

        warningConfigHandler = new TConfigHandler<>(ETFConfigScreenWarnings.WarningConfig::new, "etf_warnings.json", "ETF");
        registerConfigHandler(warningConfigHandler);

        ETFUtils2.checkModCompatibility();

        ETFConfigWarnings.registerConfigWarning(
                //figura
                new ETFConfigWarning.Simple(
                        "figura",
                        "figura",
                        "config." + ETF.MOD_ID + ".warn.figura.text.1",
                        "config." + ETF.MOD_ID + ".warn.figura.text.2",
                        () -> {
                            CONFIG.getConfig().skinFeaturesEnabled = false;
                            CONFIG.saveToFile();
                        }),
                //EBE
                new ETFConfigWarning.Simple(
                        "enhancedblockentities",
                        "enhancedblockentities",
                        "config." + ETF.MOD_ID + ".warn.ebe.text.1",
                        "config." + ETF.MOD_ID + ".warn.ebe.text.2",
                        null),
                //quark
                new ETFConfigWarning.Simple(
                        "quark",
                        "quark",
                        "config." + ETF.MOD_ID + ".warn.quark.text.3",
                        "config." + ETF.MOD_ID + ".warn.quark.text.4",
                        null),
                //iris and 3d skin layers trim warning
                new ETFConfigWarning.Simple(
                        "iris & 3d skin layers",
                        () -> ETF.IRIS_DETECTED && ETF.SKIN_LAYERS_DETECTED,
                        "config." + ETF.MOD_ID + ".warn.iris_3d.text.1",
                        "config." + ETF.MOD_ID + ".warn.iris_3d.text.2",
                        null),
                //no CEM mod, recommend EMF
                new ETFConfigWarning.Simple(
                        "emf",
                        () -> !isThisModLoaded("entity_model_features") && !isThisModLoaded("cem"),
                        "config." + ETF.MOD_ID + ".warn.no_emf.text.1",
                        "config." + ETF.MOD_ID + ".warn.no_emf.text.2",
                        null)
        );

    }

    private static String randomQuip() {
        String[] quips = new String[]{
                "also try EMF!",
                "also known as ETF!",
                "not to be confused with CIT, seriously, why does that keep happening?",
                "the worst server plugin one guy on my discord has ever seen!",
                "your third cousin's, dog's, previous owner's, uncle's, old boss's, fourth favourite mod!",
                "Thanks for 10 Million plus downloads!!",
                "why does no one download Solid Mobs :(",
                "breaking your resource packs since 17 Jan 2022.",
                "not fit for consumption in the US.",
                "one of the mods ever made!",
                ",serutaeF erutxeT ytitnE gnidoaL",
                "hello there!",
                "you just lost the game.",
                "did you know if you turn off the lights and whisper 'OptiFine' 3 times you will lose 20fps.",
                "now compatible with Minecraft!",
                "now available for Terraria!",
                "OptiFine's weirder younger half-brother that runs around making train noises.",
                ":)",
                "did you know this mod was made because I missed the glowing drowned textures in the Fresh animations addons.",
                "0% Opti, 100% Fine.",
                "Curse you Perry the Platypus!",
                "Lisa needs braces.",
                "Paranormal ResourcePacktivity.",
                "Has Anyone Really Been Far Even as Decided to Use Even Go Want to do Look More Like?"
        };
        int rand = new Random().nextInt(quips.length);
        return quips[rand];
    }

    public static void registerConfigHandler(TConfigHandler<?> configHandler) {
        if (configHandlers == null) configHandlers = new ObjectArraySet<>();
        configHandlers.add(configHandler);
    }

    public static Screen getConfigScreen(Screen parent) {
        try {
            return new ETFConfigScreenMain(parent);
        } catch (Exception e) {
            return null;
        }
    }

    public static Screen getConfigScreen(Minecraft ignored, Screen parent) {
        return getConfigScreen(parent);
    }

    public static boolean isThisModLoaded(String modId) {
        return ETFVersionDifferenceManager.isThisModLoaded(modId);
    }

    public static List<String> modsLoaded() {
        return ETFVersionDifferenceManager.modsLoaded();
    }

    @SuppressWarnings("unused")
    public static boolean isForge() {
        return ETFVersionDifferenceManager.isForge();
    }

    public static boolean isFabric() {
        return !ETFVersionDifferenceManager.isForge();
    }

    //the below act as handlers for minecraft version differences that have come up during development
    //for instance biome code changed in 1.18.2
    @Nullable
    public static String getBiomeString(Level world, BlockPos pos) {
        if (world == null || pos == null) return null;
        //1.19 & 1.18.2 variation
        return world.getBiome(pos).unwrapKey().toString().split(" / ")[1].replaceAll("[^\\da-zA-Z_:-]", "");
    }

    @NotNull
    public static Component getTextFromTranslation(@Translatable(foldMethod = true) String translationKey) {
        return Component.translatable(translationKey);
    }
}
