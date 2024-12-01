package A2;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

/**
 * Implementation of the FireflyRMI interface.
 * This class represents a firefly that can synchronize with its neighbors.
 */
public class FireflyImplementation extends UnicastRemoteObject implements FireflyRMI, Runnable {
    private static final double SYNC_RATE = 0.001; // Synchronization rate
    private double phase; // Current phase of the firefly (used for on and off state)
    private FireflyRMI[] neighbors; // Array of neighbors
    /*
    bool to set the running state of the firefly/ thread, technically it is not needed as the thread
    will stop when the program stops (no direct usage of stop method)
     */
    private volatile boolean running;

    /**
     * Constructor for the FireflyImplementation class.
     */
    protected FireflyImplementation() throws RemoteException {
        super();
        Random random = new Random();
        this.phase = random.nextDouble(); // Initialize with random phase
        this.running = true;
    }

    @Override
    public synchronized double getPhase() throws RemoteException {
        return this.phase;
    }

    @Override
    public void synchronizeWithNeighbors() throws RemoteException {
        double neighborInfluence = 0.0;
        for (FireflyRMI neighbor : neighbors) {
            try {
                // Calculate the influence of each neighbor's phase using the Kuramoto model
                neighborInfluence += Math.sin(2 * Math.PI * (neighbor.getPhase() - phase));
            } catch (RemoteException e) {
                System.err.println("Error synchronizing with neighbor: " + e.getMessage());
            }
        }

        synchronized (this) {
            // Update the phase based on the influence of neighbors
            phase += SYNC_RATE * neighborInfluence / neighbors.length;
            if (phase >= 1.0) {
                phase = 0.0; // Reset phase
            }
        }
    }

    @Override
    public void setNeighbors(FireflyRMI[] neighbors) throws RemoteException {
        this.neighbors = neighbors;
    }

    @Override
    public void run() {
        while (running) {
            try {
                phase += 0.01; // Increment phase
                if (phase >= 1.0) {
                    phase = 0.0; // Reset phase
                }

                // Synchronize with neighbors
                synchronizeWithNeighbors();

                Thread.sleep(10); // Delay to simulate real-time updates
            } catch (InterruptedException | RemoteException e) {
                System.err.println("Error in Firefly thread: " + e.getMessage());
                running = false;
            }
        }
    }

    // Method to stop the firefly thread
    public void stop() {
        this.running = false;
    }
}