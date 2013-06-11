package br.ufrn.peripateticosACS;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JTextArea;


public class ACS {
	
	/** número de peripatéticos */
	private int m;
	
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
	
	/** flag para saber se usa busca local */
	private boolean withLocalSearch = false; 
	
	/** nós que representam o grafo */
	private List<Node> nodes;
	/** número de nós */
	private int numberOfNodes;
	/** maatriz de feromônios */
	private List<List<Double>> pheromoneMatrix;

	/** as trabalhadoras, digo, formigas */
	private List<Ant> ants;
	
	private JTextArea output; 
	
	public ACS(int m, int iterations, int numberOfAnts, float Q, float alpha, float beta, float q0, float rho, float ksi) {
		this.m = m;
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
	public List<Node> loadNodes(String file){
		file = "./lib/" + file;
		List<Node> nl = new ArrayList<Node>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			
			while ((line = reader.readLine()) != null) {
				String[] v = line.trim().split("\\s+");
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
	
	public void start(String instanceFile) {
		
		this.nodes = loadNodes(instanceFile);
		this.numberOfNodes = this.nodes.size();
		
		long beginTime = System.currentTimeMillis();
		
		double tau0 = 1/(numberOfNodes * nearestNeighborTour(this.nodes));

		for(int i = 0; i < numberOfAnts; i++) {
			ants.add(new Ant(i));
		}
		
		systemStart(tau0);
		
		long endTime = System.currentTimeMillis();
		output.append("Executing time: " + ((endTime-beginTime)/1000) + "s\n");
	}
	
	/** inicia o sistema de colônia de formigas */
	void systemStart(double tau0) {
		
		initializePheromone(tau0);
		
		List<List<Node>> bestCycles = new ArrayList<List<Node>>();
		List<List<Node>> globalBestCycles = new ArrayList<List<Node>>();
		
		double globalBestCyclesLength = 0;
		
		for(int i = 0; i < iterations; i++) {
			if(i % 100 == 0) output.append("\niteração " + (i+1));
			double bestCyclesLength = 0;
			
			bestCycles.clear();
			
			initializeTours();
			
			for(int j = 0; j < numberOfAnts; j++) {
				List<List<Node>> cycles = new ArrayList<List<Node>>();
				double antCycleLength = 0;
				
				Node antInitialNode = ants.get(j).tour.get(0);
						
				for(int k = 0; k < m; k++) {
					tourConstruction(ants.get(j), new ArrayList<Node>(this.nodes));

					//localPheromoneUpdate(ants.get(j), tau0);
					
					cycles.add(new ArrayList<Node>(ants.get(j).tour));
					antCycleLength += ants.get(j).tourLength;
					
					ants.get(j).tour.clear();
					ants.get(j).tourLength = 0;
					
					ants.get(j).tour.add(antInitialNode);
				}
				
				if(withLocalSearch) {
					antCycleLength = localSearch(cycles, antCycleLength);
				}
				
				if(bestCyclesLength == 0 || bestCyclesLength > antCycleLength) {
					bestCycles.clear();
					
					for(List<Node> cycle : cycles) {
						bestCycles.add(new ArrayList<Node>(cycle));
					}
					
					bestCyclesLength = antCycleLength;
					//System.out.println("best until now: " +  antCycleLength);
				}
				
				localPheromoneUpdate(cycles, tau0);
				
				clearNodesConnections(nodes);
			}
			
			//bestCyclesLength = localSearch(bestCycles, bestCyclesLength);
			
			if(globalBestCyclesLength == 0 || globalBestCyclesLength > bestCyclesLength) {
				globalBestCycles.clear();
				
				for(List<Node> cycle : bestCycles) {
					globalBestCycles.add(new ArrayList<Node>(cycle));
				}
				
				globalBestCyclesLength = bestCyclesLength;
				output.append("\nglobal best until now: " +  globalBestCyclesLength);
				
				//printTour(globalBestCycles, globalBestCyclesLength, "Best Tour until now");
			}

			globalPheromoneUpdate(globalBestCycles, globalBestCyclesLength);
		}
		
		printTour(globalBestCycles, globalBestCyclesLength, "Global Best Tour");
	}
	
	/** inicia o tour de cada formiga em um nó aleatoriamente */
	public void initializeTours() {
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
	public double localSearch(List<List<Node>> antCycles, double antCyclesLength) {
		
		double best;
		List<List<Node>> newAntCycles;
		double newAntCyclesLength;
		
		while(true) {
			best = antCyclesLength;

			for(int i = 0; i < antCycles.get(0).size()-1; i++) {
				for(int j = i + 1; j < antCycles.get(0).size(); j++) {
					
					int k = i;
					int l = j;
					newAntCyclesLength = 0;
					
					newAntCycles = new ArrayList<List<Node>>();
					for(List<Node> c : antCycles) {
						newAntCycles.add(c);
					}
					
					while(k < l) {
						
						Node n1 = newAntCycles.get(0).get(k);
						Node n2 = newAntCycles.get(0).get(l);
						
						// swap nas conexões
						List<Node> tempConnections = new ArrayList<Node>(n1.connections);
						n1.connections = new ArrayList<Node>(n2.connections);
						n2.connections = new ArrayList<Node>(tempConnections);
						
						for(List<Node> c : newAntCycles) {
							// swap
							Node temp = c.get(k);
							c.set(k, c.get(l));
							c.set(l, temp);
							
							// está trocando o primeiro elemento?
							if(k == 0) {
								// garante que ainda continuará sendo um ciclo
								c.set(c.size() - 1, c.get(k));
							}
							
							// está trocando o último elemento elemento?
							if(l == antCycles.get(0).size() - 1) {
								c.set(0, c.get(l));
							}
							
							newAntCyclesLength += calculateCost(c);
						}
						
						k += 1;
						l -= 1;
					}
					
					if(newAntCyclesLength < antCyclesLength) {
						antCyclesLength = newAntCyclesLength;
						antCycles = new ArrayList<List<Node>>();

						for(List<Node> c : newAntCycles) {
							antCycles.add(c);
						}
					}
				}
			}
			
			if(best == antCyclesLength) {
				return antCyclesLength;
			}
		}
	}
	
	/** atualiza o feromônio global, ou seja, depois de cada iteração*/
	public void globalPheromoneUpdate(List<List<Node>> globalBestCycles, double globalBestCyclesLength) {
		int current, next;
		
		for(List<Node> cycle : globalBestCycles) {
		
			for(int i = 0; i < cycle.size()-1; i++) {
				current = cycle.get(i).id-1;
				next = cycle.get(i+1).id-1;
				
				pheromoneMatrix.get(current).set(next, (((1 - rho) * pheromoneMatrix.get(current).get(next)) + (Q * (1/globalBestCyclesLength))) );
				pheromoneMatrix.get(next).set(current, pheromoneMatrix.get(current).get(next));
			}
			
		}
	}
	
	/** atualiza o feromônio local, ou seja, depois da formiga percorrer um tour */
	public void localPheromoneUpdate(List<List<Node>> cycles, double tau0) {
		int current, next;
		
		for(List<Node> cycle : cycles) {
			for(int i = 0; i < cycle.size()-1; i++) {
				current = cycle.get(i).id-1;
				next = cycle.get(i+1).id-1;
				
				pheromoneMatrix.get(current).set(next, (((1 - ksi) * pheromoneMatrix.get(current).get(next)) + (Q * tau0)) );
				pheromoneMatrix.get(next).set(current, pheromoneMatrix.get(current).get(next));
			}
		}
	}
	
	/** constroi um tour para uma formiga */
	public void tourConstruction(Ant ant, List<Node> nl) {
		
		Node currentNode = ant.tour.get(0);
		nl.remove(currentNode);
		
		Node nextNode;
		for(int i = 0; i < numberOfNodes-2; i++) {
			nextNode = findNextNode(currentNode, nl);
			
			if(nextNode == null) {
				ant.tour.remove(currentNode);
				nl.add(currentNode);
				
				Node temp = currentNode.connections.get(currentNode.connections.size()-1);
				currentNode.connections.remove(temp);
				temp.connections.remove(currentNode);
				
				currentNode = temp;
				i = i - 2;
				continue;
			}
			
			currentNode.connections.add(nextNode);
			nextNode.connections.add(currentNode);
			
			ant.tour.add(nextNode);
			currentNode = nextNode;
			nl.remove(nextNode);
		}
		
		nl.get(0).connections.add(ant.tour.get(0));
		ant.tour.get(0).connections.add(nl.get(0));
		
		ant.tour.add(nl.get(0));
		ant.tour.add(ant.tour.get(0));
		
		ant.tourLength = calculateCost(ant.tour);
	}
	
	/** sorteia o próximo nó */
	public Node findNextNode(Node currentNode, List<Node> nl) {
		List<Double> tauEtha = new ArrayList<Double>();
		
		double rand = Math.random();
		List<Node> currentNodeNeighbors = getNeighbors(currentNode, nl);
		double totalTauEtha = calculateTauEtha(currentNode, currentNodeNeighbors, tauEtha);
		
		/** existe q0 porcentos de chance de escolher
		 *  o nó de maior feromônio, caso contrário
		 *  faz um sorteio onde quem tem mais feromônio
		 *  tem mais chance de ser escolhido
		 * */
		if(rand < q0) {
			List<Double> temp = new ArrayList<Double>(tauEtha);
			Collections.sort(temp, Collections.reverseOrder());
			
			for(Double d : temp) {
				if( !currentNode.connections.contains(currentNodeNeighbors.get(tauEtha.indexOf(d)))) {
					return currentNodeNeighbors.get(tauEtha.indexOf(d));
				}
			}
		}else {
			double roulette = 0.0;
			rand = Math.random() * totalTauEtha;
			
			for(int i = 0; i < currentNodeNeighbors.size(); i++) {
				roulette += tauEtha.get(i);
				
					if(rand < roulette) {
						return currentNodeNeighbors.get(i);
					}
			}
		}
		
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
	
	/** 
	 * faz uma busca gulosa para encontrar m ciclos. 
	 * pega sempre o vizinho mais próximos. utilizado
	 * para inicializar o feromônio. 
	 * */
	public double nearestNeighborTour(List<Node> nl) {
		double totalTourLength = 0.0;
		
		for(int i = 0; i < m; i++) {
			List<Node> nlist = new ArrayList<Node>(nl);
			List<Node> path = new ArrayList<Node>();
			double tourLength = 0;
			
			Node startingNode = nlist.get(0);
			Node nextNode = null;
			Node remove = null;
			
			path.add(startingNode);
			nlist.remove(startingNode);
			
			while(nlist.size() > 0) {
				List<Node> neighbors = getNeighbors(startingNode, nlist);
				
				double minDistance = Double.MAX_VALUE;
				
				for(Node n : neighbors) {
					double distance = calculateDistance(startingNode, n);
					
					if(distance != 0 && distance < minDistance) {
						minDistance = distance;
						nextNode = n;
						remove = n;
					}
				}
				
				// adiciona nextNode aos nós conectados de startingNode e vice-versa
				startingNode.connections.add(nextNode);
				nextNode.connections.add(startingNode);
				
				startingNode = nextNode;
				nlist.remove(remove);
				path.add(nextNode);
				tourLength += minDistance;
			}
			
			// adiciona nextNode aos nós conectados de startingNode e vice-versa
			nextNode.connections.add(path.get(0));
			path.get(0).connections.add(nextNode);
			
			/* fecha o ciclo */
			path.add(path.get(0));
			tourLength += calculateDistance(nextNode, path.get(0));
			totalTourLength += tourLength;
		}
		
		clearNodesConnections(nl);
		
		return totalTourLength;
	}
	
	/** retorna os vizinhos factíveis */
	public List<Node> getNeighbors(Node node, List<Node> nodeList){
		List<Node> neighbors = new ArrayList<Node>();
		
		for(Node n : nodeList) {
			if(!node.connections.contains(n)) {
				neighbors.add(n);
			}
		}
		
		return neighbors;
	}
	
	/** limpa as conexões feitas por cada nó */
	public void clearNodesConnections(List<Node> nl) {
		for(Node n : nl) {
			n.connections.clear();
		}
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
	
	public void setWithLocalSearch(boolean with) {
		this.withLocalSearch = with;
	}
	
	public void printPheromoneMatrix() {
		System.out.print("[ ");
		for(List<Double> r : pheromoneMatrix) {
			for(Double d : r) {
				System.out.print(d + ", ");
			}
			System.out.println();
		}
		System.out.print(" ]");
	}
	
	public void printTour(List<List<Node>> cycles, double cyclesLength, String title) {
		output.append("\n\n" + title + " Length: " + cyclesLength +"\n");
		int idx = 1;
		
		for(List<Node> c : cycles) {
			output.append("tour " + (idx++) + " : [");
			for(int i = 0; i < c.size(); i++) {
				if(i==0) output.append(" " + c.get(i).id);
				else	 output.append(", "+ c.get(i).id);
			}
			output.append("]\n");
		}
	}
	
	public void setOutput(JTextArea output) {
		this.output = output;
	}

}
