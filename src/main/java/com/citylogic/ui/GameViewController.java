package com.citylogic.ui;

import com.citylogic.application.ApplicationBuildingDescriptionProvider;
import com.citylogic.application.BuildingCatalog;
import com.citylogic.application.CityEventPublisher;
import com.citylogic.application.GameEngine;
import com.citylogic.application.ICityObserver;
import com.citylogic.application.PlacementValidator;
import com.citylogic.application.policies.EcoBufferZonePolicy;
import com.citylogic.application.policies.EnvironmentalTaxPolicy;
import com.citylogic.application.policies.GreenSubsidyPolicy;
import com.citylogic.application.policies.HousingInitiativePolicy;
import com.citylogic.application.policies.IPolicyStrategy;
import com.citylogic.domain.buildings.BuildingDescription;
import com.citylogic.domain.buildings.IBuildingState;
import com.citylogic.domain.core.CityAggregate;
import com.citylogic.domain.core.CitySnapshot;
import com.citylogic.domain.core.Dimension;
import com.citylogic.domain.core.Point;
import com.citylogic.domain.core.ResourceDelta;
import com.citylogic.domain.map.Grid;
import com.citylogic.domain.tick.SimulationConfig;
import com.citylogic.domain.tick.SimulationEngine;
import com.citylogic.domain.tick.SimulationException;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * JavaFX FXML View Controller coordinating GameEngine, CityMapCanvas,
 * Municipal Policies, and reactive simulation metrics.
 */
public class GameViewController implements Initializable, ICityObserver {
    private static final int GRID_WIDTH = 12;
    private static final int GRID_HEIGHT = 9;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML private Label budgetLabel;
    @FXML private Label budgetDeltaLabel;
    @FXML private Label populationLabel;
    @FXML private Label populationDeltaLabel;
    @FXML private Label happinessLabel;
    @FXML private ProgressBar happinessBar;
    @FXML private Label pollutionLabel;
    @FXML private ProgressBar pollutionBar;
    @FXML private Label tickLabel;
    @FXML private Label statusMessageLabel;

    @FXML private ToggleGroup toolToggleGroup;
    @FXML private ToggleButton selectToolBtn;
    @FXML private ToggleButton demolishToolBtn;
    @FXML private ToggleButton houseBtn;
    @FXML private ToggleButton factoryBtn;
    @FXML private ToggleButton parkBtn;
    @FXML private ToggleButton commercialBtn;
    @FXML private ToggleButton solarBtn;

    @FXML private Button playPauseBtn;
    @FXML private Button stepTickBtn;
    @FXML private Slider speedSlider;
    @FXML private Label speedLabel;

    @FXML private CityMapCanvas mapCanvas;

    // Inspector
    @FXML private VBox inspectorPanel;
    @FXML private Label inspectorTitle;
    @FXML private Label inspectorCoords;
    @FXML private Label inspectorFootprint;
    @FXML private Label inspectorCategory;
    @FXML private Label inspectorProduction;
    @FXML private Button togglePowerBtn;
    @FXML private Button demolishSelectedBtn;

    // Policies
    @FXML private CheckBox envTaxCheck;
    @FXML private CheckBox greenSubsidyCheck;
    @FXML private CheckBox ecoBufferCheck;
    @FXML private CheckBox housingCheck;

    // Audit Log
    @FXML private ListView<String> logListView;
    private final ObservableList<String> logItems = FXCollections.observableArrayList();

    private GameEngine gameEngine;
    private Grid grid;
    private BuildingCatalog catalog;
    private SimulationEngine simulationEngine;

    private final Map<String, IPolicyStrategy> availablePolicies = new HashMap<>();
    private String activeTool = "select";
    private Point selectedPoint = null;

    private boolean isPlaying = false;
    private long lastTickTime = 0;
    private AnimationTimer ticker;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logListView.setItems(logItems);

        // 1. Initialize Game Engine Architecture
        CityAggregate aggregate = new CityAggregate(2500.0, 0, 70.0);
        this.grid = new Grid(new Dimension(GRID_WIDTH, GRID_HEIGHT));
        this.catalog = new BuildingCatalog();
        ApplicationBuildingDescriptionProvider.initDefaultCatalog(catalog);

