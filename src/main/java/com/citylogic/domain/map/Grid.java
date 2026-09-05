package com.citylogic.domain.map;

import com.citylogic.domain.buildings.BuildingDescription;
import com.citylogic.domain.buildings.BuildingFactory;
import com.citylogic.domain.buildings.BuildingInstance;
import com.citylogic.domain.buildings.IBuildingState;
import com.citylogic.domain.core.Dimension;
import com.citylogic.domain.core.Point;

import java.util.*;

/**
 * Concrete spatial city grid implementing both query port (IGridReadPort)
 * and command port (IGridCommandPort).
 */
public class Grid implements IGridReadPort, IGridCommandPort {
    private final Dimension dimensions;
    private final Cell[][] map;
    private final BuildingFactory factory;
    private final Map<String, BuildingInstance> activeBuildings = new LinkedHashMap<>();

    public Grid(Dimension dimensions, BuildingFactory factory) {
        if (dimensions == null || dimensions.getWidth() <= 0 || dimensions.getHeight() <= 0) {
            throw new IllegalArgumentException("Valid non-null grid dimensions must be provided");
        }
        this.dimensions = dimensions;
        this.factory = factory != null ? factory : new BuildingFactory();

        this.map = new Cell[dimensions.getWidth()][dimensions.getHeight()];
        for (int x = 0; x < dimensions.getWidth(); x++) {
            for (int y = 0; y < dimensions.getHeight(); y++) {
                this.map[x][y] = new Cell(x, y);
            }
        }
    }

    public Grid(Dimension dimensions) {
        this(dimensions, new BuildingFactory());
    }

    @Override
    public Dimension getDimensions() {
        return dimensions;
    }

    public Cell getCell(int x, int y) {
        if (!isWithinBounds(x, y)) {
            return null;
        }
        return map[x][y];
    }

    public Cell getCell(Point point) {
        if (point == null) return null;
        return getCell(point.getX(), point.getY());
    }

    @Override
    public boolean isOccupied(int x, int y) {
        if (!isWithinBounds(x, y)) {
            return false;
        }
        return map[x][y].isOccupied();
    }

    @Override
    public BuildingInstance getBuilding(int x, int y) {
        if (!isWithinBounds(x, y)) {
            return null;
        }
        return map[x][y].getBuilding();
    }

    @Override
    public BuildingInstance GetBuilding(int x, int y) {
        return getBuilding(x, y);
    }

    @Override
    public BuildingInstance getBuilding(Point point) {
        if (point == null) return null;
        return getBuilding(point.getX(), point.getY());
    }

    @Override
    public BuildingInstance GetBuilding(Point point) {
        return getBuilding(point);
    }

    @Override
    public String getTerrainAt(int x, int y) {
        if (!isWithinBounds(x, y)) {
            return null;
        }
        return "grass";
    }

    @Override
    public Optional<IBuildingState> getBuildingById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(activeBuildings.get(id));
    }

    @Override
    public List<IBuildingState> getAllBuildings() {
        return new ArrayList<>(activeBuildings.values());
    }

    @Override
    public List<IBuildingState> getAdjacentBuildings(String id, int radius) {
        if (id == null || radius < 0) {
            return Collections.emptyList();
        }
        BuildingInstance origin = activeBuildings.get(id);
        if (origin == null) {
            return Collections.emptyList();
        }

        Point op = origin.getPosition();
        List<IBuildingState> adjacent = new ArrayList<>();

        for (BuildingInstance building : activeBuildings.values()) {
            if (building.getId().equals(id)) {
                continue;
            }
            Point bp = building.getPosition();
            int dx = Math.abs(bp.getX() - op.getX());
            int dy = Math.abs(bp.getY() - op.getY());
            if (Math.max(dx, dy) <= radius) {
                adjacent.add(building);
            }
        }

        return adjacent;
    }

    @Override
    public boolean isAreaFree(int x, int y, Dimension footprint) {
        if (footprint == null) return false;
        if (!isWithinBounds(x, y)) return false;
        if (!isWithinBounds(x + footprint.getWidth() - 1, y + footprint.getHeight() - 1)) {
            return false;
        }

        for (int ox = 0; ox < footprint.getWidth(); ox++) {
            for (int oy = 0; oy < footprint.getHeight(); oy++) {
                Cell cell = map[x + ox][y + oy];
                if (cell.isOccupied()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isWithinBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < dimensions.getWidth() && y < dimensions.getHeight();
    }

    @Override
    public synchronized BuildingInstance constructBuildingAt(int x, int y, BuildingDescription desc) {
        if (desc == null) {
            throw new IllegalArgumentException("BuildingDescription cannot be null");
        }
        if (!isAreaFree(x, y, desc.getFootprint())) {
            throw new IllegalStateException("Cannot construct " + desc.getName() + " at (" + x + "," + y + "): area occupied or out of bounds");
        }

        BuildingInstance building = factory.createBuilding(desc, x, y);
        Dimension footprint = desc.getFootprint();

        for (int ox = 0; ox < footprint.getWidth(); ox++) {
            for (int oy = 0; oy < footprint.getHeight(); oy++) {
                map[x + ox][y + oy].setBuilding(building);
            }
        }

        activeBuildings.put(building.getId(), building);
        return building;
    }

    @Override
    public synchronized BuildingInstance removeBuildingAt(int x, int y) {
        if (!isWithinBounds(x, y)) {
            return null;
        }
        Cell cell = map[x][y];
        if (!cell.isOccupied()) {
            return null;
        }

        BuildingInstance building = cell.getBuilding();
        Dimension footprint = building.getDescription().getFootprint();
        Point origin = building.getPosition();

        for (int ox = 0; ox < footprint.getWidth(); ox++) {
            for (int oy = 0; oy < footprint.getHeight(); oy++) {
                int cx = origin.getX() + ox;
                int cy = origin.getY() + oy;
                if (isWithinBounds(cx, cy)) {
                    Cell c = map[cx][cy];
                    if (c.getBuilding() == building) {
                        c.clear();
                    }
                }
            }
        }

        activeBuildings.remove(building.getId());
        return building;
    }
}
