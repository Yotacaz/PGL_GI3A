package fr.cy.model.simulation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.util.Scanner;



public class FileManager {
    private static FileManager instance;

    private static final String SIMULATION_FOLDER = "simulations/";
    

    private FileManager() {}

    public static FileManager getInstance() {
        if (instance == null) {
            instance = new FileManager();
        }
        return instance;
    }

    public static void saveSimulation(Simulation simulation) {
        String filePath = SIMULATION_FOLDER + simulation.getName() + ".bin";
         try (ObjectOutputStream out =
                     new ObjectOutputStream(
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
        try (ObjectInputStream in =
                     new ObjectInputStream(
                             new FileInputStream(filePath))) {
            return (Simulation) in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();

            System.out.println("Erreur lors du chargement de la simulation : " + e.getMessage());
            
        }
        return null;
    }


}
