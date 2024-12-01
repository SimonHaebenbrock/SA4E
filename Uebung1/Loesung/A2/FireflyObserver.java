package A2;

import javax.swing.*;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Observer to monitor and visualize the fireflies' synchronization.
 * This class creates a GUI to display the phases of the fireflies.
 */
public class FireflyObserver extends JPanel {
    private static final int GRID_ROWS = 5; // Number of rows
    private static final int GRID_COLS = 5; // Number of columns
    private static final int CELL_SIZE = 50; // Size of each cell in the grid
    private FireflyRMI[] fireflies; // Array of fireflies to observe

    public FireflyObserver(FireflyRMI[] fireflies) {
        this.fireflies = fireflies;
        // Timer to repaint the panel every 50 milliseconds
        Timer timer = new Timer(50, e -> repaint());
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        try {
            for (int i = 0; i < GRID_ROWS; i++) {
                for (int j = 0; j < GRID_COLS; j++) {
                    int index = i * GRID_COLS + j;
                    double phase = fireflies[index].getPhase();
                    // Set color based on the current phase of the firefly
                    g.setColor(phase < 0.5 ? Color.LIGHT_GRAY : Color.YELLOW);
                    g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        } catch (Exception e) {
            System.err.println("Error during visualization: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            // Get the RMI registry from localhost on port 1099 (Java RMI default port)
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            // Create an array to hold references to the FireflyRMI instances
            FireflyRMI[] fireflies = new FireflyRMI[GRID_ROWS * GRID_COLS];
            for (int i = 0; i < fireflies.length; i++) {
                // Lookup each Firefly instance by name and add it to the array
                fireflies[i] = (FireflyRMI) registry.lookup("Firefly" + i);
            }

            // Create and set up the observer window
            JFrame frame = new JFrame("Firefly Observer");
            FireflyObserver observer = new FireflyObserver(fireflies);
            frame.add(observer);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack(); // Adjust the frame size to fit the preferred size of the panel
            frame.setVisible(true);

            // Adjust the frame size to account for insets (borders, title bar)
            Insets insets = frame.getInsets();
            frame.setSize(GRID_COLS * CELL_SIZE + insets.left + insets.right, GRID_ROWS * CELL_SIZE + insets.top + insets.bottom);
        } catch (Exception e) {
            System.err.println("Error setting up observer: " + e.getMessage());
        }
    }
}