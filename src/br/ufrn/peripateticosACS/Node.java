package br.ufrn.peripateticosACS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class Node {

	public final int id;
	public final double[] coordinate;
	public List<Node> connections;
	
	public Node(int id, double... coordinate) {
		super();
		this.id = id;
		this.coordinate = coordinate;
		this.connections = new ArrayList<Node>();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + Arrays.hashCode(coordinate);
		result = prime * result + connections.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		Node other = (Node) obj;

		if (id != other.id) {
			return false;
		}
		
		if (!Arrays.equals(coordinate, other.coordinate)) {
			return false;
		}
		
		if (!connections.equals(other.connections)) {
			return false;
		}

		return true;
	}
	
}
