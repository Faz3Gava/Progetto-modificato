package com.citylogic.application;

import com.citylogic.domain.buildings.BuildingDescription;
import com.citylogic.domain.core.Dimension;
import com.citylogic.domain.core.ResourceDelta;

/**
 * Standard registry provider initializing default urban building archetypes.
 */
public class ApplicationBuildingDescriptionProvider {
    public static void initDefaultCatalog(BuildingCatalog catalog) {
        if (catalog == null) {
            throw new IllegalArgumentException("Catalog cannot be null");
        }

        // 1. House (Residential)
        // Accommodates 4 citizens, requires nominal maintenance ($1)
        BuildingDescription house = new BuildingDescription(
            "House",
            100.0,
            1.0,
            new Dimension(1, 1),
            new ResourceDelta(0.0, 0.0, 4, 0.0),
            BuildingDescription.Category.RESIDENTIAL,
            "Home",
            "Residential housing providing living space for 4 citizens."
        );
        catalog.register(house);

        // 2. Factory (Industrial)
        // High budget output (+150), high pollution output (+10), 2x2 footprint
        BuildingDescription factory = new BuildingDescription(
            "Factory",
            1000.0,
            5.0,
            new Dimension(2, 2),
            new ResourceDelta(150.0, 10.0, 0, 0.0),
            BuildingDescription.Category.INDUSTRIAL,
            "Factory",
            "Heavy manufacturing facility generating strong tax revenue at the cost of pollution."
        );
        catalog.register(factory);

        // 3. Park (Civic)
        // Zero maintenance, elevates citizen happiness (+2.0%)
        BuildingDescription park = new BuildingDescription(
            "Park",
            150.0,
            0.0,
            new Dimension(1, 1),
            new ResourceDelta(0.0, 0.0, 0, 2.0),
            BuildingDescription.Category.CIVIC,
            "Trees",
            "Recreational public park that elevates citizen happiness and well-being."
        );
        catalog.register(park);

        // 4. Commercial Hub (Commercial)
        // Moderate revenue (+45), minor pollution (+1), modest happiness (+0.5%)
        BuildingDescription commercial = new BuildingDescription(
            "Commercial Hub",
            350.0,
            2.0,
            new Dimension(1, 1),
            new ResourceDelta(45.0, 1.0, 0, 0.5),
            BuildingDescription.Category.COMMERCIAL,
            "Store",
            "Retail and service district contributing steady tax revenue and urban vibrancy."
        );
        catalog.register(commercial);

        // 5. Solar Plant (Utility)
        // Clean renewable energy: actively reduces smog (-4.0), requires maintenance (-$25), 2x2 footprint
        BuildingDescription solarPlant = new BuildingDescription(
            "Solar Plant",
            750.0,
            4.0,
            new Dimension(2, 2),
            new ResourceDelta(-25.0, -4.0, 0, 1.0),
            BuildingDescription.Category.UTILITY,
            "Sun",
            "Renewable photovoltaic solar plant providing clean electricity and mitigating smog."
        );
        catalog.register(solarPlant);
    }
}