        PlacementValidator validator = new PlacementValidator(catalog);
        CityEventPublisher publisher = new CityEventPublisher();
        publisher.subscribe(this);

        this.simulationEngine = new SimulationEngine(aggregate, grid, publisher, SimulationConfig.defaultConfig());
        this.gameEngine = new GameEngine(grid, grid, simulationEngine, catalog, validator);

        // 2. Policies map
        availablePolicies.put("env_tax", new EnvironmentalTaxPolicy());
        availablePolicies.put("green_sub", new GreenSubsidyPolicy());
        availablePolicies.put("eco_buf", new EcoBufferZonePolicy());
        availablePolicies.put("house_grant", new HousingInitiativePolicy());

        // 3. Setup Initial Starter Town
        gameEngine.placeBuilding(3, 3, "house", false);
        gameEngine.placeBuilding(4, 3, "house", false);
        gameEngine.placeBuilding(5, 3, "park", false);
        gameEngine.placeBuilding(3, 5, "commercial_hub", false);

        // 4. Initialize Canvas
        mapCanvas.init(grid, catalog);
        setupCanvasInteractions();

        // 5. Setup Tools
        setupTools();

        // 6. Setup Policies
        setupPolicyListeners();

        // 7. Setup Speed Slider & Play/Pause
        setupSimulationTicker();

        // 8. Refresh View
        updateMetrics(gameEngine.getCitySnapshot(), ResourceDelta.zero());
        updateInspector();

