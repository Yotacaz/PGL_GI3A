/**
 * Pathfinding module for agents in the simulation.
 * 
 * This package implements the pathfinding system that helps agents find optimal routes
 * from their current location to evacuation exits.
 * 
 * Key components:
 * - Pathfinder: Main entry point providing nearestExit and shortestPath methods
 * - DijkstraAlgorithm: Finds shortest distances to all reachable nodes
 * - DijkstraResult: Container for Dijkstra algorithm results
 * - MapfAlgorithm: Multi-Agent Path Finding with congestion and fire awareness
 * 
 * The pathfinding system considers:
 * - Graph topology (nodes and edges)
 * - Congestion levels on edges and nodes
 * - Fire hazards in the environment
 * - Evacuation exits as destinations
 * 
 * @author GI3A
 * @version 1.0
 */
package fr.cy.model.pathfinding;
