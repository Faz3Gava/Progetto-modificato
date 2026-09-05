package com.citylogic.application.policies;

import com.citylogic.domain.buildings.BuildingDescription;
import com.citylogic.domain.buildings.IBuildingState;
import com.citylogic.domain.core.ResourceDelta;
import com.citylogic.domain.map.IGridReadPort;

/**
 * Environmental Tax (Tassa Ambientale)
 * Levies municipal fees on polluting industrial facilities (+revenue, -happiness).
 */
public class EnvironmentalTaxPolicy implements IPolicyStrategy {
    @Override
    public String getId() {
        return "policy_environmental_tax";
    }

    @Override
    public String getName() {
        return "Environmental Tax";
    }

    @Override
    public String getDescription() {
        return "Levies a $60 municipal fee per active factory, generating revenue while slightly reducing industrial happiness (-1.5%).";
    }

    @Override
    public ResourceDelta calculateModifier(IBuildingState building, IGridReadPort grid) {
        BuildingDescription desc = building.getDescription();
        if (desc.getCategory() == BuildingDescription.Category.INDUSTRIAL || 
            desc.getName().toLowerCase().contains("factory")) {
            return new ResourceDelta(60.0, 0.0, 0, -1.5);
        }
        return ResourceDelta.zero();
    }
}
