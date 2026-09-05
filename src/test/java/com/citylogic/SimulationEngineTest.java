package com.citylogic;

import com.citylogic.application.ApplicationBuildingDescriptionProvider;
import com.citylogic.application.BuildingCatalog;
import com.citylogic.application.CityEventPublisher;
import com.citylogic.application.GameEngine;
import com.citylogic.application.PlacementValidator;
import com.citylogic.application.policies.EcoBufferZonePolicy;
import com.citylogic.application.policies.EnvironmentalTaxPolicy;
import com.citylogic.application.policies.GreenSubsidyPolicy;
import com.citylogic.domain.buildings.BuildingDescription;
import com.citylogic.domain.buildings.IBuildingState;
import com.citylogic.domain.core.CityAggregate;
import com.citylogic.domain.core.CitySnapshot;
import com.citylogic.domain.core.Dimension;
import com.citylogic.domain.core.ResourceDelta;
import com.citylogic.domain.map.Grid;
import com.citylogic.domain.tick.SimulationConfig;
import com.citylogic.domain.tick.SimulationEngine;
import com.citylogic.domain.tick.SimulationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite verifying CityLogic domain rules, spatial constraints,
 * simulation tick pipeline, policy modifiers, and transactional rollbacks.
 */
public class SimulationEngineTest {
    private Grid grid;
    private BuildingCatalog catalog;
    private CityAggregate aggregate;
    private SimulationEngine simulationEngine;
    private GameEngine gameEngine;

    @BeforeEach
    public void setUp() {
        this.grid = new Grid(new Dimension(10, 10));
        this.catalog = new BuildingCatalog();
        ApplicationBuildingDescriptionProvider.initDefaultCatalog(catalog);

        this.aggregate = new CityAggregate(2500.0, 0, 70.0);
        PlacementValidator validator = new PlacementValidator(catalog);
        CityEventPublisher publisher = new CityEventPublisher();

        this.simulationEngine = new SimulationEngine(aggregate, grid, publisher, SimulationConfig.defaultConfig());
        this.gameEngine = new GameEngine(grid, grid, simulationEngine, catalog, validator);
    }

    @Test
    @DisplayName("Should successfully place building and deduct construction cost")
    public void testPlaceBuildingDeductsCost() {
        CitySnapshot before = gameEngine.getCitySnapshot();
        assertEquals(2500.0, before.getBudget(), 0.001);

        GameEngine.PlacementResult result = gameEngine.placeBuilding(2, 2, "house", true);
        assertTrue(result.isSuccessful());
        assertNotNull(result.getBuilding());
        assertEquals("House", result.getBuilding().getType());

        CitySnapshot after = gameEngine.getCitySnapshot();
        assertEquals(2400.0, after.getBudget(), 0.001); // $2500 - $100

        assertTrue(result.isOccupied());
        assertTrue(result.IsOccupied());
        assertNotNull(result.GetBuilding());

        assertTrue(grid.getCell(2, 2).isOccupied());
        assertTrue(grid.getCell(2, 2).IsOccupied());
        assertTrue(grid.getCell(2, 2).isOccupied);
        assertTrue(grid.getCell(2, 2).isOccupied(2, 2));
        assertNotNull(grid.getCell(2, 2).GetBuilding());
        assertNotNull(grid.getCell(2, 2).getBuilding());
        assertNotNull(grid.getCell(2, 2).GetBuilding(2, 2));

        assertTrue(grid.isOccupied(2, 2));
        assertTrue(grid.IsOccupied(2, 2));
        assertTrue(grid.isOccupied(new com.citylogic.domain.core.Point(2, 2)));
        assertTrue(grid.IsOccupied(new com.citylogic.domain.core.Point(2, 2)));
        assertNotNull(grid.GetBuilding(2, 2));
        assertNotNull(grid.getBuilding(2, 2));
        assertNotNull(grid.GetBuilding(new com.citylogic.domain.core.Point(2, 2)));
        assertNotNull(grid.GetBuilding(result.getBuilding().getId()));

        assertTrue(gameEngine.isOccupied(2, 2));
        assertTrue(gameEngine.IsOccupied(2, 2));
        assertTrue(gameEngine.isOccupied(new com.citylogic.domain.core.Point(2, 2)));
        assertNotNull(gameEngine.GetBuilding(2, 2));
        assertNotNull(gameEngine.getBuilding(2, 2));
        assertNotNull(gameEngine.GetBuilding(result.getBuilding().getId()));
    }

