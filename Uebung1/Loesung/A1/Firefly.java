package A1;

import java.util.Random;

/**
 * This class represents a firefly.
 * Each firefly has a phase and can influence its neighbors.
 */
public class Firefly implements Runnable {
    /**
     * Rate at which fireflies influence each other.
     * The simulation to the point of full synchronization can be sped up by increasing this value,
     * or slowed down by decreasing it.
     */
    private static final double SYNC_RATE = 0.005;

    private double phase; // Current phase of the firefly (0 to 1)
    private Firefly[] neighbors; // Array of neighboring fireflies
    private volatile boolean running = true; // Flag to control the loop

    /**
     * Constructor to initialize the firefly with a random phase.
     */
    public Firefly() {
        // Random number generator for initial phase
        Random random = new Random();
        this.phase = random.nextDouble(); // Set a random initial phase
    }

    /**
     * Sets the neighbors of this firefly.
     * @param neighbors an array of neighboring fireflies
     */
    public void setNeighbors(Firefly[] neighbors) {
        this.neighbors = neighbors;
    }

    /**
     * Returns the current phase of the firefly.
     * @return the current phase
     */
    public double getPhase() {
        return phase;
    }

    public void stop() {
        running = false; // Stop the thread
    }

    /**
     * The run method for the firefly's thread.
     * It updates the phase and influences the neighbors.
     */
    @Override
    public void run() {
        while (running) {
            // Update phase
            phase += 0.01; // Progress the phase (oscillation frequency)
            if (phase >= 1.0) {
                phase = 0.0; // Reset phase when it reaches 1
            }

            // Influence from neighbors
            double neighborInfluence = 0.0;
            for (Firefly neighbor : neighbors) {
                // Calculate influence based on the phase difference
                neighborInfluence += Math.sin(2 * Math.PI * (neighbor.getPhase() - phase));
            }
            // Adjust phase based on neighbors' influence (communication latency)
            phase += SYNC_RATE * neighborInfluence / neighbors.length;

            // Short pause for the thread (simulates communication latency)
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break; // Exit the loop if interrupted
            }
        }
    }
}