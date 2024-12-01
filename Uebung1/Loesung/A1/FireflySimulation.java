package A1;

import java.awt.*;
import javax.swing.*;

/**
 * This class represents the simulation of fireflies.
 * It creates a grid of fireflies and updates their phases.
 */
public class FireflySimulation extends JPanel {
    private static final int GRID_ROWS = 5; // Number of rows in the grid
    private static final int GRID_COLS = 5; // Number of columns in the grid
    private static final int CELL_SIZE = 50; // Size of each cell in pixels
    private final Firefly[][] fireflies = new Firefly[GRID_ROWS][GRID_COLS]; // 2D array to hold fireflies

    /**
     * Constructor to initialize the firefly simulation.
     * It sets up the fireflies and their neighbors, and starts the threads.
     */
    public FireflySimulation() {
        // Initialize fireflies
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                fireflies[i][j] = new Firefly();
            }
        }

        // Assign neighbors to each firefly
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                Firefly[] neighbors = new Firefly[]{
                        fireflies[(i - 1 + GRID_ROWS) % GRID_ROWS][j], // top neighbor
                        fireflies[(i + 1) % GRID_ROWS][j], // bottom neighbor
                        fireflies[i][(j - 1 + GRID_COLS) % GRID_COLS], // left neighbor
                        fireflies[i][(j + 1) % GRID_COLS] // right neighbor
                };
                fireflies[i][j].setNeighbors(neighbors);
            }
        }

        // Start threads for each firefly
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                new Thread(fireflies[i][j]).start();
            }
        }

        // Timer for GUI updates
        Timer timer = new Timer(50, e -> repaint());
        timer.start();
    }

    /**
     * Paints the grid of fireflies.
     * @param g the Graphics object used for painting
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                // Set color based on phase
                if (fireflies[i][j].getPhase() < 0.5) {
                    g.setColor(Color.LIGHT_GRAY); // OFF
                } else {
                    g.setColor(Color.YELLOW); // ON
                }
                g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    /**
     * Returns the preferred size of the panel.
     * @return the preferred size of the panel
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(GRID_COLS * CELL_SIZE, GRID_ROWS * CELL_SIZE);
    }
}