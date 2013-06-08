package br.ufrn.peripateticosACS;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

public class ACS {
	
	/** número de iterações */
	private int iterations;
	/** quantidade de formiga */
	private int numberOfAnts;
	
	/** fator de influência do feromônio */
	private float alpha;
	/** fator de influência da distância */
	private float beta;
	
	/** parâmetro de controle para um aleatório proporcional */
	private float q0;
	
	/** coeficiente de evaporação global */
	private float rho;
	/** coeficiente de evaporação local  */
	private float ksi;
	
	/** quantidade de feromônio que uma formiga despeja em um caminho */
	private float Q;
	
	/** nós que representam o grafo */
	private List<Node> nodes;
	/** número de nós */
	private int numberOfNodes;
	/** maatriz de feromônios */
	private List<List<Double>> pheromoneMatrix;
	
	/** as trabalhadoras, digo, formigas */
	private List<Ant> ants;
	
	
	public ACS(int iterations, int numberOfAnts, float Q, float alpha, float beta, float q0, float rho, float ksi) {
		this.iterations = iterations;
		this.numberOfAnts = numberOfAnts;
		this.Q = Q;
		this.alpha = alpha;
		this.beta = beta;
		this.q0 = q0;
		this.rho = rho;
		this.ksi = ksi;
		
		this.pheromoneMatrix = new ArrayList<List<Double>>();
		this.ants = new ArrayList<Ant>();
	}
	
	/** lê o arquivo e carrega os nós */
	public List<Node> loadNodes(){
		String file = JOptionPane.showInputDialog("Qual o nome da instância? (sem extensão)");
		file = "./lib/" + file + ".tsp";
		List<Node> nl = new ArrayList<Node>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			
			while ((line = reader.readLine()) != null) {
				String[] v = line.trim().split(" ");
				Node n;
				if(v.length == 3) {
					n = new Node(
							Integer.valueOf(v[0]), 
							Double.valueOf(v[1]), 
							Double.valueOf(v[2]));
				}else {
					n = new Node(
							Integer.valueOf(v[0]), 
							Double.valueOf(v[1]), 
							Double.valueOf(v[2]), 
							Double.valueOf(v[3]));
				}
				nl.add(n);
			}
			
			reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return nl;
	}
	
	public void start() {
		
		this.nodes = loadNodes();
		this.numberOfNodes = this.nodes.size();
		
		long beginTime = System.currentTimeMillis();
		
		double tau0 = 1/(numberOfNodes * nearestNeighborTour(new ArrayList<Node>(this.nodes)));
		
		for(int i = 0; i < numberOfAnts; i++) {
			ants.add(new Ant(i));
		}
		
		systemStart(tau0);
		
		long endTime = System.currentTimeMillis();
		System.out.println("Executing time: " + ((endTime-beginTime)/1000) + "s");
	}
	
	/** inicia o sistema de colônia de formigas */
	void systemStart(double tau0) {
		
		initializePheromone(tau0);
		
		List<Node> bestTour = new ArrayList<Node>();
		List<Node> globalBestTour = new ArrayList<Node>();
		
		double globalBestTourLength = 0;
		
		for(int i = 0; i < iterations; i++) {
			System.out.println("iteração " + (i+1));
			double bestTourLength = 0;
			
			initializeTours(bestTour);
			
			for(int j = 0; j < numberOfAnts; j++) {
				tourConstruction(ants.get(j), new ArrayList<Node>(this.nodes));
				
				localPheromoneUpdate(ants.get(j), tau0);
				
				Ant a = localSearch(ants.get(j).id, new ArrayList<Node>(ants.get(j).tour), ants.get(j).tourLength);
				ants.get(j).tour = new ArrayList<Node>(a.tour);
				ants.get(j).tourLength = a.tourLength;
				
				if(bestTourLength == 0 || bestTourLength > ants.get(j).tourLength) {
					bestTour = new ArrayList<Node>(ants.get(j).tour);
					bestTourLength = ants.get(j).tourLength;
				}
			}
			
			if(globalBestTourLength == 0 || globalBestTourLength > bestTourLength) {
				globalBestTour = new ArrayList<Node>(bestTour);
				globalBestTourLength = bestTourLength;
				
				//printTour(bestTour, bestTourLength, "Best Tour until now");
			}
			
			globalPheromoneUpdate(globalBestTour, globalBestTourLength);
		}
		
		printTour(globalBestTour, globalBestTourLength, "Global Best Tour");
	}
	
