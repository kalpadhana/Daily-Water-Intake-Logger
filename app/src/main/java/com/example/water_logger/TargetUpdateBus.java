 package com.example.water_logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class TargetUpdateBus {
    private TargetUpdateBus() {}

    public interface Listener {
        void onTargetUpdated(int userId, int newTarget);
    }

    private static final List<Listener> listeners = new CopyOnWriteArrayList<>();

    public static void register(Listener l) {
        if (l != null) listeners.add(l);
    }

    public static void unregister(Listener l) {
        if (l != null) listeners.remove(l);
    }

    public static void notifyTargetUpdated(int userId, int newTarget) {
        for (Listener l : listeners) {
            try {
                l.onTargetUpdated(userId, newTarget);
            } catch (Exception ignored) {
            }
        }
    }
}

