package com.citylogic.application;

import com.citylogic.domain.buildings.BuildingDescription;
import com.citylogic.domain.map.IGridReadPort;

/**
 * Domain service validating spatial layout, boundary constraints, and cell availability.
 */
public class PlacementValidator {
    private final BuildingCatalog catalog;

    public PlacementValidator(BuildingCatalog catalog) {
        if (catalog == null) {
            throw new IllegalArgumentException("Catalog cannot be null");
        }
        this.catalog = catalog;
    }

    public boolean canPlace(int x, int y, String typeId, IGridReadPort grid) {
        if (grid == null || typeId == null) {
            return false;
        }
        BuildingDescription description = catalog.getByTypeId(typeId);
        if (description == null) {
            return false;
        }
        return grid.isAreaFree(x, y, description.getFootprint());
    }

    public boolean canPlaceDescription(int x, int y, BuildingDescription description, IGridReadPort grid) {
        if (description == null || grid == null) {
            return false;
        }
        return grid.isAreaFree(x, y, description.getFootprint());
    }
}