	/** inicia o tour de cada formiga em um nó aleatoriamente */
	public void initializeTours(List<Node> bestTour) {
		bestTour.clear();
		
		List<Integer> randomNodes = new ArrayList<Integer>();
		for(int i = 0; i < numberOfNodes; i++)  
			randomNodes.add(i);
		Collections.shuffle(randomNodes);
		
		for(int i = 0; i < ants.size(); i++) {
			ants.get(i).tour.clear();
			ants.get(i).tourLength = 0;
			
			ants.get(i).tour.add(this.nodes.get( randomNodes.get(i%numberOfAnts)));
		}
	}
	
	/** faz uma busca local no tour de uma formiga e tenta melhorá-lo */
	public Ant localSearch(int antId, List<Node> antTour, double antTourLength) {
		
		Ant ant = new Ant(-1);
		ant.tour = new ArrayList<Node>(antTour);
		ant.tourLength = antTourLength;
		
		double best;
		List<Node> newAntTour;
		double newAntTourLength;
		
		while(true) {
			best = ant.tourLength;

			for(int i = 0; i < ant.tour.size()-1; i++) {
				for(int j = i + 1; j < ant.tour.size(); j++) {
					newAntTour = new ArrayList<Node>(ant.tour);
					int k = i;
					int l = j;
					
					while(k < l) {
						// swap
						Node temp = newAntTour.get(k);
						newAntTour.set(k, newAntTour.get(l));
						newAntTour.set(l, temp);
						
						// está trocando o primeiro elemento?
						if(k == 0) {
							// garante que ainda continuará sendo um ciclo
							newAntTour.set(ant.tour.size() - 1, newAntTour.get(k));
						}
						
						// está trocando o último elemento elemento?
						if(l == ant.tour.size() - 1) {
							newAntTour.set(0, newAntTour.get(l));
						}
						
						k += 1;
						l -= 1;
					}
					
					newAntTourLength = calculateCost(newAntTour);
					
					if(newAntTourLength < ant.tourLength) {
						ant.tourLength = newAntTourLength;
						ant.tour = new ArrayList<Node>(newAntTour);
					}
				}
			}
			
			if(best == ant.tourLength) {
				return ant;
			}
		}
	}
	
	/** atualiza o feromônio global, ou seja, depois de cada iteração*/
	public void globalPheromoneUpdate(List<Node> globalBestTour, double globalBestTourLength) {
		int current, next;
		
		for(int i = 0; i < globalBestTour.size()-1; i++) {
			current = globalBestTour.get(i).id-1;
			next = globalBestTour.get(i+1).id-1;
			
			pheromoneMatrix.get(current).set(next, (((1 - rho) * pheromoneMatrix.get(current).get(next)) + (Q * (1/globalBestTourLength))) );
			pheromoneMatrix.get(next).set(current, pheromoneMatrix.get(current).get(next));
		}
	}
	
	/** atualiza o feromônio local, ou seja, depois da formiga percorrer um tour */
	public void localPheromoneUpdate(Ant ant, double tau0) {
		int current, next;
		
		for(int i = 0; i < ant.tour.size()-1; i++) {
			current = ant.tour.get(i).id-1;
			next = ant.tour.get(i+1).id-1;
			
			pheromoneMatrix.get(current).set(next, (((1 - ksi) * pheromoneMatrix.get(current).get(next)) + (ksi * tau0)) );
			pheromoneMatrix.get(next).set(current, pheromoneMatrix.get(current).get(next));
		}
	}
	
	/** constroi um tour para uma formiga */
	public void tourConstruction(Ant ant, List<Node> nl) {
		Node currentNode = ant.tour.get(0);
		nl.remove(currentNode);
		
		Node nextNode;
		for(int i = 0; i < numberOfNodes-2; i++) {
			nextNode = findNextNode(currentNode, nl);
			ant.tour.add(nextNode);
			currentNode = nextNode;
			nl.remove(nextNode);
		}
		
		ant.tour.add(nl.get(0));
		ant.tour.add(ant.tour.get(0));
		
		ant.tourLength = calculateCost(ant.tour);
	}
	
