package com.citylogic.application;

import com.citylogic.domain.core.CitySnapshot;
import com.citylogic.domain.core.ResourceDelta;

/**
 * Observer interface for reacting to committed city state updates.
 */
public interface ICityObserver {
    void onMetricsChanged(CitySnapshot snapshot, ResourceDelta delta);
}
