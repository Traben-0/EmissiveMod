package traben.entity_texture_features.features.property_reading;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.ETFApi;
import traben.entity_texture_features.features.texture_handlers.ETFDirectory;
import traben.entity_texture_features.utils.ETFEntity;
import traben.entity_texture_features.utils.ETFUtils2;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public class TrueRandomProvider implements ETFApi.ETFVariantSuffixProvider {


    private final int suffixTotal;
    private final String packname;
    protected EntityRandomSeedFunction entityRandomSeedFunction = (entity) -> entity.etf$getUuid().hashCode();

    private TrueRandomProvider(String secondPack, int suffixes) {
        this.suffixTotal = suffixes;
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

        int totalTextureCount = 2;
        while (ETFDirectory.getDirectoryVersionOf(ETFUtils2.addVariantNumberSuffix(vanillaIdentifier, totalTextureCount + 1)) != null) {
            totalTextureCount++;
        }
        return new TrueRandomProvider(secondPack, totalTextureCount);
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
        IntOpenHashSet allSuffixes = new IntOpenHashSet(suffixTotal);
        for (int i = 1; i <= suffixTotal; i++) {
            allSuffixes.add(i);
        }
        return allSuffixes;
    }

    @Override
    public int size() {
        return 1;
    }

    @SuppressWarnings("unused")


    @Override
    public int getSuffixForETFEntity(ETFEntity entityToBeTested) {
        if (entityToBeTested == null) return 0;
        return (Math.abs(entityRandomSeedFunction.toInt(entityToBeTested)) % suffixTotal) + 1;
    }

    @Override
    public void setRandomSupplier(final EntityRandomSeedFunction entityRandomSeedFunction) {
        if (entityRandomSeedFunction != null) {
            this.entityRandomSeedFunction = entityRandomSeedFunction;
        }
    }
}
