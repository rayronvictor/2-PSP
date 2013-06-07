package br.ufrn.peripateticosACS;

import java.util.Arrays;

public class Node {

	public final int id;
	public final double[] coordinate;
	
	public Node(int id, double... coordinate) {
		super();
		this.id = id;
		this.coordinate = coordinate;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + Arrays.hashCode(coordinate);
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

		return true;
	}
	
}
