package traben.entity_texture_features.features.texture_handlers;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.features.ETFManager;
import traben.entity_texture_features.utils.ETFUtils2;

import java.util.Optional;

public enum ETFDirectory {
    DOES_NOT_EXIST(null),
    ETF(new String[]{"textures", "etf/random"}),
    OLD_OPTIFINE(new String[]{"textures/entity", "optifine/mob"}),
    OPTIFINE(new String[]{"textures", "optifine/random"}),
    VANILLA(null);

    private final String[] replaceStrings;

    ETFDirectory(String[] replaceStrings) {
        this.replaceStrings = replaceStrings;
    }


    public static Object2ReferenceOpenHashMap<@NotNull ResourceLocation, @NotNull ETFDirectory> getCache() {
        return ETFManager.getInstance().ETF_DIRECTORY_CACHE;
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    @Nullable
    public static ResourceLocation getDirectoryVersionOf(@Nullable ResourceLocation vanillaIdentifier) {
        if (vanillaIdentifier == null) return null;
        ETFDirectory directory = getDirectoryOf(vanillaIdentifier);
        return switch (directory) {
            case DOES_NOT_EXIST -> null;
            case VANILLA -> vanillaIdentifier;
            default -> getIdentifierAsDirectory(vanillaIdentifier, directory);
            //ETFUtils2.replaceIdentifier(vanillaIdentifier, directory.replaceStrings[0], directory.replaceStrings[1]);
        };
    }

    @NotNull
    public static ETFDirectory getDirectoryOf(@NotNull ResourceLocation vanillaIdentifier) {
        Object2ReferenceOpenHashMap<@NotNull ResourceLocation, @NotNull ETFDirectory> cache = getCache();
        return cache.computeIfAbsent(vanillaIdentifier, ETFDirectory::findDirectoryOf);
    }

    @NotNull
    private static ETFDirectory findDirectoryOf(ResourceLocation vanillaIdentifier) {
        //check already directory'd textures
        String path = vanillaIdentifier.getPath();
        ResourceManager resources = Minecraft.getInstance().getResourceManager();

        if (path.contains("etf/random/entity") && resources.getResource(vanillaIdentifier).isPresent()) {
            return ETF;
        } else if (path.contains("optifine/random/entity") && resources.getResource(vanillaIdentifier).isPresent()) {
            return OPTIFINE;
        } else if (path.contains("optifine/mob") && resources.getResource(vanillaIdentifier).isPresent()) {
            return OLD_OPTIFINE;
        }

        //it is not cached and does not need to be
        //may either be properties or image
        ObjectArrayList<ETFDirectory> foundDirectories = new ObjectArrayList<>();

        if (resources.getResource(getIdentifierAsDirectory(vanillaIdentifier, VANILLA)).isPresent())
            foundDirectories.add(VANILLA);
        if (resources.getResource(getIdentifierAsDirectory(vanillaIdentifier, OLD_OPTIFINE)).isPresent())
            foundDirectories.add(OLD_OPTIFINE);
        if (resources.getResource(getIdentifierAsDirectory(vanillaIdentifier, OPTIFINE)).isPresent())
            foundDirectories.add(OPTIFINE);
        if (resources.getResource(getIdentifierAsDirectory(vanillaIdentifier, ETF)).isPresent())
            foundDirectories.add(ETF);

        //these are here as these will be 90%+ cases and will be faster
        if (foundDirectories.isEmpty()) {
            return DOES_NOT_EXIST;
        } else if (foundDirectories.size() == 1) {
            return foundDirectories.get(0);
        } else {
            //must be multiple
            //find the one in the highest resource-pack
            Object2ReferenceOpenHashMap<String, ETFDirectory> resourcePackNames = new Object2ReferenceOpenHashMap<>();

            for (ETFDirectory directory :
                    foundDirectories) {
                //map result already has internal 0123 order of pack directories ironed out only need to check pack order
                resources.getResource(getIdentifierAsDirectory(vanillaIdentifier, directory))
                        .ifPresent(value -> resourcePackNames.put(value.sourcePackId(), directory));
            }

            String returnedPack = ETFUtils2.returnNameOfHighestPackFromTheseMultiple(resourcePackNames.keySet().toArray(new String[0]));
            return returnedPack != null ? resourcePackNames.get(returnedPack) : VANILLA;
        }
    }

    @NotNull
    public static ResourceLocation getIdentifierAsDirectory(ResourceLocation identifier, ETFDirectory directory) {
        if (directory.doesReplace()) {
            return ETFUtils2.res(identifier.getNamespace(), identifier.getPath().replace(directory.replaceStrings[0], directory.replaceStrings[1]));
        } else {
            return identifier;
        }
    }

    public boolean doesReplace() {
        return this.replaceStrings != null;
    }


}
