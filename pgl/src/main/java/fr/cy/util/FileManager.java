package fr.cy.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fr.cy.model.simulation.Simulation;

/**
 * Class responsible for saving and loading simulations to and from files.
 */
public class FileManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private static FileManager instance;

    private static final String SIMULATION_FOLDER = "simulations/";

    private FileManager() {
    }

    public static FileManager getInstance() {
        if (instance == null) {
            instance = new FileManager();
        }
        return instance;
    }

    /**
     * Saves a simulation to a file.
     *
     * @param simulation the simulation to save
     */
    public static void saveSimulation(Simulation simulation) {
        File folder = new File(SIMULATION_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File saveFile = new File(folder, simulation.getName() + ".bin");
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(saveFile))) {

            out.writeObject(simulation);

            System.out.println("Simulation sauvegardée avec succès : " + saveFile.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la sauvegarde de la simulation : " + e.getMessage());
        }
    }

    /**
     * Loads a simulation from a file.
     *
     * @param name the name of the simulation file (without extension)
     * @return the loaded simulation or null if not found
     */
    public static Simulation loadSimulation(String name) {
        File file = new File(SIMULATION_FOLDER, name + ".bin");
        if (!file.exists()) {
            System.out.println("Simulation file not found: " + file.getAbsolutePath());
            return null;
        }

        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(file))) {
            return (Simulation) in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de la simulation : " + e.getMessage());
        }
        return null;
    }

    /**
     * Saves a simulation to a file under a custom filename (differs from
     * {@code simulation.getName()}).  The simulation's internal name is unchanged.
     *
     * @param simulation the simulation to save
     * @param fileName   the file name to use (without extension)
     */
    public static void saveSimulationAs(Simulation simulation, String fileName) {
        File folder = new File(SIMULATION_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File saveFile = new File(folder, fileName + ".bin");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(saveFile))) {
            out.writeObject(simulation);
            System.out.println("Simulation sauvegardée sous : " + saveFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }

    /**
     * Lists all available simulations in the simulations folder.
     * @return list of simulation names (without file extension)
     */
    public static List<String> getAvailableSimulations() {
        List<String> simulationNames = new ArrayList<>();
        File folder = new File(SIMULATION_FOLDER);
        
        if (!folder.exists()) {
            System.out.println("Le dossier de simulations n'existe pas. Création du dossier...");
            folder.mkdirs();
            return simulationNames;
        }
        
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".bin"));
        System.out.println("Simulations disponibles : " + (files != null ? files.length : 0));
        
        if (files != null) {
            for (File file : files) {
                String nameWithoutExtension = file.getName().replace(".bin", "");
                simulationNames.add(nameWithoutExtension);
            }
        }
        
        return simulationNames;
    }

}
