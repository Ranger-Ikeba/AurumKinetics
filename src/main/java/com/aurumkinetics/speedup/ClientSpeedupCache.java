package com.aurumkinetics.speedup;

import java.util.HashMap;
import java.util.Map;

/** На клиенте: containerId → speedupId (или null). */
public class ClientSpeedupCache {
    private static final Map<Integer, String> CACHE = new HashMap<>();

    public static void put(int containerId, String speedupId) {
        if (speedupId == null || speedupId.isEmpty()) {
            CACHE.remove(containerId);
        } else {
            CACHE.put(containerId, speedupId);
        }
    }

    public static SpeedupType get(int containerId) {
        String id = CACHE.get(containerId);
        if (id == null) return null;
        return SpeedupType.byId(id);
    }

    public static void clear(int containerId) {
        CACHE.remove(containerId);
    }
}
