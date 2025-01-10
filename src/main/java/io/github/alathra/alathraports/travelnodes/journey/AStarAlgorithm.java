package io.github.alathra.alathraports.travelnodes.journey;

import io.github.alathra.alathraports.travelnodes.ports.Port;

import java.util.*;

public class AStarAlgorithm {

	public static ArrayList<Port> findShortestPath(Port origin, Port destination) {
		Set<Port> visited = new HashSet<>();
		Map<Port, Double> gScores = new HashMap<>();
		Map<Port, Double> fScores = new HashMap<>();
		Map<Port, Port> cameFrom = new HashMap<>();

		PriorityQueue<Port> openSet = new PriorityQueue<>(1, new Comparator<Port>() {

			@Override
			public int compare(Port o1, Port o2) {
				return Double.compare(fScores.get(o1), fScores.get(o2));
			}

		});
		openSet.add(origin);

		gScores.put(origin, 0.0);
		fScores.put(origin, heuristicCost(origin, destination));

		while (!openSet.isEmpty()) {
			Port current = openSet.poll();

			if (current.equals(destination)) {
				return reconstructPath(cameFrom, destination);
			}

			visited.add(current);

			for (Port neighbor : current.getPortsInRange()) {
				if (neighbor == null) continue;

				if (visited.contains(neighbor)) {
					continue;
				}

				double tentativeGScore = gScores.get(current) + current.distanceTo(neighbor);
				double tentativeFScore = tentativeGScore + heuristicCost(neighbor, destination);

				if (!openSet.contains(neighbor)) {
					fScores.put(neighbor, tentativeFScore);
					openSet.add(neighbor);
				} else if (tentativeGScore >= gScores.get(neighbor)) {
					continue;
				}

				cameFrom.put(neighbor, current);
				gScores.put(neighbor, tentativeGScore);
				fScores.put(neighbor, tentativeFScore);
			}

		}

		return null;
	}

	/**
	 * As the A* algorithm does not keep tract of pathing, this goes backward from the end to
	 * find the route which was taken.
	 *
	 * @param cameFrom - Array of backwards ports
	 * @param current  - Current port
	 * @return List of ports from start to finish.
	 */
	private static ArrayList<Port> reconstructPath(Map<Port, Port> cameFrom, Port current) {
		ArrayList<Port> path = new ArrayList<>();
		path.add(current);

		while (cameFrom.containsKey(current)) {
			current = cameFrom.get(current);
			path.addFirst(current);
		}

		return path;
	}

	/**
	 * Estimated cost, must be less than the true costs to function.
	 *
	 * @param a - Port a (current)
	 * @param b - Port b (next/goal)
	 * @return estimated distance
	 */
	private static double heuristicCost(Port a, Port b) {
		double costMultiplier = Math.sqrt(1.0 / Math.min(a.getSize().getTier(), b.getSize().getTier()));
		return a.distanceTo(b) * costMultiplier;
	}

}
