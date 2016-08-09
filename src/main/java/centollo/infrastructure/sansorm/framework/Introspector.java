package centollo.infrastructure.sansorm.framework;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class Introspector {
    private static final Map<Class<?>, Introspected> descriptorMap;

    static {
        descriptorMap = new ConcurrentHashMap<>();
    }

    private Introspector() {
    }

    static Introspected getIntrospected(Class<?> clazz) {
        Introspected introspected = descriptorMap.get(clazz);
        if (introspected != null) {
            return introspected;
        }

        // Introspection should only occur once per class.
        synchronized (clazz) {
            // Double check.  This avoids multiple introspections of the same class.
            introspected = descriptorMap.get(clazz);
            if (introspected != null) {
                return introspected;
            }

            introspected = new Introspected(clazz);
            descriptorMap.put(clazz, introspected);
            return introspected;
        }
    }
}
