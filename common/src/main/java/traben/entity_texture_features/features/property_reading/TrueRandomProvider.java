package traben.entity_texture_features.features.property_reading;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.ETFApi;
import traben.entity_texture_features.features.texture_handlers.ETFDirectory;
import traben.entity_texture_features.utils.ETFEntity;
import traben.entity_texture_features.utils.ETFUtils2;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public class TrueRandomProvider implements ETFApi.ETFVariantSuffixProvider {


    private final int[] suffixes;
    private final String packname;
    protected EntityRandomSeedFunction entityRandomSeedFunction = ETFEntity::etf$getOptifineId;

    private TrueRandomProvider(String secondPack, int[] suffixes) {
        this.suffixes = suffixes;
        this.packname = secondPack;
    }

    @Nullable
    public static TrueRandomProvider of(ResourceLocation vanillaIdentifier) {
        ResourceManager resources = Minecraft.getInstance().getResourceManager();

        ResourceLocation second = ETFDirectory.getDirectoryVersionOf(ETFUtils2.addVariantNumberSuffix(vanillaIdentifier, 2));
        if (second == null) return null;

        @Nullable String secondPack = resources.getResource(second).map(Resource::sourcePackId).orElse(null);
        @Nullable String vanillaPack = resources.getResource(vanillaIdentifier).map(Resource::sourcePackId).orElse(null);

        if (secondPack == null || !secondPack.equals(ETFUtils2.returnNameOfHighestPackFromTheseTwo(secondPack, vanillaPack))) {
            return null;
        }
        List<Integer> suffixes = new ArrayList<>();
        suffixes.add(1);
        suffixes.add(2);

        //matches optifines weird behaviour
        boolean notAllowSkip = !ETF.config().getConfig().optifine_allowWeirdSkipsInTrueRandom;

        for (int i = 3; i < suffixes.size() + 10; i++) {
            if (ETFDirectory.getDirectoryVersionOf(ETFUtils2.addVariantNumberSuffix(vanillaIdentifier, i)) != null) {
                suffixes.add(i);
            }else if (notAllowSkip){
                 break;
            }
        }

        if (suffixes.get(suffixes.size() - 1) != suffixes.size()) {
            ETFUtils2.logWarn("Random suffixes ["+suffixes+"] are not sequential for " + vanillaIdentifier + " in pack " + secondPack +
                    " this is not recommended but has been enabled in the optifine compat settings.");
        }

        return new TrueRandomProvider(secondPack, suffixes.stream().mapToInt(i -> i).toArray());
    }

    public @Nullable String getPackName() {
        return packname;
    }

    @Override
    public boolean entityCanUpdate(UUID uuid) {
        return false;
    }

    @SuppressWarnings("unused")
    @Override
    public IntOpenHashSet getAllSuffixes() {
        return new IntOpenHashSet(suffixes);
    }

    @Override
    public int size() {
        return 1;
    }

    @SuppressWarnings("unused")


    @Override
    public int getSuffixForETFEntity(ETFEntity entityToBeTested) {
        if (entityToBeTested == null) return 0;
        return suffixes[Math.abs(entityRandomSeedFunction.toInt(entityToBeTested)) % suffixes.length] ;
    }

    @Override
    public void setRandomSupplier(final EntityRandomSeedFunction entityRandomSeedFunction) {
        if (entityRandomSeedFunction != null) {
            this.entityRandomSeedFunction = entityRandomSeedFunction;
        }
    }
}