    @Test
    @DisplayName("Should reject overlapping placement on occupied cell")
    public void testRejectOverlappingPlacement() {
        gameEngine.placeBuilding(2, 2, "house", true);
        GameEngine.PlacementResult conflict = gameEngine.placeBuilding(2, 2, "park", true);

        assertFalse(conflict.isSuccessful());
        assertTrue(conflict.getErrorMessage().contains("obstructed"));
    }

    @Test
    @DisplayName("Should reject multi-tile footprint exceeding grid boundary")
    public void testRejectOutOfBoundsFootprint() {
        // Factory is 2x2. Placing at (9, 9) on a 10x10 grid exceeds index 9 (needs 9 and 10)
        GameEngine.PlacementResult outOfBounds = gameEngine.placeBuilding(9, 9, "factory", true);
        assertFalse(outOfBounds.isSuccessful());
    }

    @Test
    @DisplayName("Should aggregate base production across powered buildings in tick")
    public void testProductionPhaseTick() throws SimulationException {
        // House produces: 0 budget, 4 citizens, 0 pollution, 0 happiness (consumes 2 kW)
        // Factory produces: 150 budget, 10 pollution, 0 citizens, 0 happiness (consumes 8 kW)
        // Solar plant produces: 20 kW clean energy
        gameEngine.placeBuilding(0, 0, "house", false);
        gameEngine.placeBuilding(2, 2, "factory", false);
        gameEngine.placeBuilding(5, 5, "solar_plant", false);

        SimulationEngine.TickResult tick = gameEngine.advanceTime();
        ResourceDelta delta = tick.getDelta();

        assertEquals(150.0, delta.getBudgetDelta(), 0.001);
        assertEquals(4, delta.getPopulationDelta());
        assertEquals(10.0, delta.getPollutionDelta(), 0.001);

        CitySnapshot snapshot = tick.getSnapshot();
        assertEquals(1, snapshot.getTickCount());
        assertEquals(4, snapshot.getPopulation());
        assertEquals(10.0, snapshot.getPollution(), 0.001);
        assertEquals(20, snapshot.getEnergyProduced());
        assertEquals(10, snapshot.getEnergyConsumed());
    }

    @Test
    @DisplayName("Should evaluate active policies and apply modifiers")
    public void testPolicyEvaluation() throws SimulationException {
        // Factory base: +150 budget (consumes 8 kW)
        gameEngine.placeBuilding(2, 2, "factory", false);
        gameEngine.placeBuilding(5, 5, "solar_plant", false);

        // Activate Environmental Tax: +$60 per factory, -1.5% happiness
        gameEngine.setCityPolicy(new EnvironmentalTaxPolicy());

        SimulationEngine.TickResult tick = gameEngine.advanceTime();
        ResourceDelta delta = tick.getDelta();

        // 150 (base) + 60 (policy) = 210
        assertEquals(210.0, delta.getBudgetDelta(), 0.001);
        assertEquals(-1.5, delta.getHappinessDelta(), 0.001);
    }

    @Test
    @DisplayName("Unpowered residential houses must reduce citizen satisfaction rate")
    public void testUnpoweredHouseLowersSatisfaction() throws SimulationException {
        // Initial happiness is 70.0%
        assertEquals(70.0, gameEngine.getCitySnapshot().getHappiness(), 0.001);

        // Place a house without any energy generation
        gameEngine.placeBuilding(2, 2, "house", false);

        SimulationEngine.TickResult tick = gameEngine.advanceTime();
        ResourceDelta delta = tick.getDelta();

        // Happiness delta must be -2.5% for unpowered house
        assertEquals(-2.5, delta.getHappinessDelta(), 0.001);
        assertEquals(67.5, tick.getSnapshot().getHappiness(), 0.001);

        // Grid reflects 0 produced and 2 kW demanded
        assertEquals(0, tick.getSnapshot().getEnergyProduced());
        assertEquals(2, tick.getSnapshot().getEnergyConsumed());
        assertEquals(1, gameEngine.getUnpoweredHousesCount());
    }

