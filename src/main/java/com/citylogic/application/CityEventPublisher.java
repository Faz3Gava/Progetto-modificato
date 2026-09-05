package com.citylogic.application;

import com.citylogic.domain.core.CitySnapshot;
import com.citylogic.domain.core.ResourceDelta;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe event publisher implementation for city metrics observers.
 */
public class CityEventPublisher implements ICityEventPublisher {
    private final List<ICityObserver> observers = new CopyOnWriteArrayList<>();

    @Override
    public void publish(CitySnapshot snapshot, ResourceDelta delta) {
        if (snapshot == null) return;
        ResourceDelta d = delta != null ? delta : ResourceDelta.zero();
        for (ICityObserver observer : observers) {
            try {
                observer.onMetricsChanged(snapshot, d);
            } catch (Exception e) {
                System.err.println("Error in city observer: " + e.getMessage());
            }
        }
    }

    @Override
    public void subscribe(ICityObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void unsubscribe(ICityObserver observer) {
        if (observer != null) {
            observers.remove(observer);
        }
    }
}
