package com.citylogic.domain.core;

import java.util.Objects;

/**
 * Immutable DTO capturing the point-in-time state of the municipality.
 */
public final class CitySnapshot {
    private final double budget;
    private final double pollution;
    private final int population;
    private final double happiness;
    private final int tickCount;
    private final int energyProduced;
    private final int energyConsumed;

    public CitySnapshot(double budget, double pollution, int population, double happiness, int tickCount, int energyProduced, int energyConsumed) {
        this.budget = budget;
        this.pollution = Math.max(0.0, pollution);
        this.population = Math.max(0, population);
        this.happiness = Math.max(0.0, Math.min(100.0, happiness));
        this.tickCount = Math.max(0, tickCount);
        this.energyProduced = Math.max(0, energyProduced);
        this.energyConsumed = Math.max(0, energyConsumed);
    }

    public CitySnapshot(double budget, double pollution, int population, double happiness, int tickCount) {
        this(budget, pollution, population, happiness, tickCount, 0, 0);
    }

    public double getBudget() {
        return budget;
    }

    public double getPollution() {
        return pollution;
    }

    public int getPopulation() {
        return population;
    }

    public double getHappiness() {
        return happiness;
    }

    public int getTickCount() {
        return tickCount;
    }

    public int getEnergyProduced() {
        return energyProduced;
    }

    public int getEnergyConsumed() {
        return energyConsumed;
    }

    public int getEnergySurplus() {
        return energyProduced - energyConsumed;
    }

    public boolean isEnergyDeficit() {
        return energyConsumed > energyProduced;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CitySnapshot)) return false;
        CitySnapshot that = (CitySnapshot) o;
        return Double.compare(that.budget, budget) == 0 &&
               Double.compare(that.pollution, pollution) == 0 &&
               population == that.population &&
               Double.compare(that.happiness, happiness) == 0 &&
               tickCount == that.tickCount &&
               energyProduced == that.energyProduced &&
               energyConsumed == that.energyConsumed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(budget, pollution, population, happiness, tickCount, energyProduced, energyConsumed);
    }

    @Override
    public String toString() {
        return String.format("CitySnapshot[tick=%d, budget=$%.1f, pollution=%.1f, pop=%d, happy=%.1f%%, energy=%d/%d kW]",
            tickCount, budget, pollution, population, happiness, energyProduced, energyConsumed);
    }
}
