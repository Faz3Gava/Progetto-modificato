package com.citylogic.application;

import com.citylogic.domain.core.CitySnapshot;
import com.citylogic.domain.core.ResourceDelta;

/**
 * Publisher interface for broadcasting simulation state changes to observers.
 */
public interface ICityEventPublisher {
    void publish(CitySnapshot snapshot, ResourceDelta delta);
    void subscribe(ICityObserver observer);
    void unsubscribe(ICityObserver observer);
}
