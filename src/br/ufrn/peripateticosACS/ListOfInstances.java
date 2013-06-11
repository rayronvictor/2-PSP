package br.ufrn.peripateticosACS;

import java.io.File;

import javax.swing.AbstractListModel;

public class ListOfInstances extends AbstractListModel {
	private static final long serialVersionUID = 2420574751253576806L;
	
	private File[] instances;
	
	public ListOfInstances(File[] instances) {
		this.instances = instances;
	}
	
	@Override
	public Object getElementAt(int index) {
		return instances[index].getName();
	}

	@Override
	public int getSize() {
		return instances.length;
	}
	
}
