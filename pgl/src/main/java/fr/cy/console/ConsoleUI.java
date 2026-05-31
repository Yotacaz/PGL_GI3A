package fr.cy.console;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.properties.EmotionalState;
import fr.cy.model.fire.Fire;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

import java.util.List;
import java.util.Scanner;

/**
 * Interface console pour visualiser et interagir avec le graphe.
 * Affiche les nœuds, arêtes et informations détaillées en format texte.
 */
public class ConsoleUI {

    private final Graph graph;
    private final Scanner scanner;

    public ConsoleUI(Graph graph) {
        this.graph = graph;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Affiche le graphe complet et lance la boucle interactive.
     */
    public void run() {
        boolean running = true;
        while (running) {
            displayMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> displayGraph();
                case "2" -> displayAllNodes();
                case "3" -> displayAllEdges();
                case "4" -> selectNodeAndDisplay();
                case "5" -> selectEdgeAndDisplay();
                case "6" -> displayStatistics();
                case "7" -> createNodeInteractive();
                case "8" -> createEdgeInteractive();
                case "0" -> {
                    System.out.println("\n👋 Au revoir!");
                    running = false;
                }
                default -> System.out.println("\n❌ Option invalide, réessayez.\n");
            }
        }
        scanner.close();
    }

    private void displayMainMenu() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📊 VISUALISEUR DE GRAPHE - CONSOLE");
        System.out.println("=".repeat(60));
        System.out.println("1️⃣  Afficher le graphe complet");
        System.out.println("2️⃣  Lister tous les nœuds");
        System.out.println("3️⃣  Lister toutes les arêtes");
        System.out.println("4️⃣  Détails d'un nœud");
        System.out.println("5️⃣  Détails d'une arête");
        System.out.println("6️⃣  Afficher statistiques globales");
        System.out.println("7️⃣  Ajouter un nœud");
        System.out.println("8️⃣  Ajouter une arête");
        System.out.println("0️⃣  Quitter");
        System.out.println("=".repeat(60));
        System.out.print("Choix: ");
    }

    /**
     * Affiche une visualisation ASCII du graphe avec les nœuds et arêtes.
     */
    private void displayGraph() {
        System.out.println("\n" + "═".repeat(80));
        System.out.println("🔗 GRAPHE COMPLET");
        System.out.println("═".repeat(80));

        List<Node> nodes = graph.getNodes();
        List<Edge> edges = graph.getEdges();

        // Afficher les nœuds
        System.out.println("\n📍 NŒUDS (" + nodes.size() + "):");
        System.out.println("─".repeat(80));
        for (Node node : nodes) {
            String exitMarker = node.isExit() ? " 🚪" : "";
            String fireMarker = node.isOnFire() ? " 🔥" : "";
            System.out.printf("  %s%s%s%n", node, exitMarker, fireMarker);
        }

        // Afficher les arêtes
        System.out.println("\n🔗 ARÊTES (" + edges.size() + "):");
        System.out.println("─".repeat(80));
        for (Edge edge : edges) {
            System.out.printf("  %s%n", edge);
        }

        System.out.println("\n" + "═".repeat(80));
    }

    /**
     * Affiche la liste détaillée de tous les nœuds.
     */
    private void displayAllNodes() {
        List<Node> nodes = graph.getNodes();

        System.out.println("\n" + "═".repeat(80));
        System.out.println("📍 TOUS LES NŒUDS (" + nodes.size() + ")");
        System.out.println("═".repeat(80));

        for (Node node : nodes) {
            displayNodeDetails(node);
            System.out.println("─".repeat(80));
        }
    }

    /**
     * Affiche la liste de toutes les arêtes.
     */
    private void displayAllEdges() {
        List<Edge> edges = graph.getEdges();

        System.out.println("\n" + "═".repeat(80));
        System.out.println("🔗 TOUTES LES ARÊTES (" + edges.size() + ")");
        System.out.println("═".repeat(80));

        for (Edge edge : edges) {
            System.out.printf("  [%d] → [%d]%n", edge.getStart().getId(), edge.getEnd().getId());
        }

        System.out.println("═".repeat(80));
    }

    /**
     * Permet de sélectionner un nœud et affiche ses détails.
     */
    private void selectNodeAndDisplay() {
        System.out.println("\n📍 Entrez l'ID du nœud (ou 'q' pour quitter): ");
        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("q"))
            return;

        try {
            int nodeId = Integer.parseInt(input);
            Node node = graph.getNodes().stream()
                    .filter(n -> n.getId() == nodeId)
                    .findFirst()
                    .orElse(null);

            if (node != null) {
                System.out.println("\n" + "═".repeat(80));
                System.out.println("📋 DÉTAILS DU NŒUD #" + nodeId);
                System.out.println("═".repeat(80));
                displayNodeDetails(node);
                elementActionsMenu(node);
                System.out.println("═".repeat(80));
            } else {
                System.out.println("\n❌ Nœud non trouvé!");
            }
        } catch (NumberFormatException e) {
            System.out.println("\n❌ ID invalide!");
        }
    }

    /**
     * Permet de sélectionner une arête et affiche ses détails.
     */
    private void selectEdgeAndDisplay() {
        System.out.println("\n🔗 Entrez l'ID de l'arête (ou 'q' pour quitter): ");
        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("q")) {
            return;
        }

        try {
            int edgeId = Integer.parseInt(input);
            Edge edge = graph.getEdges().stream()
                    .filter(e -> e.getId() == edgeId)
                    .findFirst()
                    .orElse(null);

            if (edge != null) {
                System.out.println("\n" + "═".repeat(80));
                System.out.println("📋 DÉTAILS DE L'ARÊTE #" + edgeId);
                System.out.println("═".repeat(80));
                displayEdgeDetails(edge);
                elementActionsMenu(edge);
                System.out.println("═".repeat(80));
            } else {
                System.out.println("\n❌ Arête non trouvée!");
            }
        } catch (NumberFormatException e) {
            System.out.println("\n❌ ID invalide!");
        }
    }

    /**
     * Ajoute un nœud en mode interactif.
     */
    private void createNodeInteractive() {
        System.out.println("\n➕ AJOUT D'UN NŒUD");

        Double x = readDouble("Position X: ");
        if (x == null) {
            return;
        }

        Double y = readDouble("Position Y: ");
        if (y == null) {
            return;
        }

        boolean useDefaultCapacity = readYesNo("Utiliser la capacité par défaut ? (o/n) [o]: ", true);
        Node createdNode;

        if (useDefaultCapacity) {
            createdNode = graph.createNode(x, y);
        } else {
            Double capacity = readDouble("Capacité du nœud: ");
            if (capacity == null) {
                return;
            }
            createdNode = graph.createNode(x, y, capacity);
        }

        boolean exit = readYesNo("Ce nœud est une sortie ? (o/n) [n]: ", false);
        createdNode.setExit(exit);

        System.out.println("\n✅ Nœud créé: " + createdNode);
    }

    /**
     * Ajoute une arête en mode interactif.
     */
    private void createEdgeInteractive() {
        System.out.println("\n➕ AJOUT D'UNE ARÊTE");

        if (graph.getNodes().size() < 2) {
            System.out.println("\n❌ Il faut au moins deux nœuds pour créer une arête.");
            return;
        }

        System.out.println("Nœuds disponibles:");
        for (Node node : graph.getNodes()) {
            System.out.println("  - " + node.getId() + " : " + node);
        }

        Integer startId = readInt("ID du nœud de début: ");
        if (startId == null) {
            return;
        }

        Integer endId = readInt("ID du nœud de fin: ");
        if (endId == null) {
            return;
        }

        Node startNode = graph.getNodeById(startId);
        Node endNode = graph.getNodeById(endId);

        if (startNode == null || endNode == null) {
            System.out.println("\n❌ Nœud de début ou de fin introuvable.");
            return;
        }

        boolean useDefaultEdge = readYesNo("Utiliser les paramètres par défaut de l'arête ? (o/n) [o]: ", true);
        Edge createdEdge;

        if (useDefaultEdge) {
            createdEdge = graph.createEdge(startNode, endNode);
        } else {
            Double length = readDouble("Longueur: ");
            if (length == null) {
                return;
            }

            Double width = readDouble("Largeur: ");
            if (width == null) {
                return;
            }

            boolean directed = readYesNo("Arête orientée ? (o/n) [n]: ", false);
            createdEdge = graph.createEdge(startNode, endNode, length, width, directed);
        }

        System.out.println("\n✅ Arête créée: " + createdEdge);
    }

    /**
     * Affiche les détails complets d'un nœud.
     */
    private void displayNodeDetails(Node node) {
        List<Agent> agents = node.getAgents();

        System.out.println("\n📍 NŒUD #" + node.getId());
        System.out.println("   Position: (" + (int) node.getX() + ", " + (int) node.getY() + ")");
        System.out.println("   Sortie: " + (node.isExit() ? "Oui ✅" : "Non"));
        System.out.println("   Arêtes connectées: " + node.getEdges().size());

        // --- Occupation ---
        System.out.println("\n   ── Occupation ──");
        System.out.println("   Agents présents: " + agents.size());
        System.out.println("   Capacité: " + String.format("%.1f", node.getCapacity()));
        System.out.println("   Espace occupé: " + String.format("%.1f", node.getOccupiedSpace()));
        System.out.println("   Congestion: " + String.format("%.0f%%", node.getCongestion() * 100));
        System.out.println("   Accessible: " + (!node.isFull() ? "Oui ✅" : "Non — plein ❌"));

        // --- Agents ---
        System.out.println("\n   ── Agents ──");
        if (agents.isEmpty()) {
            System.out.println("   Vitesse moy.: —");
            System.out.println("   Stress moy.: —");
            System.out.println("   État dominant: —");
        } else {
            double avgSpeed = agents.stream()
                    .mapToDouble(Agent::getMaxSpeed)
                    .average().orElse(0);
            System.out.println("   Vitesse moy.: " + String.format("%.1f", avgSpeed));

            double avgStress = agents.stream()
                    .mapToDouble(Agent::getStressLevel)
                    .average().orElse(0);
            System.out.println("   Stress moy.: " + String.format("%.0f%%", avgStress * 100));

            EmotionalState dominant = getDominantState(agents);
            System.out.println("   État dominant: " + dominant.name());
        }

        // --- Stress global ---
        System.out.println("\n   ── Stress global ──");
        double globalStress = node.getCachedTotalStressInducedIncludingNeighbors();
        System.out.println("   Stress (+ voisins): " + String.format("%.0f%%", globalStress * 100));

        // --- Feu ---
        System.out.println("\n   ── Feu ──");
        if (node.isOnFire()) {
            System.out.println("   🔥 EN FEU");
            if (node.getFire() != null) {
                System.out.println("   Intensité: " + String.format("%.2f", node.getFire().getIntensity()));
                System.out.println("   Fumée: " + String.format("%.2f", node.getFire().getSmokeLevel()));
                System.out.println("   Propagation: " + String.format("%.2f", node.getFire().getSpreadRate()));
                System.out.println("   Brûle depuis: " + node.getFire().getBurningTicks() + " ticks");
            }
        } else {
            System.out.println("   Pas de feu ✅");
        }
    }

    /**
     * Affiche les détails complets d'une arête.
     */
    private void displayEdgeDetails(Edge edge) {
        List<Agent> agents = edge.getAgents();

        System.out.println("\n🔗 ARÊTE #" + edge.getId());
        System.out.println("   Extrémités: " + edge.getStart().getId() + " → " + edge.getEnd().getId());
        System.out.println("   Dirigée: " + (edge.isDirected() ? "Oui ✅" : "Non"));
        System.out.println("   Longueur: " + String.format("%.1f", edge.getLength()));
        System.out.println("   Largeur: " + String.format("%.1f", edge.getWidth()));
        System.out.println("   Capacité: " + String.format("%.1f", edge.getCapacity()));

        // --- Occupation ---
        System.out.println("\n   ── Occupation ──");
        System.out.println("   Agents présents: " + agents.size());
        System.out.println("   Espace occupé: " + String.format("%.1f", edge.getOccupiedSpace()));
        System.out.println("   Congestion: " + String.format("%.0f%%", edge.getCongestion() * 100));
        System.out.println("   Accessible: " + (!edge.isFull() ? "Oui ✅" : "Non — plein ❌"));
        System.out.println("   Vitesse max agents: " + String.format("%.2f", edge.getMaxAgentSpeed()));

        // --- Agents ---
        System.out.println("\n   ── Agents ──");
        if (agents.isEmpty()) {
            System.out.println("   Aucun agent sur l'arête");
        } else {
            double avgSpeed = agents.stream()
                    .mapToDouble(Agent::getMaxSpeed)
                    .average().orElse(0);
            System.out.println("   Vitesse moy.: " + String.format("%.1f", avgSpeed));

            double avgStress = agents.stream()
                    .mapToDouble(Agent::getStressLevel)
                    .average().orElse(0);
            System.out.println("   Stress moy.: " + String.format("%.0f%%", avgStress * 100));

            EmotionalState dominant = getDominantState(agents);
            System.out.println("   État dominant: " + dominant.name());
        }

        // --- Stress global ---
        System.out.println("\n   ── Stress global ──");
        double globalStress = edge.getCachedTotalStressInducedIncludingNeighbors();
        System.out.println("   Stress (+ voisins): " + String.format("%.0f%%", globalStress * 100));

        // --- Feu ---
        System.out.println("\n   ── Feu ──");
        if (edge.isOnFire()) {
            System.out.println("   🔥 EN FEU");
            if (edge.getFire() != null) {
                System.out.println("   Intensité: " + String.format("%.2f", edge.getFire().getIntensity()));
                System.out.println("   Fumée: " + String.format("%.2f", edge.getFire().getSmokeLevel()));
                System.out.println("   Propagation: " + String.format("%.2f", edge.getFire().getSpreadRate()));
                System.out.println("   Brûle depuis: " + edge.getFire().getBurningTicks() + " ticks");
            }
        } else {
            System.out.println("   Pas de feu ✅");
        }
    }

    /**
     * Affiche les actions possibles sur un élément sélectionné.
     */
    private void elementActionsMenu(GraphElement element) {
        boolean running = true;

        while (running) {
            System.out.println("\n   ── Actions ──");
            System.out.println("   1️⃣ Ajouter un feu");
            System.out.println("   2️⃣ Ajouter un agent");
            System.out.println("   3️⃣ Ajouter des agents en masse");
            System.out.println("   4️⃣ Voir tous les agents");
            System.out.println("   5️⃣ Revoir les détails");
            System.out.println("   0️⃣ Retour");
            System.out.print("   Choix: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    createFireInteractive(element);
                    displaySelectedElementDetails(element);
                }
                case "2" -> {
                    createAgentInteractive(element);
                    displaySelectedElementDetails(element);
                }
                case "3" -> {
                    createAgentsBulkInteractive(element);
                    displaySelectedElementDetails(element);
                }
                case "4" -> displayElementAgents(element);
                case "5" -> displaySelectedElementDetails(element);
                case "0" -> running = false;
                default -> System.out.println("   ❌ Option invalide.");
            }
        }
    }

    /**
     * Affiche les détails d'un élément, quel qu'il soit.
     */
    private void displaySelectedElementDetails(GraphElement element) {
        if (element instanceof Node node) {
            displayNodeDetails(node);
            return;
        }

        if (element instanceof Edge edge) {
            displayEdgeDetails(edge);
        }
    }

    /**
     * Affiche tous les agents présents sur un élément.
     */
    private void displayElementAgents(GraphElement element) {
        List<Agent> agents = element.getAgents();

        System.out.println("\n" + "═".repeat(80));
        System.out.println("👥 AGENTS DE L'ÉLÉMENT #" + element.getId());
        System.out.println("═".repeat(80));

        if (agents.isEmpty()) {
            System.out.println("Aucun agent présent.");
            System.out.println("═".repeat(80));
            return;
        }

        System.out.println("Nombre d'agents: " + agents.size());
        System.out.println("─".repeat(80));
        for (Agent agent : agents) {
            System.out.println("  - " + agent);
            System.out.println("    ID: " + agent.getId());
            System.out.println("    Nom: " + agent.getName());
            System.out.println("    Vitesse max: " + agent.getMaxSpeed());
            System.out.println("    Stress: " + String.format("%.0f%%", agent.getStressLevel() * 100));
            System.out.println("    État: " + agent.getEmotionalState().name());
            System.out.println("─".repeat(80));
        }

        System.out.println("═".repeat(80));
    }

    /**
     * Ajoute ou remplace un feu sur un élément.
     */
    private void createFireInteractive(GraphElement element) {
        System.out.println("\n   ➕ AJOUT D'UN FEU");
        boolean useDefaultValues = readYesNo("   Utiliser les valeurs par défaut ? (o/n) [o]: ", true);

        Fire fire;
        if (useDefaultValues) {
            fire = new Fire(0.5, 0.4, 0.2);
        } else {
            Double intensity = readDouble("   Intensité: ");
            if (intensity == null) {
                return;
            }

            Double smokeLevel = readDouble("   Fumée: ");
            if (smokeLevel == null) {
                return;
            }

            Double spreadRate = readDouble("   Propagation: ");
            if (spreadRate == null) {
                return;
            }

            fire = new Fire(intensity, smokeLevel, spreadRate);
        }

        element.setFire(fire);
        System.out.println("\n   ✅ Feu ajouté: intensité=" + String.format("%.2f", fire.getIntensity())
                + ", fumée=" + String.format("%.2f", fire.getSmokeLevel())
                + ", propagation=" + String.format("%.2f", fire.getSpreadRate()));
    }

    /**
     * Ajoute un agent sur un élément.
     */
    private void createAgentInteractive(GraphElement element) {
        System.out.println("\n   ➕ AJOUT D'UN AGENT");
        boolean useDefaultValues = readYesNo("   Utiliser les valeurs par défaut ? (o/n) [o]: ", true);

        Agent agent;
        if (useDefaultValues) {
            agent = new Agent("Agent", 5, 0.5, 0.5);
        } else {
            System.out.print("   Nom: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                name = "Agent";
            }

            Double maxSpeed = readDouble("   Vitesse max: ");
            if (maxSpeed == null) {
                return;
            }

            Double stressTolerance = readDouble("   Tolérance stress (0-1): ");
            if (stressTolerance == null) {
                return;
            }

            Double crowdingTolerance = readDouble("   Tolérance congestion (0-1): ");
            if (crowdingTolerance == null) {
                return;
            }

            agent = new Agent(name, maxSpeed.intValue(), stressTolerance, crowdingTolerance);
        }

        element.addAgent(agent);
        System.out.println("\n   ✅ Agent ajouté: " + agent.getName() + " (#" + agent.getId() + ")");
        System.out.println("   Congestion actuelle: " + String.format("%.0f%%", element.getCongestion() * 100));
    }

    /**
     * Ajoute plusieurs agents en une seule fois sur un élément.
     */
    private void createAgentsBulkInteractive(GraphElement element) {
        System.out.println("\n   ➕ AJOUT D'AGENTS EN MASSE");

        Integer count = readInt("   Nombre d'agents à ajouter: ");
        if (count == null) {
            return;
        }

        if (count <= 0) {
            System.out.println("   ❌ Le nombre doit être supérieur à 0.");
            return;
        }

        boolean useDefaultValues = readYesNo("   Utiliser les valeurs par défaut ? (o/n) [o]: ", true);
        String namePrefix = "Agent";
        int maxSpeed = 5;
        double stressTolerance = 0.5;
        double crowdingTolerance = 0.5;

        if (!useDefaultValues) {
            System.out.print("   Préfixe du nom (ex: Agent): ");
            String prefix = scanner.nextLine().trim();
            if (!prefix.isEmpty()) {
                namePrefix = prefix;
            }

            Double speedInput = readDouble("   Vitesse max: ");
            if (speedInput == null) {
                return;
            }
            maxSpeed = speedInput.intValue();

            Double stressInput = readDouble("   Tolérance stress (0-1): ");
            if (stressInput == null) {
                return;
            }
            stressTolerance = stressInput;

            Double crowdingInput = readDouble("   Tolérance congestion (0-1): ");
            if (crowdingInput == null) {
                return;
            }
            crowdingTolerance = crowdingInput;
        }

        int added = 0;
        for (int i = 1; i <= count; i++) {
            Agent agent = new Agent(namePrefix + "_" + i, maxSpeed, stressTolerance, crowdingTolerance);
            element.addAgent(agent);
            added++;
        }

        System.out.println("\n   ✅ " + added + " agents ajoutés sur l'élément.");
        System.out.println("   Congestion actuelle: " + String.format("%.0f%%", element.getCongestion() * 100));
    }

    /**
     * Détermine l'état émotionnel dominant parmi les agents.
     */
    private EmotionalState getDominantState(List<Agent> agents) {
        int calm = 0, selfish = 0, panicking = 0;
        for (Agent a : agents) {
            switch (a.getEmotionalState()) {
                case CALM -> calm++;
                case SELFISH -> selfish++;
                case PANICKING -> panicking++;
            }
        }
        if (panicking >= calm && panicking >= selfish)
            return EmotionalState.PANICKING;
        if (selfish >= calm)
            return EmotionalState.SELFISH;
        return EmotionalState.CALM;
    }

    /**
     * Affiche les statistiques globales du graphe.
     */
    private void displayStatistics() {
        List<Node> nodes = graph.getNodes();
        List<Edge> edges = graph.getEdges();

        System.out.println("\n" + "═".repeat(80));
        System.out.println("📊 STATISTIQUES GLOBALES");
        System.out.println("═".repeat(80));

        System.out.println("\n   Nœuds: " + nodes.size());
        System.out.println("   Arêtes: " + edges.size());

        // Sorties
        long exits = nodes.stream().filter(Node::isExit).count();
        System.out.println("   Sorties: " + exits);

        // Nœuds en feu
        long onFire = nodes.stream().filter(Node::isOnFire).count();
        System.out.println("   Nœuds en feu: " + onFire);

        // Agents totaux
        int totalAgents = nodes.stream()
                .mapToInt(n -> n.getAgents().size())
                .sum();
        System.out.println("   Agents totaux: " + totalAgents);

        // Congestion
        double avgCongestion = nodes.stream()
                .mapToDouble(Node::getCongestion)
                .average()
                .orElse(0);
        System.out.println("   Congestion moyenne: " + String.format("%.0f%%", avgCongestion * 100));

        // Stress moyen
        double avgStress = nodes.stream()
                .mapToDouble(Node::getCachedTotalStressInducedIncludingNeighbors)
                .average()
                .orElse(0);
        System.out.println("   Stress moyen: " + String.format("%.0f%%", avgStress * 100));

        System.out.println("\n" + "═".repeat(80));
    }

    private Integer readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("q")) {
                return null;
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Valeur entière invalide, recommencez ou tapez q pour annuler.");
            }
        }
    }

    private Double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("q")) {
                return null;
            }
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Valeur décimale invalide, recommencez ou tapez q pour annuler.");
            }
        }
    }

    private boolean readYesNo(String prompt, boolean defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.isEmpty()) {
                return defaultValue;
            }
            if (input.equals("o") || input.equals("oui") || input.equals("y") || input.equals("yes")) {
                return true;
            }
            if (input.equals("n") || input.equals("non") || input.equals("no")) {
                return false;
            }
            System.out.println("❌ Réponse invalide, utilisez o/n.");
        }
    }
}
