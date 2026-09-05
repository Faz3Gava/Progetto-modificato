package com.citylogic.domain.core;

import java.util.Objects;

/**
 * Immutable 2D bounding dimension of grids and building footprints.
 */
public final class Dimension {
    private final int width;
    private final int height;

    public Dimension(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be strictly positive: " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getArea() {
        return width * height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dimension)) return false;
        Dimension dimension = (Dimension) o;
        return width == dimension.width && height == dimension.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }
}
