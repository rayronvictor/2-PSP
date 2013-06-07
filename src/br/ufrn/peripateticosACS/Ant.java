package br.ufrn.peripateticosACS;

import java.util.ArrayList;
import java.util.List;

public class Ant {
	
	public int id;
	public List<Node> tour;
	public double tourLength; 
	
	public Ant(int antId) {
		this.id = antId;
		this.tour = new ArrayList<Node>();
		this.tourLength = 0;
	}
}
