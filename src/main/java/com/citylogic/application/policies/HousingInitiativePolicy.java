package com.citylogic.application.policies;

import com.citylogic.domain.buildings.BuildingDescription;
import com.citylogic.domain.buildings.IBuildingState;
import com.citylogic.domain.core.ResourceDelta;
import com.citylogic.domain.map.IGridReadPort;

/**
 * Housing Initiative Policy
 * Subsidizes residential zones to attract new citizens faster.
 */
public class HousingInitiativePolicy implements IPolicyStrategy {
    @Override
    public String getId() {
        return "policy_housing_initiative";
    }

    @Override
    public String getName() {
        return "Housing Development Grant";
    }

    @Override
    public String getDescription() {
        return "Invests $8 per house in civic amenities: accelerates population influx (+2 citizens per house).";
    }

    @Override
    public ResourceDelta calculateModifier(IBuildingState building, IGridReadPort grid) {
        BuildingDescription desc = building.getDescription();
        if (desc.getCategory() == BuildingDescription.Category.RESIDENTIAL || 
            desc.getName().toLowerCase().contains("house")) {
            return new ResourceDelta(-8.0, 0.0, 2, 1.0);
        }
        return ResourceDelta.zero();
    }
}
