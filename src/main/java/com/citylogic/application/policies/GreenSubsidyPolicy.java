package com.citylogic.application.policies;

import com.citylogic.domain.buildings.BuildingDescription;
import com.citylogic.domain.buildings.IBuildingState;
import com.citylogic.domain.core.ResourceDelta;
import com.citylogic.domain.map.IGridReadPort;

/**
 * Green Subsidy (Sussidio Verde)
 * Allocates municipal funds to parks for botanical care and smog mitigation.
 */
public class GreenSubsidyPolicy implements IPolicyStrategy {
    @Override
    public String getId() {
        return "policy_green_subsidy";
    }

    @Override
    public String getName() {
        return "Green Subsidy";
    }

    @Override
    public String getDescription() {
        return "Allocates $15 per park for landscaping and botanical care, gaining +2.5% happiness and cleansing 2.0 pollution.";
    }

    @Override
    public ResourceDelta calculateModifier(IBuildingState building, IGridReadPort grid) {
        BuildingDescription desc = building.getDescription();
        if (desc.getCategory() == BuildingDescription.Category.CIVIC || 
            desc.getName().toLowerCase().contains("park")) {
            return new ResourceDelta(-15.0, -2.0, 0, 2.5);
        }
        return ResourceDelta.zero();
    }
}
