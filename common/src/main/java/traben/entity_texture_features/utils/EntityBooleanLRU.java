package traben.entity_texture_features.utils;

import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;

import java.util.UUID;

public class EntityBooleanLRU extends Object2BooleanLinkedOpenHashMap<UUID> {
    private final int capacity;

    {
        defaultReturnValue(false);
    }

    public EntityBooleanLRU(int capacity) {
        this.capacity = capacity;
    }

    public EntityBooleanLRU() {
        this.capacity = 2048;
    }


    @Override
    public boolean getBoolean(Object k) {
        return super.getAndMoveToFirst((UUID) k);
    }

    @Override
    public boolean put(UUID uuid, boolean v) {
        if (size() >= capacity) {
            UUID lastKey = lastKey();
            if (!lastKey.equals(uuid)) {
                removeBoolean(lastKey);
            }
        }
        return this.putAndMoveToFirst(uuid, v);
    }
}
