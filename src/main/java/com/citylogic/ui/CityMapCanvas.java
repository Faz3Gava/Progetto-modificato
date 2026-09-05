package com.citylogic.ui;

import com.citylogic.application.BuildingCatalog;
import com.citylogic.domain.buildings.BuildingDescription;
import com.citylogic.domain.buildings.IBuildingState;
import com.citylogic.domain.core.Dimension;
import com.citylogic.domain.core.Point;
import com.citylogic.domain.map.Cell;
import com.citylogic.domain.map.Grid;
import com.citylogic.domain.map.IGridReadPort;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.Optional;

/**
 * Custom JavaFX Canvas rendering the urban simulation grid, terrain,
 * placed buildings, hover shadows, and selection highlights.
 */
public class CityMapCanvas extends Canvas {
    private static final int TILE_SIZE = 54;
    private static final int PADDING = 24;

    private IGridReadPort gridReader;
    private BuildingCatalog catalog;

    private Point selectedPoint = null;
    private Point hoverPoint = null;
    private String activeTool = "select"; // 'select', 'demolish', or building typeId

    public CityMapCanvas() {
        super(750, 580);
        widthProperty().addListener(evt -> redraw());
        heightProperty().addListener(evt -> redraw());
    }

    public void init(IGridReadPort gridReader, BuildingCatalog catalog) {
        this.gridReader = gridReader;
        this.catalog = catalog;
        updateCanvasDimensions();
        redraw();
    }

    public void setActiveTool(String tool) {
        this.activeTool = tool;
        redraw();
    }

    public void setSelectedPoint(Point point) {
        this.selectedPoint = point;
        redraw();
    }

    public void setHoverPoint(Point point) {
        this.hoverPoint = point;
        redraw();
    }

    public Point getGridCoordinatesFromPixel(double pixelX, double pixelY) {
        if (gridReader == null) return null;
        Dimension dim = gridReader.getDimensions();

        double totalGridWidth = dim.getWidth() * TILE_SIZE;
        double totalGridHeight = dim.getHeight() * TILE_SIZE;
        double startX = Math.max(PADDING, (getWidth() - totalGridWidth) / 2.0);
        double startY = Math.max(PADDING, (getHeight() - totalGridHeight) / 2.0);

        int gx = (int) Math.floor((pixelX - startX) / TILE_SIZE);
        int gy = (int) Math.floor((pixelY - startY) / TILE_SIZE);

        if (gridReader.isWithinBounds(gx, gy)) {
            return new Point(gx, gy);
        }
        return null;
    }

    private void updateCanvasDimensions() {
        if (gridReader == null) return;
        Dimension dim = gridReader.getDimensions();
        double requiredWidth = dim.getWidth() * TILE_SIZE + PADDING * 2;
        double requiredHeight = dim.getHeight() * TILE_SIZE + PADDING * 2;
        setWidth(Math.max(700, requiredWidth));
        setHeight(Math.max(540, requiredHeight));
    }

    public void redraw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        // 1. Background Fill
        gc.setFill(Color.web("#0f172a")); // Slate 900
        gc.fillRect(0, 0, w, h);

        if (gridReader == null) return;

        Dimension dim = gridReader.getDimensions();
        double totalGridWidth = dim.getWidth() * TILE_SIZE;
        double totalGridHeight = dim.getHeight() * TILE_SIZE;
        double startX = Math.max(PADDING, (w - totalGridWidth) / 2.0);
        double startY = Math.max(PADDING, (h - totalGridHeight) / 2.0);

        // 2. City Border / Grid Ground Shadow
        gc.setFill(Color.web("#1e293b", 0.6));
        gc.fillRoundRect(startX - 6, startY - 6, totalGridWidth + 12, totalGridHeight + 12, 16, 16);

        // 3. Draw Grid Tiles
        for (int x = 0; x < dim.getWidth(); x++) {
            for (int y = 0; y < dim.getHeight(); y++) {
                double tx = startX + x * TILE_SIZE;
                double ty = startY + y * TILE_SIZE;

                // Checkerboard subtle soil pattern
                boolean alt = (x + y) % 2 == 0;
                Color tileColor = alt ? Color.web("#1e293b") : Color.web("#162032");

                gc.setFill(tileColor);
                gc.fillRoundRect(tx + 1, ty + 1, TILE_SIZE - 2, TILE_SIZE - 2, 6, 6);

                // Grid cell border
                gc.setStroke(Color.web("#334155", 0.4));
                gc.setLineWidth(1);
                gc.strokeRoundRect(tx + 1, ty + 1, TILE_SIZE - 2, TILE_SIZE - 2, 6, 6);
            }
        }

        // 4. Draw Buildings
        for (IBuildingState building : gridReader.getAllBuildings()) {
            drawBuilding(gc, building, startX, startY);
        }

        // 5. Draw Placement Hover Preview or Demolish Highlight
        if (hoverPoint != null && gridReader.isWithinBounds(hoverPoint.getX(), hoverPoint.getY())) {
            drawHoverOverlay(gc, startX, startY);
        }

