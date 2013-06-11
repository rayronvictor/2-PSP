package br.ufrn.peripateticosACS;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Main extends JFrame implements ActionListener, ListSelectionListener{
	
	private JPanel mainPane;
	
	private JTextField txtIterations;
	private JTextField txtNumberOfAnts;
	private JTextField txtQ;
	private JTextField txtAlpha;
	private JTextField txtBeta;
	private JTextField txtQ0;
	private JTextField txtRho;
	private JTextField txtKsi;
	
	private JCheckBox cbWithLocalSearch;
	
	private JList listOptions;
	private JButton btnStart;
	private JTextArea txtOutput;
	private String instanceFile;
	
	
	public Main() {
		super("PCV 2-Peripatéticos");
		
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		mainPane = new JPanel();
		mainPane = new JPanel();
		mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainPane.setLayout(new BorderLayout(0, 0));
		setContentPane(mainPane);
		
		initComponents();
		
		this.pack();
		this.setVisible(true);
	}
	
	public void initComponents() {
		listOptions = new JList( new ListOfInstances(load()));
		listOptions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listOptions.addListSelectionListener(this);

		JScrollPane listOptionsScrollPane = new JScrollPane(listOptions);
		listOptionsScrollPane.setMaximumSize(new Dimension(200, 150));
		listOptionsScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		
		JPanel instancesPane = new JPanel();
		instancesPane.setBorder(new EmptyBorder(0, 50, 0, 0));
		instancesPane.setLayout(new BoxLayout(instancesPane, BoxLayout.Y_AXIS));
		
		btnStart = new JButton("Iniciar");
		btnStart.addActionListener(this);
		
		instancesPane.add(new JLabel("intâncias"));
		instancesPane.add(listOptionsScrollPane);
		instancesPane.add(Box.createGlue());
		instancesPane.add(btnStart);
		
		mainPane.add(instancesPane, BorderLayout.EAST);
		
		JPanel optionsPane = new JPanel(new GridLayout(0, 2));
		
		JLabel lbl1 = new JLabel("iterações: ");
		optionsPane.add(lbl1);
		
		txtIterations = new JTextField("150");
		optionsPane.add(txtIterations);
		
		JLabel lbl2 = new JLabel("n. formigas: ");
		optionsPane.add(lbl2);
		
		txtNumberOfAnts = new JTextField("25");
		optionsPane.add(txtNumberOfAnts);
		
		JLabel lbl3 = new JLabel("Q: ");
		optionsPane.add(lbl3);
		
		txtQ = new JTextField("1");
		optionsPane.add(txtQ);

		JLabel lbl4 = new JLabel("alpha: ");
		optionsPane.add(lbl4);
		
		txtAlpha = new JTextField("1");
		optionsPane.add(txtAlpha);
		
		JLabel lbl5 = new JLabel("beta: ");
		optionsPane.add(lbl5);
		
		txtBeta = new JTextField("2");
		optionsPane.add(txtBeta);
		
		JLabel lbl6 = new JLabel("q0: ");
		optionsPane.add(lbl6);
		
		txtQ0 = new JTextField("0.9");
		optionsPane.add(txtQ0);
		
		JLabel lbl7 = new JLabel("rho: ");
		optionsPane.add(lbl7);
		
		txtRho = new JTextField("0.7");
		optionsPane.add(txtRho);
		
		JLabel lbl8 = new JLabel("ksi: ");
		optionsPane.add(lbl8);
		
		txtKsi = new JTextField("0.7");
		optionsPane.add(txtKsi);
		
		JLabel lbl9 = new JLabel("busca local: ");
		optionsPane.add(lbl9);
		
		cbWithLocalSearch = new JCheckBox();
		optionsPane.add(cbWithLocalSearch);
		
		mainPane.add(optionsPane, BorderLayout.CENTER);
		
		txtOutput = new JTextArea(20, 0);
		txtOutput.setEditable(false);
		
		JScrollPane outputScrollPane = new JScrollPane(txtOutput);
		outputScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		
		mainPane.add(outputScrollPane, BorderLayout.SOUTH);
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnStart) {
			iniciar();
		}
		
	}
	
	public void iniciar() {
		Thread thread = new Thread() {
			
			@Override
			public void run() {
				if(instanceFile != null && !instanceFile.equals("")) {
					txtOutput.setText("");
					btnStart.setEnabled(false);
					
					ACS acs = new ACS(
							2, 												// m-peripatéticos, com m=2
							Integer.valueOf(txtIterations.getText()),		// iterações
							Integer.valueOf(txtNumberOfAnts.getText()),		// número de formigas
							Float.valueOf(txtQ.getText()),					// Q, quantidade de feromônio depositado
							Float.valueOf(txtAlpha.getText()),				// alpha, influência do feromônio
							Float.valueOf(txtBeta.getText()),				// beta, influência da distância
							Float.valueOf(txtQ0.getText()),					// q0, controle para um aleatório proporcional
							Float.valueOf(txtRho.getText()),				// rho, evaporação global
							Float.valueOf(txtKsi.getText()));				// ksi, evaporação local
					
					acs.setWithLocalSearch(cbWithLocalSearch.isSelected());
					acs.setOutput(txtOutput);
					acs.start(instanceFile);
					
					btnStart.setEnabled(true);
				}else {
					JOptionPane.showMessageDialog(null, "Selecione uma instância!");
				}
			}
		};
		thread.start();
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e.getSource() == listOptions && e.getValueIsAdjusting()) {
			instanceFile = (String)listOptions.getSelectedValue();
		}
	}
	
	public static File[] load() {
		try {
			String applicationPath = new File(".").getCanonicalPath();
			File folder = new File(applicationPath + "/lib");
			File[] files = folder.listFiles(new OnlyExt("tsp"));
			
			return files;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Main m = new Main();
	}
	

}
