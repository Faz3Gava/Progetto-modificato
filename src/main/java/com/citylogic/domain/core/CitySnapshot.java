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

    public CitySnapshot(double budget, double pollution, int population, double happiness, int tickCount) {
        this.budget = budget;
        this.pollution = Math.max(0.0, pollution);
        this.population = Math.max(0, population);
        this.happiness = Math.max(0.0, Math.min(100.0, happiness));
        this.tickCount = Math.max(0, tickCount);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CitySnapshot)) return false;
        CitySnapshot that = (CitySnapshot) o;
        return Double.compare(that.budget, budget) == 0 &&
               Double.compare(that.pollution, pollution) == 0 &&
               population == that.population &&
               Double.compare(that.happiness, happiness) == 0 &&
               tickCount == that.tickCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(budget, pollution, population, happiness, tickCount);
    }

    @Override
    public String toString() {
        return String.format("CitySnapshot[tick=%d, budget=$%.1f, pollution=%.1f, pop=%d, happy=%.1f%%]",
            tickCount, budget, pollution, population, happiness);
    }
}