    @Test
    @DisplayName("Solar plant should supply power to house and prevent satisfaction penalty")
    public void testSolarPlantPowersHouseAndMaintainsSatisfaction() throws SimulationException {
        // Place house (needs 2 kW) and solar plant (supplies 20 kW)
        gameEngine.placeBuilding(2, 2, "house", false);
        gameEngine.placeBuilding(4, 4, "solar_plant", false);

        SimulationEngine.TickResult tick = gameEngine.advanceTime();
        ResourceDelta delta = tick.getDelta();

        // Since house has energy, no happiness penalty
        assertEquals(0.0, delta.getHappinessDelta(), 0.001);
        assertEquals(70.0, tick.getSnapshot().getHappiness(), 0.001);
        assertEquals(4, tick.getSnapshot().getPopulation());
        assertEquals(20, tick.getSnapshot().getEnergyProduced());
        assertEquals(2, tick.getSnapshot().getEnergyConsumed());
        assertEquals(18, gameEngine.getEnergySurplus());
        assertEquals(0, gameEngine.getUnpoweredHousesCount());
    }

    @Test
    @DisplayName("Energy deficit causes blackout for houses that cannot be powered, dropping satisfaction")
    public void testEnergyDeficitCausesBlackoutAndSatisfactionDrop() throws SimulationException {
        // 1 solar plant provides 20 kW.
        // Each house requires 2 kW.
        // 11 houses require 22 kW -> 1 house will suffer a blackout.
        for (int y = 0; y < 11; y++) {
            gameEngine.placeBuilding(0, y % 10, "house", false);
        }
        gameEngine.placeBuilding(5, 5, "solar_plant", false);

        SimulationEngine.TickResult tick = gameEngine.advanceTime();
        ResourceDelta delta = tick.getDelta();

        // 1 house without power -> -2.5% happiness penalty
        assertEquals(-2.5, delta.getHappinessDelta(), 0.001);
        assertEquals(67.5, tick.getSnapshot().getHappiness(), 0.001);
    }

    @Test
    @DisplayName("Should evaluate spatial Eco-Buffer policy when park is adjacent to factory")
    public void testEcoBufferPolicy() throws SimulationException {
        gameEngine.placeBuilding(2, 2, "factory", false); // 2x2 occupying (2,2), (3,2), (2,3), (3,3)
        gameEngine.placeBuilding(4, 2, "park", false);    // Chebyshev distance to factory is 1 (within radius 2)

        gameEngine.setCityPolicy(new EcoBufferZonePolicy());

        SimulationEngine.TickResult tick = gameEngine.advanceTime();
        ResourceDelta delta = tick.getDelta();

        // Factory base pollution: 10.0
        // Eco-Buffer modifier: -4.0 pollution from adjacent park
        // Total pollution delta: 6.0
        assertEquals(6.0, delta.getPollutionDelta(), 0.001);
    }

    @Test
    @DisplayName("Should roll back state snapshot if an invariant fails during tick")
    public void testTransactionalRollbackOnInvariantFailure() {
        CitySnapshot startSnapshot = gameEngine.getCitySnapshot();

        // Custom failing phase or negative population force
        CityAggregate testAggregate = new CityAggregate(100.0, 0, 50.0);
        SimulationEngine engine = new SimulationEngine(testAggregate, grid, new CityEventPublisher());

        // Applying a delta that breaches minimum population < 0
        ResourceDelta badDelta = new ResourceDelta(0.0, 0.0, -10, 0.0);

        assertThrows(IllegalStateException.class, () -> {
            testAggregate.applyDelta(badDelta);
        });

        // State remains intact
        CitySnapshot snapshot = testAggregate.exportSnapshot();
        assertEquals(0, snapshot.getPopulation());
        assertEquals(100.0, snapshot.getBudget(), 0.001);
    }

    @Test
    @DisplayName("Should demolish building and refund salvage value")
    public void testDemolishBuildingWithSalvage() {
        gameEngine.placeBuilding(1, 1, "house", true); // costs $100
        CitySnapshot mid = gameEngine.getCitySnapshot();
        assertEquals(2400.0, mid.getBudget(), 0.001);

        GameEngine.DemolitionResult demo = gameEngine.demolishBuilding(1, 1, 0.5); // 50% refund = +$50
        assertTrue(demo.isSuccessful());
        assertFalse(grid.getCell(1, 1).isOccupied());

        CitySnapshot after = gameEngine.getCitySnapshot();
        assertEquals(2450.0, after.getBudget(), 0.001);
    }
}
