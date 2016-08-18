package centollo.infrastructure.sansorm.framework.introspection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Introspector {
    private static final Map<Class<?>, Introspected> descriptorMap;

    static {
        descriptorMap = new ConcurrentHashMap<Class<?>, Introspected>();
    }

    private Introspector() {
        // private constructor
    }

    public static Introspected getIntrospected(Class<?> clazz) {
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
