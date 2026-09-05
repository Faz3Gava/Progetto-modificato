# CityLogic — JavaFX Desktop Application

This directory contains the complete **Java + JavaFX** implementation of the **CityLogic** municipal simulation and urban planning engine, structured according to the Domain-Driven Design (DDD) specifications in `docs/design-document.md`.

---

## 🏗️ Architecture Overview

The codebase is organized in Maven standard directory layout (`com.citylogic`):

- **`com.citylogic.domain.core`**:
  - `Point`, `Dimension`, `ResourceDelta`: Immutable value objects.
  - `CitySnapshot`: Immutable DTO for state export and transaction rollback.
  - `CityAggregate`: Aggregate root strictly enforcing invariants (bankruptcy threshold, non-negative population, clamped happiness).

- **`com.citylogic.domain.buildings`**:
  - `IBuildingState`: Read-only projection port.
  - `BuildingDescription`: Flyweight metadata specifying construction cost, maintenance, footprint, and yields.
  - `BuildingInstance`: Entity representing an active building on the grid with power toggle and maintenance tracking.
  - `BuildingFactory`: Factory pattern generating building instances.

- **`com.citylogic.domain.map`**:
  - `Cell`: Individual grid tile holding pollution and building references.
  - `Grid`: Spatial 2D matrix implementing `IGridReadPort` and `IGridCommandPort`.

- **`com.citylogic.domain.tick`**:
  - `ITickPhase`: Pipeline strategy interface.
  - `ProductionPhase`: Sums base production across all powered buildings.
  - `PolicyEvaluationPhase`: Evaluates active municipal ordinances.
  - `SimulationEngine`: Transactional orchestrator with automated rollback on invariant failure.

- **`com.citylogic.application`**:
  - `BuildingCatalog`: Application-level building type lookup.
  - `ApplicationBuildingDescriptionProvider`: Registers House, Factory, Park, Commercial Hub, and Solar Plant.
  - `PlacementValidator`: Checks boundary and collision constraints.
  - `GameEngine`: Application service facade used by presentation layer.
  - `policies.*`: `EnvironmentalTaxPolicy`, `GreenSubsidyPolicy`, `EcoBufferZonePolicy`, `HousingInitiativePolicy`.

- **`com.citylogic.ui`**:
  - `CityLogicApp`: Main JavaFX `Application` entrypoint.
  - `CityMapCanvas`: Custom JavaFX Canvas drawing interactive tiles, buildings, hover highlights, and selection indicators.
  - `GameViewController`: FXML Controller binding metrics, simulation ticker, tools, and policy toggles.
  - `GameView.fxml` & `styles.css`: Complete FXML layout and modern slate dark stylesheet.

---

## 🚀 How to Run Locally

### Prerequisites
- **JDK 17 or 21+** (e.g. Eclipse Temurin, Liberica, or Oracle JDK)
- **Apache Maven 3.8+** (or use your IDE's embedded Maven)

### Running with Maven
From the project root:

```bash
mvn clean javafx:run
```

Or on Linux / macOS:
```bash
./run.sh
```

Or on Windows:
```cmd
run.bat
```

### Running Unit Tests
Execute the full JUnit 5 domain and simulation test suite:

```bash
mvn test
```

### Packaging an Executable JAR
To produce a standalone fat JAR containing all dependencies:

```bash
mvn clean package
java -jar target/citylogic-javafx-1.0.0.jar
```

---

## 💻 Importing into IDEs

### IntelliJ IDEA
1. Open IntelliJ IDEA.
2. Select **File -> Open...** and select the project root folder (where `pom.xml` is located).
3. Wait for Maven to sync dependencies.
4. Run `com.citylogic.Main` or run `mvn javafx:run` from the Maven tool window.

### VS Code
1. Install the **Extension Pack for Java** and **Maven for Java**.
2. Open the project root folder.
3. Open `src/main/java/com/citylogic/Main.java` and click **Run Java**.

### Eclipse
1. Select **File -> Import... -> Existing Maven Projects**.
2. Choose the root folder and click **Finish**.
3. Right-click `com.citylogic.Main` -> **Run As -> Java Application**.