        // 6. Draw Selected Cell Highlight
        if (selectedPoint != null && gridReader.isWithinBounds(selectedPoint.getX(), selectedPoint.getY())) {
            double sx = startX + selectedPoint.getX() * TILE_SIZE;
            double sy = startY + selectedPoint.getY() * TILE_SIZE;

            gc.setStroke(Color.web("#38bdf8")); // Sky 400
            gc.setLineWidth(3);
            gc.strokeRoundRect(sx + 2, sy + 2, TILE_SIZE - 4, TILE_SIZE - 4, 8, 8);
        }
    }

    private void drawBuilding(GraphicsContext gc, IBuildingState building, double startX, double startY) {
        Point pos = building.getPosition();
        BuildingDescription desc = building.getDescription();
        Dimension foot = desc.getFootprint();

        double bx = startX + pos.getX() * TILE_SIZE + 3;
        double by = startY + pos.getY() * TILE_SIZE + 3;
        double bw = foot.getWidth() * TILE_SIZE - 6;
        double bh = foot.getHeight() * TILE_SIZE - 6;

        // Theme colors by category
        Color baseColor;
        Color strokeColor;
        String iconSymbol;

        switch (desc.getCategory()) {
            case RESIDENTIAL:
                baseColor = Color.web("#15803d"); // Green
                strokeColor = Color.web("#22c55e");
                iconSymbol = "🏠";
                break;
            case COMMERCIAL:
                baseColor = Color.web("#0369a1"); // Sky
                strokeColor = Color.web("#38bdf8");
                iconSymbol = "🏢";
                break;
            case INDUSTRIAL:
                baseColor = Color.web("#b45309"); // Amber
                strokeColor = Color.web("#f59e0b");
                iconSymbol = "🏭";
                break;
            case CIVIC:
                baseColor = Color.web("#047857"); // Emerald
                strokeColor = Color.web("#34d399");
                iconSymbol = "🌲";
                break;
            case UTILITY:
                baseColor = Color.web("#6d28d9"); // Purple
                strokeColor = Color.web("#a855f7");
                iconSymbol = "⚡";
                break;
            default:
                baseColor = Color.web("#374151");
                strokeColor = Color.web("#9ca3af");
                iconSymbol = "🏛️";
                break;
        }

        if (!building.isPowered()) {
            baseColor = baseColor.darker().desaturate();
            strokeColor = Color.web("#6b7280");
        }

        // Gradient body
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, baseColor.brighter()),
            new Stop(1, baseColor)
        );

        gc.setFill(gradient);
        gc.fillRoundRect(bx, by, bw, bh, 10, 10);

        gc.setStroke(strokeColor);
        gc.setLineWidth(2);
        gc.strokeRoundRect(bx, by, bw, bh, 10, 10);

        // Building Icon / Symbol
        gc.setFont(Font.font("Segoe UI Emoji, Arial", FontWeight.BOLD, foot.getWidth() > 1 ? 24 : 18));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.WHITE);
        gc.fillText(iconSymbol, bx + bw / 2.0, by + bh / 2.0 + 2);

        // Label
        gc.setFont(Font.font("System", FontWeight.SEMI_BOLD, 10));
        gc.setFill(Color.web("#e2e8f0"));
        gc.fillText(desc.getName(), bx + bw / 2.0, by + bh - 6);

        // Power Status indicator badge
        if (!building.isPowered()) {
            gc.setFill(Color.web("#ef4444"));
            gc.fillOval(bx + bw - 14, by + 4, 10, 10);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1);
            gc.strokeOval(bx + bw - 14, by + 4, 10, 10);
        }
    }

    private void drawHoverOverlay(GraphicsContext gc, double startX, double startY) {
        if ("select".equals(activeTool)) {
            double hx = startX + hoverPoint.getX() * TILE_SIZE;
            double hy = startY + hoverPoint.getY() * TILE_SIZE;
            gc.setStroke(Color.web("#94a3b8", 0.8));
            gc.setLineWidth(2);
            gc.strokeRoundRect(hx + 2, hy + 2, TILE_SIZE - 4, TILE_SIZE - 4, 6, 6);
            return;
        }

        if ("demolish".equals(activeTool)) {
            double hx = startX + hoverPoint.getX() * TILE_SIZE;
            double hy = startY + hoverPoint.getY() * TILE_SIZE;
            gc.setFill(Color.web("#ef4444", 0.35));
            gc.fillRoundRect(hx + 2, hy + 2, TILE_SIZE - 4, TILE_SIZE - 4, 6, 6);
            gc.setStroke(Color.web("#ef4444"));
            gc.setLineWidth(2);
            gc.strokeRoundRect(hx + 2, hy + 2, TILE_SIZE - 4, TILE_SIZE - 4, 6, 6);
            return;
        }

        // Active tool is building typeId
        if (catalog != null) {
            BuildingDescription desc = catalog.getByTypeId(activeTool);
            if (desc != null) {
                Dimension foot = desc.getFootprint();
                boolean canPlace = gridReader.isAreaFree(hoverPoint.getX(), hoverPoint.getY(), foot);

                double hx = startX + hoverPoint.getX() * TILE_SIZE;
                double hy = startY + hoverPoint.getY() * TILE_SIZE;
                double hw = foot.getWidth() * TILE_SIZE;
                double hh = foot.getHeight() * TILE_SIZE;

                Color tint = canPlace ? Color.web("#22c55e", 0.35) : Color.web("#ef4444", 0.35);
                Color border = canPlace ? Color.web("#22c55e") : Color.web("#ef4444");

                gc.setFill(tint);
                gc.fillRoundRect(hx + 3, hy + 3, hw - 6, hh - 6, 10, 10);
                gc.setStroke(border);
                gc.setLineWidth(2);
                gc.strokeRoundRect(hx + 3, hy + 3, hw - 6, hh - 6, 10, 10);
            }
        }
    }
}
