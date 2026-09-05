package com.citylogic.domain.tick;

import com.citylogic.application.ICityEventPublisher;
import com.citylogic.application.policies.IPolicyStrategy;
import com.citylogic.domain.core.CityAggregate;
import com.citylogic.domain.core.CitySnapshot;
import com.citylogic.domain.core.ResourceDelta;
import com.citylogic.domain.map.IGridReadPort;

import java.util.Collections;
import java.util.List;

/**
 * Core simulation orchestrator executing discrete transactional ticks.
 * Guarantees all-or-nothing transactional rollback if city state invariants are breached.
 */
public class SimulationEngine {
    private final CityAggregate cityState;
    private final IGridReadPort gridReader;
    private final ICityEventPublisher eventPublisher;
    private final List<ITickPhase> phases;
    private final PolicyEvaluationPhase policyPhase;
    private ResourceDelta lastDelta = ResourceDelta.zero();

    public SimulationEngine(
            CityAggregate cityState,
            IGridReadPort gridReader,
            ICityEventPublisher eventPublisher,
            SimulationConfig config) {
        if (cityState == null) throw new IllegalArgumentException("cityState cannot be null");
        if (gridReader == null) throw new IllegalArgumentException("gridReader cannot be null");
        if (eventPublisher == null) throw new IllegalArgumentException("eventPublisher cannot be null");

        this.cityState = cityState;
        this.gridReader = gridReader;
        this.eventPublisher = eventPublisher;

        SimulationConfig.PhasePipeline pipeline = SimulationConfig.createPipeline(
            config != null ? config : SimulationConfig.defaultConfig()
        );
        this.phases = pipeline.getPhases();
        this.policyPhase = pipeline.getPolicyPhase();
    }

    public SimulationEngine(CityAggregate cityState, IGridReadPort gridReader, ICityEventPublisher eventPublisher) {
        this(cityState, gridReader, eventPublisher, SimulationConfig.defaultConfig());
    }

    /**
     * Advances the city simulation by one transactional tick.
     * @return The updated CitySnapshot and the applied ResourceDelta
     * @throws SimulationException if invariant fails and transaction is rolled back
     */
    public synchronized TickResult advanceTick() throws SimulationException {
        // 1. Transactional state snapshot
        CitySnapshot startSnapshot = cityState.exportSnapshot();

        // 2. Execute discrete phases
        ResourceDelta totalDelta = ResourceDelta.zero();
        for (ITickPhase phase : phases) {
            ResourceDelta phaseDelta = phase.execute(startSnapshot, gridReader);
            if (phaseDelta != null) {
                totalDelta = totalDelta.merge(phaseDelta);
            }
        }

        // 3. Commit delta or rollback
        try {
            cityState.applyDelta(totalDelta);
        } catch (Exception err) {
            cityState.restoreFromSnapshot(startSnapshot);
            throw new SimulationException("Simulation Tick Failed (Rolled back to tick " + startSnapshot.getTickCount() + "): " + err.getMessage(), err);
        }

        CitySnapshot committedSnapshot = cityState.exportSnapshot();
        this.lastDelta = totalDelta;

        // 4. Publish committed event
        eventPublisher.publish(committedSnapshot, totalDelta);

        return new TickResult(committedSnapshot, totalDelta);
    }

    public synchronized CitySnapshot getCurrentSnapshot() {
        return cityState.exportSnapshot();
    }

    public synchronized ResourceDelta getLastDelta() {
        return lastDelta;
    }

    public synchronized void loadState(CitySnapshot snapshot) {
        cityState.restoreFromSnapshot(snapshot);
        eventPublisher.publish(cityState.exportSnapshot(), ResourceDelta.zero());
    }

    public synchronized void activatePolicy(IPolicyStrategy policy) {
        if (policyPhase == null) {
            throw new IllegalStateException("PolicyEvaluationPhase is not enabled in simulation pipeline");
        }
        policyPhase.activatePolicy(policy);
    }

    public synchronized void deactivatePolicy(String policyName) {
        if (policyPhase == null) {
            throw new IllegalStateException("PolicyEvaluationPhase is not enabled in simulation pipeline");
        }
        policyPhase.deactivatePolicy(policyName);
    }

    public synchronized List<String> getActivePolicyNames() {
        return policyPhase != null ? policyPhase.getActivePolicyNames() : Collections.emptyList();
    }

    public synchronized List<IPolicyStrategy> getActivePolicies() {
        return policyPhase != null ? policyPhase.getActivePolicies() : Collections.emptyList();
    }

    public static final class TickResult {
        private final CitySnapshot snapshot;
        private final ResourceDelta delta;

        public TickResult(CitySnapshot snapshot, ResourceDelta delta) {
            this.snapshot = snapshot;
            this.delta = delta;
        }

        public CitySnapshot getSnapshot() {
            return snapshot;
        }

        public ResourceDelta getDelta() {
            return delta;
        }
    }
}
