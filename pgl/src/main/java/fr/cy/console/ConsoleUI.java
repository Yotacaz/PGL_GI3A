package fr.cy.console;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.properties.EmotionalState;
import fr.cy.model.fire.Fire;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import fr.cy.model.simulation.Simulation;

import java.util.List;
import java.util.Scanner;

/**
 * Interface console pour piloter et visualiser la simulation d'évacuation.
 * Délègue la création d'agents à AgentManager et expose les contrôles de simulation.
 */
public class ConsoleUI {

    private final Simulation simulation;
    private final Graph graph;
    private final Scanner scanner;

    public ConsoleUI(Simulation simulation) {
        this.simulation = simulation;
        this.graph = simulation.getGraph();
        this.scanner = new Scanner(System.in);
    }

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
                case "9" -> simulationControlMenu();
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
        System.out.printf("🔄 SIMULATION  |  Tick: %d  |  %s%n",
                simulation.getCurrentTick(),
                simulation.isRunning() ? "▶ EN COURS" : "⏸ EN PAUSE");
        System.out.printf("   Agents: %d actifs  |  %d évacués  |  %d morts%n",
                simulation.getAgentManager().getAgentsToEvacuate().size(),
                simulation.getAgentManager().getEvacuatedAgents().size(),
                simulation.getAgentManager().getDeadAgents().size());
        System.out.println("=".repeat(60));
        System.out.println("1️⃣  Afficher le graphe complet");
        System.out.println("2️⃣  Lister tous les nœuds");
        System.out.println("3️⃣  Lister toutes les arêtes");
        System.out.println("4️⃣  Détails d'un nœud");
        System.out.println("5️⃣  Détails d'une arête");
        System.out.println("6️⃣  Statistiques globales");
        System.out.println("7️⃣  Ajouter un nœud");
        System.out.println("8️⃣  Ajouter une arête");
        System.out.println("9️⃣  Contrôle de la simulation");
        System.out.println("0️⃣  Quitter");
        System.out.println("=".repeat(60));
        System.out.print("Choix: ");
    }

    // ─── Simulation control ───────────────────────────────────────────────────

    private void simulationControlMenu() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n" + "─".repeat(60));
            System.out.printf("⚙️  SIMULATION  |  Tick: %d  |  %s%n",
                    simulation.getCurrentTick(),
                    simulation.isRunning() ? "▶ EN COURS" : "⏸ EN PAUSE");
            System.out.printf("   Actifs: %d  |  Évacués: %d  |  Morts: %d%n",
                    simulation.getAgentManager().getAgentsToEvacuate().size(),
                    simulation.getAgentManager().getEvacuatedAgents().size(),
                    simulation.getAgentManager().getDeadAgents().size());
            System.out.println("─".repeat(60));
            System.out.println("   1️⃣  Avancer d'un tick");
            System.out.println("   2️⃣  Avancer de N ticks");
            System.out.println("   3️⃣  " + (simulation.isRunning() ? "Mettre en pause" : "Démarrer"));
            System.out.println("   4️⃣  Réinitialiser");
            System.out.println("   0️⃣  Retour");
            System.out.println("─".repeat(60));
            System.out.print("   Choix: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    simulation.stepTick();
                    System.out.printf("   ✅ Tick %d exécuté (%.2f ms)%n",
                            simulation.getCurrentTick(), simulation.getLastEngineLoadMs());
                }
                case "2" -> {
                    Integer n = readInt("   Nombre de ticks: ");
                    if (n != null && n > 0) {
                        for (int i = 0; i < n; i++) simulation.stepTick();
                        System.out.printf("   ✅ %d ticks exécutés — tick actuel: %d%n",
                                n, simulation.getCurrentTick());
                    }
                }
                case "3" -> {
                    if (simulation.isRunning()) {
                        simulation.stop();
                        System.out.println("   ⏸ Simulation mise en pause.");
                    } else {
                        simulation.start();
                        System.out.println("   ▶ Simulation démarrée.");
                    }
                }
                case "4" -> {
                    simulation.reset();
                    System.out.println("   🔄 Simulation réinitialisée.");
                }
                case "0" -> inMenu = false;
                default -> System.out.println("   ❌ Option invalide.");
            }
        }
    }

    // ─── Graph display ────────────────────────────────────────────────────────

    private void displayGraph() {
        System.out.println("\n" + "═".repeat(80));
        System.out.println("🔗 GRAPHE COMPLET");
        System.out.println("═".repeat(80));

        List<Node> nodes = graph.getNodes();
        List<Edge> edges = graph.getEdges();

        System.out.println("\n📍 NŒUDS (" + nodes.size() + "):");
        System.out.println("─".repeat(80));
        for (Node node : nodes) {
            String exitMarker = node.isExit() ? " 🚪" : "";
            String fireMarker = node.isOnFire() ? " 🔥" : "";
            System.out.printf("  %s%s%s%n", node, exitMarker, fireMarker);
        }

        System.out.println("\n🔗 ARÊTES (" + edges.size() + "):");
        System.out.println("─".repeat(80));
        for (Edge edge : edges) {
            System.out.printf("  %s%n", edge);
        }

        System.out.println("\n" + "═".repeat(80));
    }

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

    private void selectNodeAndDisplay() {
        System.out.println("\n📍 Entrez l'ID du nœud (ou 'q' pour quitter): ");
        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("q")) return;

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

    private void selectEdgeAndDisplay() {
        System.out.println("\n🔗 Entrez l'ID de l'arête (ou 'q' pour quitter): ");
        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("q")) return;

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

    // ─── Node / Edge creation ─────────────────────────────────────────────────

    private void createNodeInteractive() {
        System.out.println("\n➕ AJOUT D'UN NŒUD");

        Double x = readDouble("Position X: ");
        if (x == null) return;

        Double y = readDouble("Position Y: ");
        if (y == null) return;

        boolean useDefaultCapacity = readYesNo("Utiliser la capacité par défaut ? (o/n) [o]: ", true);
        Node createdNode;

        if (useDefaultCapacity) {
            createdNode = graph.createNode(x, y);
        } else {
            Double capacity = readDouble("Capacité du nœud: ");
            if (capacity == null) return;
            createdNode = graph.createNode(x, y, capacity);
        }

        boolean exit = readYesNo("Ce nœud est une sortie ? (o/n) [n]: ", false);
        createdNode.setExit(exit);

        System.out.println("\n✅ Nœud créé: " + createdNode);
    }

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
        if (startId == null) return;

        Integer endId = readInt("ID du nœud de fin: ");
        if (endId == null) return;

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
            if (length == null) return;

            Double width = readDouble("Largeur: ");
            if (width == null) return;

            boolean directed = readYesNo("Arête orientée ? (o/n) [n]: ", false);
            createdEdge = graph.createEdge(startNode, endNode, length, width, directed);
        }

        System.out.println("\n✅ Arête créée: " + createdEdge);
    }

    // ─── Detail views ─────────────────────────────────────────────────────────

    private void displayNodeDetails(Node node) {
        List<Agent> agents = node.getAgents();

        System.out.println("\n📍 NŒUD #" + node.getId());
        System.out.println("   Position: (" + (int) node.getX() + ", " + (int) node.getY() + ")");
        System.out.println("   Sortie: " + (node.isExit() ? "Oui ✅" : "Non"));
        System.out.println("   Arêtes connectées: " + node.getEdges().size());

        System.out.println("\n   ── Occupation ──");
        System.out.println("   Agents présents: " + agents.size());
        System.out.println("   Capacité: " + String.format("%.1f", node.getCapacity()));
        System.out.println("   Espace occupé: " + String.format("%.1f", node.getOccupiedSpace()));
        System.out.println("   Congestion: " + String.format("%.0f%%", node.getCongestion() * 100));
        System.out.println("   Accessible: " + (!node.isFull() ? "Oui ✅" : "Non — plein ❌"));

        System.out.println("\n   ── Agents ──");
        if (agents.isEmpty()) {
            System.out.println("   Vitesse moy.: —");
            System.out.println("   Stress moy.: —");
            System.out.println("   État dominant: —");
        } else {
            double avgSpeed = agents.stream().mapToDouble(Agent::getMaxSpeed).average().orElse(0);
            System.out.println("   Vitesse moy.: " + String.format("%.1f", avgSpeed));
            double avgStress = agents.stream().mapToDouble(Agent::getStressLevel).average().orElse(0);
            System.out.println("   Stress moy.: " + String.format("%.0f%%", avgStress * 100));
            System.out.println("   État dominant: " + getDominantState(agents).name());
        }

        System.out.println("\n   ── Stress global ──");
        System.out.println("   Stress (+ voisins): " + String.format("%.0f%%",
                node.getCachedTotalStressInducedIncludingNeighbors() * 100));

        System.out.println("\n   ── Feu ──");
        if (node.isOnFire()) {
            System.out.println("   🔥 EN FEU");
            if (node.getFire() != null) {
                System.out.println("   Intensité: " + String.format("%.2f", node.getFire().getIntensity()));
                System.out.println("   Fumée: " + String.format("%.2f", node.getFire().getSmokeLevel()));
                System.out.println("   Propagation: " + String.format("%.2f", node.getFire().getSpreadRate()));
                System.out.println("   Brûle depuis: " + node.getFire().getBurningTime() + " ticks");
            }
        } else {
            System.out.println("   Pas de feu ✅");
        }
    }

    private void displayEdgeDetails(Edge edge) {
        List<Agent> agents = edge.getAgents();

        System.out.println("\n🔗 ARÊTE #" + edge.getId());
        System.out.println("   Extrémités: " + edge.getStart().getId() + " → " + edge.getEnd().getId());
        System.out.println("   Dirigée: " + (edge.isDirected() ? "Oui ✅" : "Non"));
        System.out.println("   Longueur: " + String.format("%.1f", edge.getLength()));
        System.out.println("   Largeur: " + String.format("%.1f", edge.getWidth()));
        System.out.println("   Capacité: " + String.format("%.1f", edge.getCapacity()));

        System.out.println("\n   ── Occupation ──");
        System.out.println("   Agents présents: " + agents.size());
        System.out.println("   Espace occupé: " + String.format("%.1f", edge.getOccupiedSpace()));
        System.out.println("   Congestion: " + String.format("%.0f%%", edge.getCongestion() * 100));
        System.out.println("   Accessible: " + (!edge.isFull() ? "Oui ✅" : "Non — plein ❌"));
        System.out.println("   Vitesse max agents: " + String.format("%.2f", edge.getMaxAgentSpeed()));

        System.out.println("\n   ── Agents ──");
        if (agents.isEmpty()) {
            System.out.println("   Aucun agent sur l'arête");
        } else {
            double avgSpeed = agents.stream().mapToDouble(Agent::getMaxSpeed).average().orElse(0);
            System.out.println("   Vitesse moy.: " + String.format("%.1f", avgSpeed));
            double avgStress = agents.stream().mapToDouble(Agent::getStressLevel).average().orElse(0);
            System.out.println("   Stress moy.: " + String.format("%.0f%%", avgStress * 100));
            System.out.println("   État dominant: " + getDominantState(agents).name());
        }

        System.out.println("\n   ── Stress global ──");
        System.out.println("   Stress (+ voisins): " + String.format("%.0f%%",
                edge.getCachedTotalStressInducedIncludingNeighbors() * 100));

        System.out.println("\n   ── Feu ──");
        if (edge.isOnFire()) {
            System.out.println("   🔥 EN FEU");
            if (edge.getFire() != null) {
                System.out.println("   Intensité: " + String.format("%.2f", edge.getFire().getIntensity()));
                System.out.println("   Fumée: " + String.format("%.2f", edge.getFire().getSmokeLevel()));
                System.out.println("   Propagation: " + String.format("%.2f", edge.getFire().getSpreadRate()));
                System.out.println("   Brûle depuis: " + edge.getFire().getBurningTime() + " ticks");
            }
        } else {
            System.out.println("   Pas de feu ✅");
        }
    }

    // ─── Element actions ──────────────────────────────────────────────────────

    private void elementActionsMenu(GraphElement element) {
        boolean inMenu = true;
        while (inMenu) {
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
                case "1" -> { createFireInteractive(element); displaySelectedElementDetails(element); }
                case "2" -> { createAgentInteractive(element); displaySelectedElementDetails(element); }
                case "3" -> { createAgentsBulkInteractive(element); displaySelectedElementDetails(element); }
                case "4" -> displayElementAgents(element);
                case "5" -> displaySelectedElementDetails(element);
                case "0" -> inMenu = false;
                default -> System.out.println("   ❌ Option invalide.");
            }
        }
    }

    private void displaySelectedElementDetails(GraphElement element) {
        if (element instanceof Node node) { displayNodeDetails(node); return; }
        if (element instanceof Edge edge) { displayEdgeDetails(edge); }
    }

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

    private void createFireInteractive(GraphElement element) {
        System.out.println("\n   ➕ AJOUT D'UN FEU");
        boolean useDefaultValues = readYesNo("   Utiliser les valeurs par défaut ? (o/n) [o]: ", true);

        Fire fire;
        if (useDefaultValues) {
            fire = new Fire(0.5, 0.4, 0.2);
        } else {
            Double intensity = readDouble("   Intensité: ");
            if (intensity == null) return;
            Double smokeLevel = readDouble("   Fumée: ");
            if (smokeLevel == null) return;
            Double spreadRate = readDouble("   Propagation: ");
            if (spreadRate == null) return;
            fire = new Fire(intensity, smokeLevel, spreadRate);
        }

        element.setFire(fire);
        System.out.println("\n   ✅ Feu ajouté: intensité=" + String.format("%.2f", fire.getIntensity())
                + ", fumée=" + String.format("%.2f", fire.getSmokeLevel())
                + ", propagation=" + String.format("%.2f", fire.getSpreadRate()));
    }

    /**
     * Ajoute un agent sur l'élément via AgentManager pour qu'il participe à la simulation.
     * Les propriétés de l'agent sont générées aléatoirement par AgentGenerator.
     */
    private void createAgentInteractive(GraphElement element) {
        System.out.println("\n   ➕ AJOUT D'UN AGENT (propriétés aléatoires via la simulation)");
        System.out.print("   Nom/préfixe: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) name = "Agent";

        if (element instanceof Node node) {
            simulation.getAgentManager().generateAgentOnNode(name, node);
        } else if (element instanceof Edge edge) {
            Double progress = readDouble("   Position sur l'arête (0.0–1.0): ");
            if (progress == null) return;
            progress = Math.max(0.0, Math.min(1.0, progress));
            simulation.getAgentManager().generateAgentOnEdge(name, edge, progress);
        }

        System.out.println("\n   ✅ Agent ajouté. Congestion: "
                + String.format("%.0f%%", element.getCongestion() * 100));
    }

    /**
     * Ajoute plusieurs agents sur l'élément via AgentManager.
     */
    private void createAgentsBulkInteractive(GraphElement element) {
        System.out.println("\n   ➕ AJOUT D'AGENTS EN MASSE (propriétés aléatoires via la simulation)");

        Integer count = readInt("   Nombre d'agents à ajouter: ");
        if (count == null || count <= 0) {
            System.out.println("   ❌ Le nombre doit être supérieur à 0.");
            return;
        }

        System.out.print("   Préfixe du nom [Agent]: ");
        String namePrefix = scanner.nextLine().trim();
        if (namePrefix.isEmpty()) namePrefix = "Agent";

        if (element instanceof Node node) {
            simulation.getAgentManager().generateAgentsOnNode(namePrefix, node, count);
        } else if (element instanceof Edge edge) {
            for (int i = 0; i < count; i++) {
                simulation.getAgentManager().generateAgentOnEdge(namePrefix, edge, 0.5);
            }
        }

        System.out.println("\n   ✅ " + count + " agents ajoutés. Congestion: "
                + String.format("%.0f%%", element.getCongestion() * 100));
    }

    // ─── Statistics ───────────────────────────────────────────────────────────

    private void displayStatistics() {
        List<Node> nodes = graph.getNodes();
        List<Edge> edges = graph.getEdges();

        System.out.println("\n" + "═".repeat(80));
        System.out.println("📊 STATISTIQUES GLOBALES");
        System.out.println("═".repeat(80));

        System.out.println("\n   ── Simulation ──");
        System.out.println("   Tick actuel: " + simulation.getCurrentTick());
        System.out.println("   État: " + (simulation.isRunning() ? "▶ En cours" : "⏸ En pause"));
        System.out.println("   Agents actifs: " + simulation.getAgentManager().getAgentsToEvacuate().size());
        System.out.println("   Agents évacués: " + simulation.getAgentManager().getEvacuatedAgents().size());
        System.out.println("   Agents morts: " + simulation.getAgentManager().getDeadAgents().size());
        System.out.println("   Durée dernier tick: " + String.format("%.2f ms", simulation.getLastEngineLoadMs()));

        System.out.println("\n   ── Graphe ──");
        System.out.println("   Nœuds: " + nodes.size());
        System.out.println("   Arêtes: " + edges.size());

        long exits = nodes.stream().filter(Node::isExit).count();
        System.out.println("   Sorties: " + exits);

        long onFire = nodes.stream().filter(Node::isOnFire).count();
        System.out.println("   Nœuds en feu: " + onFire);

        int totalAgents = nodes.stream().mapToInt(n -> n.getAgents().size()).sum();
        System.out.println("   Agents sur nœuds: " + totalAgents);

        double avgCongestion = nodes.stream().mapToDouble(Node::getCongestion).average().orElse(0);
        System.out.println("   Congestion moyenne: " + String.format("%.0f%%", avgCongestion * 100));

        double avgStress = nodes.stream()
                .mapToDouble(Node::getCachedTotalStressInducedIncludingNeighbors)
                .average().orElse(0);
        System.out.println("   Stress moyen: " + String.format("%.0f%%", avgStress * 100));

        System.out.println("\n" + "═".repeat(80));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private EmotionalState getDominantState(List<Agent> agents) {
        int calm = 0, selfish = 0, panicking = 0;
        for (Agent a : agents) {
            switch (a.getEmotionalState()) {
                case CALM -> calm++;
                case SELFISH -> selfish++;
                case PANICKING -> panicking++;
            }
        }
        if (panicking >= calm && panicking >= selfish) return EmotionalState.PANICKING;
        if (selfish >= calm) return EmotionalState.SELFISH;
        return EmotionalState.CALM;
    }

    private Integer readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("q")) return null;
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
            if (input.equalsIgnoreCase("q")) return null;
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
            if (input.isEmpty()) return defaultValue;
            if (input.equals("o") || input.equals("oui") || input.equals("y") || input.equals("yes")) return true;
            if (input.equals("n") || input.equals("non") || input.equals("no")) return false;
            System.out.println("❌ Réponse invalide, utilisez o/n.");
        }
    }
}
