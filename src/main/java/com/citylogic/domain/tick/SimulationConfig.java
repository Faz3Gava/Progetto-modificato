package com.citylogic.domain.tick;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration and factory for simulation tick execution pipeline.
 */
public class SimulationConfig {
    public static final String PHASE_PRODUCTION = "PRODUCTION";
    public static final String PHASE_POLICY = "POLICY";

    private final List<String> enabledPhases;

    public SimulationConfig(List<String> enabledPhases) {
        this.enabledPhases = enabledPhases != null ? new ArrayList<>(enabledPhases) : defaultPhases();
    }

    public SimulationConfig() {
        this(defaultPhases());
    }

    public static List<String> defaultPhases() {
        return Arrays.asList(PHASE_PRODUCTION, PHASE_POLICY);
    }

    public static SimulationConfig defaultConfig() {
        return new SimulationConfig();
    }

    public List<String> getEnabledPhases() {
        return new ArrayList<>(enabledPhases);
    }

    public static class PhasePipeline {
        private final List<ITickPhase> phases;
        private final PolicyEvaluationPhase policyPhase;

        public PhasePipeline(List<ITickPhase> phases, PolicyEvaluationPhase policyPhase) {
            this.phases = phases;
            this.policyPhase = policyPhase;
        }

        public List<ITickPhase> getPhases() {
            return phases;
        }

        public PolicyEvaluationPhase getPolicyPhase() {
            return policyPhase;
        }
    }

    public static PhasePipeline createPipeline(SimulationConfig config) {
        List<ITickPhase> phases = new ArrayList<>();
        PolicyEvaluationPhase policyPhase = null;

        for (String phaseName : config.getEnabledPhases()) {
            if (PHASE_PRODUCTION.equals(phaseName)) {
                phases.add(new ProductionPhase());
            } else if (PHASE_POLICY.equals(phaseName)) {
                policyPhase = new PolicyEvaluationPhase();
                phases.add(policyPhase);
            } else {
                throw new IllegalArgumentException("Unknown simulation phase: " + phaseName);
            }
        }

        return new PhasePipeline(phases, policyPhase);
    }
}
