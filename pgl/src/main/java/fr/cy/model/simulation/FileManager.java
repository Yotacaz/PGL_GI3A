package fr.cy.model.simulation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Gestionnaire de persistance des simulations en format binaire.
 */
public class FileManager {

    /**
     * Sauvegarde une simulation dans un fichier binaire.
     *
     * @param simulation simulation à sauvegarder
     * @param path chemin du fichier de destination
     * @throws IOException en cas d'erreur d'écriture
     */
    public void saveSimulation(Simulation simulation, Path path) throws IOException {
        if (simulation == null) {
            throw new IllegalArgumentException("La simulation ne peut pas être nulle.");
        }

        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (ObjectOutputStream outputStream = new ObjectOutputStream(Files.newOutputStream(path))) {
            outputStream.writeObject(simulation);
        }
    }

    /**
     * Charge une simulation depuis un fichier binaire.
     *
     * @param path chemin du fichier source
     * @return simulation restaurée
     * @throws IOException en cas d'erreur de lecture
     * @throws ClassNotFoundException si le contenu binaire ne correspond pas à une simulation valide
     */
    public Simulation loadSimulation(Path path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream inputStream = new ObjectInputStream(Files.newInputStream(path))) {
            Object loadedObject = inputStream.readObject();

            if (!(loadedObject instanceof Simulation simulation)) {
                throw new IOException("Le fichier ne contient pas une simulation valide.");
            }

            return simulation;
        }
    }
}