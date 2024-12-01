package A2;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * RMI Server to register Firefly instances.
 * This class sets up the RMI registry and registers Firefly instances.
 */
public class FireflyServer {
    public static void main(String[] args) {
        try {
            // Create an RMI registry on port 1099 (Java RMI default port)
            Registry registry = LocateRegistry.createRegistry(1099);

            // Create a list to hold Firefly instances
            List<FireflyImplementation> fireflies = new ArrayList<>();
            for (int i = 0; i < 25; i++) {
                // Create a new Firefly instance
                FireflyImplementation firefly = new FireflyImplementation();
                fireflies.add(firefly);
                // Add the Firefly instance to the registry with a unique name
                String name = "Firefly" + i;
                registry.rebind(name, firefly);
                System.out.println(name + " was added to the registry.");
            }

            // Set neighbors for each firefly
            for (int i = 0; i < fireflies.size(); i++) {
                FireflyImplementation firefly = fireflies.get(i);
                // Create an array of neighbors excluding the current firefly
                FireflyImplementation[] neighbors = fireflies.stream()
                        .filter(f -> f != firefly)
                        .toArray(FireflyImplementation[]::new);
                firefly.setNeighbors(neighbors);
            }

            // Start the firefly threads
            for (FireflyImplementation firefly : fireflies) {
                new Thread(firefly).start();
            }
        } catch (RemoteException e) {
            System.err.println("Error setting up RMI server: " + e.getMessage());
        }
    }
}