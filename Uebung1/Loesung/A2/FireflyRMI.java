package A2;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI Interface for Firefly communication.
 * This interface defines the methods that can be called remotely by other Firefly instances.
 */
public interface FireflyRMI extends Remote {
    // Method to get the current phase of the firefly
    double getPhase() throws RemoteException;

    // Method to synchronize the firefly's phase with its neighbors
    void synchronizeWithNeighbors() throws RemoteException;

    // Method to set the neighbors of the firefly
    void setNeighbors(FireflyRMI[] neighbors) throws RemoteException;
}