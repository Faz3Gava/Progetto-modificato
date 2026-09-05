package com.citylogic.application.policies;

import com.citylogic.domain.buildings.BuildingDescription;
import com.citylogic.domain.buildings.IBuildingState;
import com.citylogic.domain.core.ResourceDelta;
import com.citylogic.domain.map.IGridReadPort;

import java.util.List;

/**
 * Eco-Buffer Zone Initiative
 * Spatial policy: Industrial factories buffered by parks within radius 2 gain eco-compliance bonuses.
 */
public class EcoBufferZonePolicy implements IPolicyStrategy {
    @Override
    public String getId() {
        return "policy_eco_buffer";
    }

    @Override
    public String getName() {
        return "Eco-Buffer Initiative";
    }

    @Override
    public String getDescription() {
        return "Factories located near a park (radius 2) filter emissions: reduces pollution by -4.0 per adjacent park.";
    }

    @Override
    public ResourceDelta calculateModifier(IBuildingState building, IGridReadPort grid) {
        BuildingDescription desc = building.getDescription();
        if (desc.getCategory() == BuildingDescription.Category.INDUSTRIAL || 
            desc.getName().toLowerCase().contains("factory")) {
            List<IBuildingState> adjacent = grid.getAdjacentBuildings(building.getId(), 2);
            long nearbyParks = adjacent.stream()
                .filter(b -> b.getDescription().getCategory() == BuildingDescription.Category.CIVIC ||
                             b.getDescription().getName().toLowerCase().contains("park"))
                .count();

            if (nearbyParks > 0) {
                return new ResourceDelta(0.0, -4.0 * nearbyParks, 0, 1.0 * nearbyParks);
            }
        }
        return ResourceDelta.zero();
    }
}
