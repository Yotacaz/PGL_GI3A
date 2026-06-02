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

    public static void saveSimulation(Simulation simulation) {
        String filePath = SIMULATION_FOLDER + simulation.getName() + ".bin";
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(filePath))) {

            out.writeObject(simulation);

            System.out.println("Simulation sauvegardée avec succès");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la sauvegarde de la simulation : " + e.getMessage());
        }
    }

    public static Simulation loadSimulation(String name) {
        String filePath = SIMULATION_FOLDER + name + ".bin";
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(filePath))) {
            return (Simulation) in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();

            System.out.println("Erreur lors du chargement de la simulation : " + e.getMessage());

        }
        return null;
    }

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
