package org.systemj;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import org.systemj.nodes.BaseGRCNode;

public class UglyPrinter {

	private List<BaseGRCNode> nodes;
	private List<DeclaredObjects> declo;
	private String dir;
	private PrintWriter pw;

	public UglyPrinter () {}

	public UglyPrinter(List<BaseGRCNode> nodes) {
		super();
		this.nodes = nodes;
	}

	public UglyPrinter(List<BaseGRCNode> nodes, String dir) {
		super();
		this.dir = dir;
		this.nodes = nodes;
	}

	public List<DeclaredObjects> getDelcaredObjects() {
		return declo;
	}

	public void setDelcaredObjects(List<DeclaredObjects> declo) {
		this.declo = declo;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public boolean hasDir() { return dir != null; }

	public List<BaseGRCNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<BaseGRCNode> nodes) {
		this.nodes = nodes;
	}

	static class Java {
		public static final String CLASS_SIGNAL = "systemj.lib.emb.Signal";
		public static final String CLASS_I_CHANNEL = "systemj.lib.emb.input_Channel";
		public static final String CLASS_O_CHANNEL = "systemj.lib.emb.output_Channel";
	
	}

	public void uglyprint() throws FileNotFoundException {
	
		File f = null;
		if(this.hasDir()){
			f = new File(dir+"/hmpsoc");
			f.mkdirs();
		}
		else{
			f = new File("hmpsoc");
			f.mkdir();
		}
//		pw = new PrintWriter(f);
//		pw.println("www");
//		pw.flush();
//		pw.close();
		
	}

}
