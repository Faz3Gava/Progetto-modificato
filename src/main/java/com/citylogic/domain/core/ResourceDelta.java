package com.citylogic.domain.core;

import java.util.Objects;

/**
 * Immutable value object representing transactional changes to city metrics in a single tick.
 */
public final class ResourceDelta {
    private final double budgetDelta;
    private final double pollutionDelta;
    private final int populationDelta;
    private final double happinessDelta;

    public ResourceDelta(double budgetDelta, double pollutionDelta, int populationDelta, double happinessDelta) {
        this.budgetDelta = budgetDelta;
        this.pollutionDelta = pollutionDelta;
        this.populationDelta = populationDelta;
        this.happinessDelta = happinessDelta;
    }

    public static ResourceDelta zero() {
        return new ResourceDelta(0.0, 0.0, 0, 0.0);
    }

    public double getBudgetDelta() {
        return budgetDelta;
    }

    public double getPollutionDelta() {
        return pollutionDelta;
    }

    public int getPopulationDelta() {
        return populationDelta;
    }

    public double getHappinessDelta() {
        return happinessDelta;
    }

    public ResourceDelta merge(ResourceDelta other) {
        if (other == null) {
            return this;
        }
        return new ResourceDelta(
            this.budgetDelta + other.budgetDelta,
            this.pollutionDelta + other.pollutionDelta,
            this.populationDelta + other.populationDelta,
            this.happinessDelta + other.happinessDelta
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceDelta)) return false;
        ResourceDelta that = (ResourceDelta) o;
        return Double.compare(that.budgetDelta, budgetDelta) == 0 &&
               Double.compare(that.pollutionDelta, pollutionDelta) == 0 &&
               populationDelta == that.populationDelta &&
               Double.compare(that.happinessDelta, happinessDelta) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(budgetDelta, pollutionDelta, populationDelta, happinessDelta);
    }

    @Override
    public String toString() {
        return String.format("ResourceDelta[budget=%+.1f, pollution=%+.1f, pop=%+d, happy=%+.1f%%]",
            budgetDelta, pollutionDelta, populationDelta, happinessDelta);
    }
}