        addLog("Welcome to CityLogic JavaFX Edition! Starter town established.");
    }

    private void setupCanvasInteractions() {
        mapCanvas.setOnMouseMoved(evt -> {
            Point p = mapCanvas.getGridCoordinatesFromPixel(evt.getX(), evt.getY());
            mapCanvas.setHoverPoint(p);
        });

        mapCanvas.setOnMouseExited(evt -> {
            mapCanvas.setHoverPoint(null);
        });

        mapCanvas.setOnMouseClicked(evt -> {
            Point p = mapCanvas.getGridCoordinatesFromPixel(evt.getX(), evt.getY());
            if (p == null) return;

            handleGridClick(p.getX(), p.getY());
        });
    }

    private void handleGridClick(int x, int y) {
        if ("select".equals(activeTool)) {
            selectedPoint = new Point(x, y);
            mapCanvas.setSelectedPoint(selectedPoint);
            updateInspector();
            return;
        }

        if ("demolish".equals(activeTool)) {
            GameEngine.DemolitionResult res = gameEngine.demolishBuilding(x, y);
            if (res.isSuccessful()) {
                addLog("Demolished " + res.getBuilding().getType() + " at (" + x + ", " + y + ")");
                mapCanvas.redraw();
                updateInspector();
            } else {
                setStatusMessage("No building to demolish at (" + x + ", " + y + ")", true);
            }
            return;
        }

        // Active tool is building typeId
        GameEngine.PlacementResult res = gameEngine.placeBuilding(x, y, activeTool, true);
        if (res.isSuccessful()) {
            addLog("Constructed " + res.getBuilding().getType() + " at (" + x + ", " + y + ")");
            selectedPoint = new Point(x, y);
            mapCanvas.setSelectedPoint(selectedPoint);
            mapCanvas.redraw();
            updateInspector();
            setStatusMessage("Constructed " + res.getBuilding().getType() + " successfully!", false);
        } else {
            setStatusMessage(res.getErrorMessage(), true);
        }
    }

    private void setupTools() {
        toolToggleGroup = new ToggleGroup();
        selectToolBtn.setToggleGroup(toolToggleGroup);
        demolishToolBtn.setToggleGroup(toolToggleGroup);
        houseBtn.setToggleGroup(toolToggleGroup);
        factoryBtn.setToggleGroup(toolToggleGroup);
        parkBtn.setToggleGroup(toolToggleGroup);
        commercialBtn.setToggleGroup(toolToggleGroup);
        solarBtn.setToggleGroup(toolToggleGroup);

        selectToolBtn.setSelected(true);

        toolToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                selectToolBtn.setSelected(true);
                activeTool = "select";
            } else if (newVal == selectToolBtn) {
                activeTool = "select";
            } else if (newVal == demolishToolBtn) {
                activeTool = "demolish";
            } else if (newVal == houseBtn) {
                activeTool = "house";
            } else if (newVal == factoryBtn) {
                activeTool = "factory";
            } else if (newVal == parkBtn) {
                activeTool = "park";
            } else if (newVal == commercialBtn) {
                activeTool = "commercial_hub";
            } else if (newVal == solarBtn) {
                activeTool = "solar_plant";
            }
            mapCanvas.setActiveTool(activeTool);
            setStatusMessage("Tool: " + activeTool, false);
        });
    }

    private void setupPolicyListeners() {
        envTaxCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            IPolicyStrategy p = availablePolicies.get("env_tax");
            if (newVal) {
                gameEngine.setCityPolicy(p);
                addLog("Policy Activated: " + p.getName());
            } else {
                gameEngine.clearCityPolicy(p.getName());
                addLog("Policy Deactivated: " + p.getName());
            }
        });

        greenSubsidyCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            IPolicyStrategy p = availablePolicies.get("green_sub");
            if (newVal) {
                gameEngine.setCityPolicy(p);
                addLog("Policy Activated: " + p.getName());
            } else {
                gameEngine.clearCityPolicy(p.getName());
                addLog("Policy Deactivated: " + p.getName());
            }
        });

        ecoBufferCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            IPolicyStrategy p = availablePolicies.get("eco_buf");
            if (newVal) {
                gameEngine.setCityPolicy(p);
                addLog("Policy Activated: " + p.getName());
            } else {
                gameEngine.clearCityPolicy(p.getName());
                addLog("Policy Deactivated: " + p.getName());
            }
        });

        housingCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            IPolicyStrategy p = availablePolicies.get("house_grant");
            if (newVal) {
                gameEngine.setCityPolicy(p);
                addLog("Policy Activated: " + p.getName());
            } else {
                gameEngine.clearCityPolicy(p.getName());
                addLog("Policy Deactivated: " + p.getName());
            }
        });
    }

    private void setupSimulationTicker() {
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            speedLabel.setText(String.format("%.1fx", newVal.doubleValue()));
        });

        stepTickBtn.setOnAction(evt -> executeTick());

        playPauseBtn.setOnAction(evt -> {
            isPlaying = !isPlaying;
            if (isPlaying) {
                playPauseBtn.setText("⏸ Pause");
                playPauseBtn.getStyleClass().add("btn-pause");
            } else {
                playPauseBtn.setText("▶ Play");
                playPauseBtn.getStyleClass().remove("btn-pause");
            }
        });

        ticker = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isPlaying) return;

                double speed = speedSlider.getValue();
                long intervalNanos = (long) (1_000_000_000L / Math.max(0.5, speed));

                if (now - lastTickTime >= intervalNanos) {
                    lastTickTime = now;
                    executeTick();
                }
            }
        };
        ticker.start();
    }

    private void executeTick() {
        try {
            SimulationEngine.TickResult result = gameEngine.advanceTime();
            updateMetrics(result.getSnapshot(), result.getDelta());
            mapCanvas.redraw();

            if (result.getSnapshot().getBudget() < 0) {
                setStatusMessage("WARNING: City is in municipal debt!", true);
            }
        } catch (SimulationException e) {
            isPlaying = false;
            playPauseBtn.setText("▶ Play");
            playPauseBtn.getStyleClass().remove("btn-pause");
            setStatusMessage(e.getMessage(), true);
            addLog("SIMULATION ERROR: " + e.getMessage());
        }
    }

    private void updateMetrics(CitySnapshot snapshot, ResourceDelta delta) {
        Platform.runLater(() -> {
            budgetLabel.setText(String.format("$%,.0f", snapshot.getBudget()));
            budgetDeltaLabel.setText(String.format("%+,.0f/tick", delta.getBudgetDelta()));
            budgetDeltaLabel.setStyle(delta.getBudgetDelta() >= 0 ? "-fx-text-fill: #22c55e;" : "-fx-text-fill: #ef4444;");

            populationLabel.setText(String.format("%,d", snapshot.getPopulation()));
            populationDeltaLabel.setText(String.format("%+d", delta.getPopulationDelta()));

            happinessLabel.setText(String.format("%.1f%%", snapshot.getHappiness()));
            happinessBar.setProgress(snapshot.getHappiness() / 100.0);

            pollutionLabel.setText(String.format("%.1f ppm", snapshot.getPollution()));
            pollutionBar.setProgress(Math.min(1.0, snapshot.getPollution() / 150.0));

            tickLabel.setText(String.format("Tick #%d", snapshot.getTickCount()));
        });
    }

    private void updateInspector() {
        if (selectedPoint == null) {
            inspectorTitle.setText("No Tile Selected");
            inspectorCoords.setText("-");
            inspectorFootprint.setText("-");
            inspectorCategory.setText("-");
            inspectorProduction.setText("-");
            togglePowerBtn.setDisable(true);
            demolishSelectedBtn.setDisable(true);
            return;
        }

        Cell cell = grid.getCell(selectedPoint.getX(), selectedPoint.getY());
        inspectorCoords.setText("(" + selectedPoint.getX() + ", " + selectedPoint.getY() + ")");

        if (cell == null || !cell.isOccupied()) {
            inspectorTitle.setText("Empty Grassland");
            inspectorFootprint.setText("1x1");
            inspectorCategory.setText("Open Terrain");
            inspectorProduction.setText("None");
            togglePowerBtn.setDisable(true);
            demolishSelectedBtn.setDisable(true);
            return;
        }

        IBuildingState building = cell.getBuilding();
        BuildingDescription desc = building.getDescription();

        inspectorTitle.setText(desc.getName());
        inspectorFootprint.setText(desc.getFootprint().toString());
        inspectorCategory.setText(desc.getCategory().name());
        inspectorProduction.setText(String.format("Budget: %+,.0f | Citizens: %+d | Pollution: %+.1f | Happy: %+.1f%%",
            desc.getBaseProduction().getBudgetDelta(),
            desc.getBaseProduction().getPopulationDelta(),
            desc.getBaseProduction().getPollutionDelta(),
            desc.getBaseProduction().getHappinessDelta()
        ));

        togglePowerBtn.setDisable(false);
        togglePowerBtn.setText(building.isPowered() ? "⚡ Power: ON" : "🔌 Power: OFF");
        togglePowerBtn.setOnAction(evt -> {
            gameEngine.toggleBuildingPower(building.getId());
            togglePowerBtn.setText(building.isPowered() ? "⚡ Power: ON" : "🔌 Power: OFF");
            mapCanvas.redraw();
            addLog("Toggled power for " + desc.getName() + ": " + (building.isPowered() ? "ON" : "OFF"));
        });

        demolishSelectedBtn.setDisable(false);
        demolishSelectedBtn.setOnAction(evt -> {
            Point p = building.getPosition();
            GameEngine.DemolitionResult res = gameEngine.demolishBuilding(p.getX(), p.getY());
            if (res.isSuccessful()) {
                addLog("Demolished " + desc.getName() + " via inspector.");
                mapCanvas.redraw();
                updateInspector();
            }
        });
    }

    private void addLog(String message) {
        Platform.runLater(() -> {
            String time = LocalTime.now().format(TIME_FMT);
            logItems.add(0, "[" + time + "] " + message);
            if (logItems.size() > 50) {
                logItems.remove(logItems.size() - 1);
            }
        });
    }

    private void setStatusMessage(String message, boolean isError) {
        Platform.runLater(() -> {
            statusMessageLabel.setText(message);
            statusMessageLabel.setStyle(isError ? "-fx-text-fill: #ef4444;" : "-fx-text-fill: #38bdf8;");
        });
    }

    @Override
    public void onMetricsChanged(CitySnapshot snapshot, ResourceDelta delta) {
        updateMetrics(snapshot, delta);
    }
}
