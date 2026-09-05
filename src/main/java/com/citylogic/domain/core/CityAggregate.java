package com.citylogic.domain.core;

/**
 * Aggregate Root for global city state (DDD pattern).
 * Metric modifications occur purely through applyDelta within transactional ticks.
 */
public class CityAggregate {
    public static final double MIN_BUDGET = -10000.0;
    public static final double MIN_HAPPINESS = 0.0;
    public static final double MAX_HAPPINESS = 100.0;

    private double budget;
    private double pollution;
    private int population;
    private double happiness;
    private int tickCount;

    public CityAggregate(double initialBudget, int initialPopulation, double initialHappiness) {
        this.budget = initialBudget;
        this.pollution = 0.0;
        this.population = initialPopulation;
        this.happiness = clamp(initialHappiness);
        this.tickCount = 0;
        validateInvariants();
    }

    public CityAggregate() {
        this(2500.0, 0, 70.0);
    }

    public synchronized void applyDelta(ResourceDelta delta) {
        if (delta == null) {
            delta = ResourceDelta.zero();
        }

        double nextBudget = this.budget + delta.getBudgetDelta();
        double nextPollution = Math.max(0.0, this.pollution + delta.getPollutionDelta());
        int nextPopulation = this.population + delta.getPopulationDelta();
        double nextHappiness = clamp(this.happiness + delta.getHappinessDelta());

        // Invariant enforcement
        if (nextBudget < MIN_BUDGET) {
            throw new IllegalStateException(String.format(
                "City invariant violated: budget $%.2f is below bankruptcy threshold $%.2f",
                nextBudget, MIN_BUDGET
            ));
        }
        if (nextPopulation < 0) {
            throw new IllegalStateException(String.format(
                "City invariant violated: population cannot be negative (%d)", nextPopulation
            ));
        }

        this.budget = nextBudget;
        this.pollution = nextPollution;
        this.population = nextPopulation;
        this.happiness = nextHappiness;
        this.tickCount++;
    }

    public synchronized CitySnapshot exportSnapshot() {
        return new CitySnapshot(this.budget, this.pollution, this.population, this.happiness, this.tickCount);
    }

    public synchronized void restoreFromSnapshot(CitySnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Snapshot to restore cannot be null");
        }
        this.budget = snapshot.getBudget();
        this.pollution = snapshot.getPollution();
        this.population = snapshot.getPopulation();
        this.happiness = snapshot.getHappiness();
        this.tickCount = snapshot.getTickCount();
        validateInvariants();
    }

    private void validateInvariants() {
        if (this.budget < MIN_BUDGET) {
            throw new IllegalStateException("Budget " + this.budget + " is below minimum threshold " + MIN_BUDGET);
        }
        if (this.population < 0) {
            throw new IllegalStateException("Population cannot be negative: " + this.population);
        }
    }

    private static double clamp(double value) {
        return Math.min(MAX_HAPPINESS, Math.max(MIN_HAPPINESS, value));
    }
}
