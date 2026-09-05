# CityLogic

CityLogic is an interactive city simulation and spatial grid management web application built with React, TypeScript, and Tailwind CSS. It faithfully ports and expands the original domain-driven design (DDD) architecture of the CityLogic simulation engine.

## Core Architecture & Features

- **Domain-Driven Design (DDD)**:
  - **CityAggregate**: Aggregate root managing invariants, budget limits (bankruptcy threshold at -$10,000), citizen population, happiness [0-100%], and air pollution.
  - **Spatial Grid**: Multi-cell spatial allocation system supporting diverse building footprints (1x1 Houses, 2x2 Industrial Factories, 1x1 Parks, 1x1 Commercial Hubs, 2x2 Solar Plants). Provides Chebyshev distance calculation for adjacent zoning effects.
  - **Transactional Tick Pipeline**: Implements `ITickPhase` across `ProductionPhase` and `PolicyEvaluationPhase`, executing atomic state transitions with automated rollback on invariant violations.
  - **Municipal Policy Council**: Enact and repeal active ordinances (`IPolicyStrategy`) including Environmental Taxes, Green Subsidies, Eco-Buffer Zones, and Housing Grants.
  - **Building Inspection & Power Link**: Toggle electrical grid connectivity per structure to manage municipal production and operational maintenance dynamically.

## Getting Started

```bash
npm install
npm run dev
```

Runs the development server on `http://localhost:3000`.
