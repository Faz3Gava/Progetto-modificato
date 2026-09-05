package com.citylogic.application;

import com.citylogic.application.policies.IPolicyStrategy;
import com.citylogic.domain.buildings.BuildingDescription;
import com.citylogic.domain.buildings.IBuildingState;
import com.citylogic.domain.core.CitySnapshot;
import com.citylogic.domain.map.IGridCommandPort;
import com.citylogic.domain.map.IGridReadPort;
import com.citylogic.domain.tick.SimulationEngine;
import com.citylogic.domain.tick.SimulationException;

import java.util.Optional;

/**
 * Orchestrating application service facade handling presentation requests,
 * construction, demolition, policy toggle, and time advancement.
 */
public class GameEngine {
    private final IGridCommandPort mapCommander;
    private final IGridReadPort gridReader;
    private final SimulationEngine simulationEngine;
    private final BuildingCatalog catalog;
    private final PlacementValidator validator;

    public GameEngine(
            IGridCommandPort mapCommander,
            IGridReadPort gridReader,
            SimulationEngine simulationEngine,
            BuildingCatalog catalog,
            PlacementValidator validator) {
        if (mapCommander == null) throw new IllegalArgumentException("mapCommander cannot be null");
        if (gridReader == null) throw new IllegalArgumentException("gridReader cannot be null");
        if (simulationEngine == null) throw new IllegalArgumentException("simulationEngine cannot be null");
        if (catalog == null) throw new IllegalArgumentException("catalog cannot be null");
        if (validator == null) throw new IllegalArgumentException("validator cannot be null");

        this.mapCommander = mapCommander;
        this.gridReader = gridReader;
        this.simulationEngine = simulationEngine;
        this.catalog = catalog;
        this.validator = validator;
    }

    public IGridReadPort getGridReader() {
        return gridReader;
    }

    public IGridCommandPort getMapCommander() {
        return mapCommander;
    }

    public SimulationEngine getSimulationEngine() {
        return simulationEngine;
    }

    public BuildingCatalog getCatalog() {
        return catalog;
    }

    public CitySnapshot getCitySnapshot() {
        return simulationEngine.getCurrentSnapshot();
    }

    public boolean isOccupied(int x, int y) {
        return gridReader.isOccupied(x, y);
    }

    public boolean IsOccupied(int x, int y) {
        return isOccupied(x, y);
    }

    public IBuildingState getBuilding(int x, int y) {
        return gridReader.getBuilding(x, y);
    }

    public IBuildingState GetBuilding(int x, int y) {
        return getBuilding(x, y);
    }

    public PlacementResult placeBuilding(int x, int y, String typeId, boolean enforceBudget) {
        if (typeId == null || typeId.trim().isEmpty()) {
            return PlacementResult.failure("Invalid building type ID");
        }

        BuildingDescription description = catalog.getByTypeId(typeId);
        if (description == null) {
            return PlacementResult.failure("Building type '" + typeId + "' not found in catalog");
        }

        if (!validator.canPlace(x, y, typeId, gridReader)) {
            return PlacementResult.failure("The footprint is obstructed or exceeds city borders");
        }

        CitySnapshot currentSnapshot = simulationEngine.getCurrentSnapshot();
        if (enforceBudget && currentSnapshot.getBudget() < description.getConstructionCost()) {
            return PlacementResult.failure(String.format(
                "Insufficient municipal funds: costs $%.0f, but city has $%.0f",
                description.getConstructionCost(), currentSnapshot.getBudget()
            ));
        }

        // Place on grid
        IBuildingState building = mapCommander.constructBuildingAt(x, y, description);

        // Deduct construction cost from city aggregate
        if (enforceBudget && description.getConstructionCost() > 0) {
            CitySnapshot updatedSnapshot = new CitySnapshot(
                currentSnapshot.getBudget() - description.getConstructionCost(),
                currentSnapshot.getPollution(),
                currentSnapshot.getPopulation(),
                currentSnapshot.getHappiness(),
                currentSnapshot.getTickCount()
            );
            simulationEngine.loadState(updatedSnapshot);
        }

        return PlacementResult.success(building);
    }

    public PlacementResult placeBuilding(int x, int y, String typeId) {
        return placeBuilding(x, y, typeId, true);
    }

    public DemolitionResult demolishBuilding(int x, int y, double refundPercent) {
        IBuildingState removed = mapCommander.removeBuildingAt(x, y);
        if (removed == null) {
            return DemolitionResult.failure("No building found at coordinates (" + x + ", " + y + ")");
        }

        // Salvage refund
        if (refundPercent > 0) {
            double refund = Math.floor(removed.getDescription().getConstructionCost() * refundPercent);
            if (refund > 0) {
                CitySnapshot currentSnapshot = simulationEngine.getCurrentSnapshot();
                CitySnapshot updatedSnapshot = new CitySnapshot(
                    currentSnapshot.getBudget() + refund,
                    currentSnapshot.getPollution(),
                    currentSnapshot.getPopulation(),
                    currentSnapshot.getHappiness(),
                    currentSnapshot.getTickCount()
                );
                simulationEngine.loadState(updatedSnapshot);
            }
        }

        return DemolitionResult.success(removed);
    }

    public DemolitionResult demolishBuilding(int x, int y) {
        return demolishBuilding(x, y, 0.5);
    }

    public SimulationEngine.TickResult advanceTime() throws SimulationException {
        return simulationEngine.advanceTick();
    }

    public void setCityPolicy(IPolicyStrategy policy) {
        simulationEngine.activatePolicy(policy);
    }

    public void clearCityPolicy(String policyName) {
        simulationEngine.deactivatePolicy(policyName);
    }

    public boolean toggleBuildingPower(String buildingId) {
        Optional<IBuildingState> opt = gridReader.getBuildingById(buildingId);
        if (opt.isPresent()) {
            IBuildingState building = opt.get();
            building.setPowered(!building.isPowered());
            return true;
        }
        return false;
    }

    public static class PlacementResult {
        private final boolean successful;
        private final IBuildingState building;
        private final String errorMessage;

        private PlacementResult(boolean successful, IBuildingState building, String errorMessage) {
            this.successful = successful;
            this.building = building;
            this.errorMessage = errorMessage;
        }

        public static PlacementResult success(IBuildingState building) {
            return new PlacementResult(true, building, null);
        }

        public static PlacementResult failure(String errorMessage) {
            return new PlacementResult(false, null, errorMessage);
        }

        public boolean isSuccessful() {
            return successful;
        }

        public IBuildingState getBuilding() {
            return building;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public static class DemolitionResult {
        private final boolean successful;
        private final IBuildingState building;
        private final String errorMessage;

        private DemolitionResult(boolean successful, IBuildingState building, String errorMessage) {
            this.successful = successful;
            this.building = building;
            this.errorMessage = errorMessage;
        }

        public static DemolitionResult success(IBuildingState building) {
            return new DemolitionResult(true, building, null);
        }

        public static DemolitionResult failure(String errorMessage) {
            return new DemolitionResult(false, null, errorMessage);
        }

        public boolean isSuccessful() {
            return successful;
        }

        public IBuildingState getBuilding() {
            return building;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
