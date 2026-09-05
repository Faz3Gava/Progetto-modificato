package com.citylogic.application;

import com.citylogic.domain.buildings.BuildingDescription;

import java.util.*;

/**
 * Application repository and catalog for available building specifications.
 */
public class BuildingCatalog {
    private final Map<String, BuildingDescription> byTypeId = new LinkedHashMap<>();

    public synchronized void register(BuildingDescription description) {
        if (description == null) {
            throw new IllegalArgumentException("BuildingDescription cannot be null");
        }
        byTypeId.put(description.getTypeId(), description);
    }

    public synchronized BuildingDescription intern(BuildingDescription description) {
        if (description == null) {
            throw new IllegalArgumentException("BuildingDescription cannot be null");
        }
        return byTypeId.computeIfAbsent(description.getTypeId(), k -> description);
    }

    public synchronized BuildingDescription getByTypeId(String typeId) {
        if (typeId == null) return null;
        return byTypeId.get(typeId);
    }

    public synchronized List<BuildingDescription> listAll() {
        return new ArrayList<>(byTypeId.values());
    }
}
