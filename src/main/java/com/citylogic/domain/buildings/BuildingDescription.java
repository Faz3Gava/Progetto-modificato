package com.citylogic.domain.buildings;

import com.citylogic.domain.core.Dimension;
import com.citylogic.domain.core.ResourceDelta;

import java.util.Objects;

/**
 * Immutable metadata and economic specification for a building type (Flyweight pattern).
 */
public final class BuildingDescription {
    public enum Category {
        RESIDENTIAL,
        COMMERCIAL,
        INDUSTRIAL,
        CIVIC,
        UTILITY
    }

    private final String typeId;
    private final String name;
    private final double constructionCost;
    private final double baseMaintenanceCost;
    private final Dimension footprint;
    private final ResourceDelta baseProduction;
    private final Category category;
    private final String icon;
    private final String descriptionText;
    private final int energyProduction;
    private final int energyConsumption;

    public BuildingDescription(
            String name,
            double constructionCost,
            double baseMaintenanceCost,
            Dimension footprint,
            ResourceDelta baseProduction,
            Category category,
            String icon,
            String descriptionText,
            int energyProduction,
            int energyConsumption) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Building name cannot be null or empty");
        }
        if (constructionCost < 0 || baseMaintenanceCost < 0) {
            throw new IllegalArgumentException("Costs cannot be negative");
        }
        if (footprint == null) {
            throw new IllegalArgumentException("Footprint cannot be null");
        }
        this.name = name;
        this.typeId = name.trim().toLowerCase().replaceAll("\\s+", "_");
        this.constructionCost = constructionCost;
        this.baseMaintenanceCost = baseMaintenanceCost;
        this.footprint = footprint;
        this.baseProduction = baseProduction != null ? baseProduction : ResourceDelta.zero();
        this.category = category != null ? category : Category.RESIDENTIAL;
        this.icon = icon != null ? icon : "Building";
        this.descriptionText = descriptionText != null ? descriptionText : "";
        this.energyProduction = Math.max(0, energyProduction);
        this.energyConsumption = Math.max(0, energyConsumption);
    }

    public BuildingDescription(
            String name,
            double constructionCost,
            double baseMaintenanceCost,
            Dimension footprint,
            ResourceDelta baseProduction,
            Category category,
            String icon,
            String descriptionText) {
        this(name, constructionCost, baseMaintenanceCost, footprint, baseProduction, category, icon, descriptionText, 0, 0);
    }

    public BuildingDescription(
            String name,
            double constructionCost,
            double baseMaintenanceCost,
            Dimension footprint,
            ResourceDelta baseProduction) {
        this(name, constructionCost, baseMaintenanceCost, footprint, baseProduction, Category.RESIDENTIAL, "Building", "", 0, 0);
    }

    public String getTypeId() {
        return typeId;
    }

    public String getName() {
        return name;
    }

    public double getConstructionCost() {
        return constructionCost;
    }

    public double getBaseMaintenanceCost() {
        return baseMaintenanceCost;
    }

    public Dimension getFootprint() {
        return footprint;
    }

    public ResourceDelta getBaseProduction() {
        return baseProduction;
    }

    public Category getCategory() {
        return category;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public int getEnergyProduction() {
        return energyProduction;
    }

    public int getEnergyConsumption() {
        return energyConsumption;
    }

    public int getNetEnergy() {
        return energyProduction - energyConsumption;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BuildingDescription)) return false;
        BuildingDescription that = (BuildingDescription) o;
        return Objects.equals(typeId, that.typeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeId);
    }

    @Override
    public String toString() {
        return name + " (" + footprint + ", Cost: $" + (int)constructionCost + ")";
    }
}
