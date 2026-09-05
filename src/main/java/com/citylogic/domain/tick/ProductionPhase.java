package com.citylogic.domain.tick;

import com.citylogic.domain.buildings.IBuildingState;
import com.citylogic.domain.core.CitySnapshot;
import com.citylogic.domain.core.ResourceDelta;
import com.citylogic.domain.map.IGridReadPort;

/**
 * Production Phase: aggregates base economic & demographic production from
 * all active and powered buildings in the city.
 */
public class ProductionPhase implements ITickPhase {
    @Override
    public ResourceDelta execute(CitySnapshot snapshot, IGridReadPort grid) {
        ResourceDelta total = ResourceDelta.zero();

        for (IBuildingState building : grid.getAllBuildings()) {
            if (building.isPowered()) {
                total = total.merge(building.getBaseProduction());
            }
        }

        return total;
    }
}
