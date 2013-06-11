package br.ufrn.peripateticosACS;

import java.io.File;
import java.io.FilenameFilter;

public class OnlyExt implements FilenameFilter {
	String ext;
	
	public OnlyExt(String ext) {
		this.ext = ("." + ext).toLowerCase();
	}
	
	@Override
	public boolean accept(File dir, String name) {
		return name.toLowerCase().endsWith(ext);
	}

}
