package traben.entity_texture_features.utils;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.ETF;


public class ETFLruCache<X, Y> extends Object2ObjectLinkedOpenHashMap<X, Y> {

    final int capacity;

    {
        defRetValue = null;
    }

    public ETFLruCache() {
        this.capacity = 2048;
    }

    @Nullable
    public Y get(Object key) {
        //noinspection unchecked
        return getAndMoveToFirst((X) key);
    }


    public Y put(X key, Y value) {
        double sizeModifier = Math.max(1, ETF.config().getConfig().advanced_IncreaseCacheSizeModifier);
        if (size() >= capacity * sizeModifier) {
            X lastKey = lastKey();
            if (!lastKey.equals(key)) {
                remove(lastKey);
            }
        }
        return putAndMoveToFirst(key, value);
    }

    public void removeEntryOnly(X key) {
        remove(key);
    }
}
