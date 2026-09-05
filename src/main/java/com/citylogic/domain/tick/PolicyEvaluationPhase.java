package com.citylogic.domain.tick;

import com.citylogic.application.policies.IPolicyStrategy;
import com.citylogic.domain.buildings.IBuildingState;
import com.citylogic.domain.core.CitySnapshot;
import com.citylogic.domain.core.ResourceDelta;
import com.citylogic.domain.map.IGridReadPort;

import java.util.*;

/**
 * Policy Evaluation Phase: applies all active municipal ordinances
 * (IPolicyStrategy instances) to each powered building on the grid
 * and accumulates the resulting modifiers into the tick delta.
 */
public class PolicyEvaluationPhase implements ITickPhase {
    private final Map<String, IPolicyStrategy> activePolicies = new LinkedHashMap<>();

    public synchronized void activatePolicy(IPolicyStrategy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("Policy cannot be null");
        }
        activePolicies.put(policy.getName(), policy);
    }

    public synchronized void deactivatePolicy(String policyName) {
        if (policyName != null) {
            activePolicies.remove(policyName);
        }
    }

    public synchronized List<String> getActivePolicyNames() {
        return new ArrayList<>(activePolicies.keySet());
    }

    public synchronized List<IPolicyStrategy> getActivePolicies() {
        return new ArrayList<>(activePolicies.values());
    }

    @Override
    public ResourceDelta execute(CitySnapshot snapshot, IGridReadPort grid) {
        ResourceDelta total = ResourceDelta.zero();

        List<IBuildingState> buildings = grid.getAllBuildings();
        List<IPolicyStrategy> policies;
        synchronized (this) {
            policies = new ArrayList<>(activePolicies.values());
        }

        for (IBuildingState building : buildings) {
            if (!building.isPowered()) {
                continue;
            }
            for (IPolicyStrategy policy : policies) {
                ResourceDelta mod = policy.calculateModifier(building, grid);
                if (mod != null) {
                    total = total.merge(mod);
                }
            }
        }

        return total;
    }
}
