package com.citylogic.domain.tick;

/**
 * Checked domain exception thrown when a simulation tick fails an invariant and is rolled back.
 */
public class SimulationException extends Exception {
    public SimulationException(String message) {
        super(message);
    }

    public SimulationException(String message, Throwable cause) {
        super(message, cause);
    }
}
