package A1;

import javax.swing.*;

/**
 * The main class to start the firefly simulation.
 */
public class Main {
    /**
     * The main method to launch the application.
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Firefly Synchronization");
            FireflySimulation simulation = new FireflySimulation();
            frame.add(simulation);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}