	/** sorteia o próximo nó */
	public Node findNextNode(Node currentNode, List<Node> nl) {
		List<Double> tauEtha = new ArrayList<Double>();
		
		double rand = Math.random();
		double totalTauEtha = calculateTauEtha(currentNode, nl, tauEtha);
		
		/** existe q0 porcentos de chance de escolher
		 *  o nó de maior feromônio, caso contrário
		 *  faz um sorteio onde quem tem mais feromônio
		 *  tem mais chance de ser escolhido
		 * */
		if(rand < q0) {
			double argmax = Collections.max(tauEtha);
			return nl.get(tauEtha.indexOf(argmax));
		}else {
			double roulette = 0.0;
			rand = Math.random() * totalTauEtha;
			
			for(int i = 0; i < nl.size(); i++) {
				roulette += tauEtha.get(i);
				
				if(rand < roulette) {
					return nl.get(i);
				}
			}
		}
		
		System.out.println("!!!!!!!!!! findNextNode: returned null");
		return null;
		
	}
	
	/** calcula o valor tauEtha usado para o calculo da probabilidade. 
	 *  sum(Txy * NxyˆBeta), onde Nxy é 1/distancia
	 * */
	public double calculateTauEtha(Node currentNode, List<Node> nl, List<Double> tauEtha) {
		double total = 0.0;
		double tauEthaVal = 0.0;
		
		for(int i = 0; i < nl.size(); i++) {
			double distance = calculateDistance(currentNode, nl.get(i));
			
			if(distance != 0) {
				tauEthaVal = Math.pow(pheromoneMatrix.get(currentNode.id-1).get(i), alpha) * Math.pow(1.0/distance, beta);
			}else {
				tauEthaVal = 0.0;
			}
			
			tauEtha.add(tauEthaVal);
			total += tauEthaVal;
		}
		
		return total;
	}
	
	/** inicializa a matriz de feromônios 
	 *  tau0 = 1/(número_de_nos * custo_tour_vizinhos_mais_proximos)
	 * */
	public void initializePheromone(double tau0) {
		
		for(int i = 0; i < this.nodes.size(); i++) {
			pheromoneMatrix.add(new ArrayList<Double>());
			
			for(int j = 0; j < this.nodes.size(); j++) {
				if(i == j) {
					pheromoneMatrix.get(i).add(0.0);
				}else {
					pheromoneMatrix.get(i).add(tau0);
				}
			}
		}
	}
	
	/** tour entre os vizinhos mais próximos para inicializar o feromônio */
	public double nearestNeighborTour(List<Node> nl) {
		
		List<Node> path = new ArrayList<Node>();
		int remove = 0;
		double tourLength = 0;
		Node nextNode= nl.get(0);
		
		Node startingNode = nl.get(nl.size() - 1);
		path.add(startingNode);
		nl.remove(startingNode);
		
		while(nl.size() > 0) {
			double minDistance = calculateDistance(startingNode, nl.get(0));
			remove = 0;
			
			for(int i = 1; i < nl.size(); i++) {
				
				double distance = calculateDistance(startingNode, nl.get(i));
				
				if(distance != 0 && distance < minDistance) {
					minDistance = distance;
					nextNode = nl.get(i);
					remove = i;
				}
			}
			
			startingNode = nextNode;
			nl.remove(remove);
			path.add(nextNode);
			tourLength += minDistance;
		}
		
		/* fecha o ciclo */
		path.add(path.get(0));
		tourLength += calculateDistance(nextNode, path.get(0));

		return tourLength;
	}
	
	/** calcula o custo de um tour */
	private double calculateCost(List<Node> antTour) {
		double antTourLength = 0.0;
		
		for(int i = 0; i < antTour.size()-1; i ++) {
			antTourLength += calculateDistance(antTour.get(i), antTour.get(i+1));
		}
		
		return antTourLength;
	}
	
	/** calcula distância euclidiana entre 2 nós */
	public double calculateDistance(Node n1, Node n2) {
		double result = 0.0;
		
		for (int i = 0; i < n1.coordinate.length; i++) {
			result += Math.pow(n1.coordinate[i] - n2.coordinate[i], 2.0);
		}

		return Math.sqrt(result);
	}

	public void printTour(List<Node> tour, double tourLength, String title) {
		System.out.println(title + " Length: " + tourLength);
		
		System.out.print("tour: [");
		for(int i = 0; i < tour.size(); i++) {
			if(i==0)System.out.print(tour.get(i).id);
			else	System.out.print(", "+ tour.get(i).id);
		}
		System.out.println("]");
	}
}
