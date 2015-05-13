package org.systemj;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.systemj.DeclaredObjects.Channel;
import org.systemj.DeclaredObjects.Signal;
import org.systemj.DeclaredObjects.Var;
import org.systemj.nodes.ActionNode;
import org.systemj.nodes.BaseGRCNode;

public class UglyPrinter {

	private List<BaseGRCNode> nodes;
	private List<DeclaredObjects> declo;
	private List<List<ActionNode>> acts;
	private String dir;

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
		
		printJavaClass(f);
		
		
//		pw = new PrintWriter(f);
//		pw.println("www");
//		pw.flush();
//		pw.close();
		
	}

	private void printJavaClass(File dir) throws FileNotFoundException {
		if(acts.size() != declo.size())
			throw new RuntimeException("Error !");
		
		for(int k=0;k<acts.size();k++){
			DeclaredObjects d = declo.get(k);
			String CDName = d.getCDName();
			PrintWriter pw = new PrintWriter(new File(dir, "CD"+k+".java"));
			
			
			pw.println("package hmpsoc;\n");
			pw.println("public class CD"+k+"{");
			pw.println("public static final String CDName = \""+CDName+"\";");
			{
				Iterator<Signal> iter = d.getInputSignalIterator();
				while(iter.hasNext()){
					Signal s = iter.next();
					pw.println("private static "+Java.CLASS_SIGNAL+" "+s.name+";");
				}
			}
			{
				Iterator<Signal> iter = d.getOutputSignalIterator();
				while(iter.hasNext()){
					Signal s = iter.next();
					pw.println("private static "+Java.CLASS_SIGNAL+" "+s.name+";");
				}
			}
			{
				Iterator<Signal> iter = d.getInternalSignalIterator();
				while(iter.hasNext()){
					Signal s = iter.next();
					pw.println("private static "+Java.CLASS_SIGNAL+" "+s.name+";");
				}
			}
			{
				Iterator<Channel> iter = d.getInputChannelIterator();
				while(iter.hasNext()){
					Channel s = iter.next();
					pw.println("private static "+Java.CLASS_I_CHANNEL+" "+s.name+";");
				}
			}
			{
				Iterator<Channel> iter = d.getOutputChannelIterator();
				while(iter.hasNext()){
					Channel s = iter.next();
					pw.println("private static "+Java.CLASS_O_CHANNEL+" "+s.name+";");
				}
			}
			{
				Iterator<Var> iter = d.getVarDeclIterator();
				while(iter.hasNext()){
					Var s = iter.next();
					pw.println("private static "+s.type+" "+s.name+";");
				}
			}

			
			List<ActionNode> l = acts.get(k);
			pw.println();
			
			List<List<StringBuilder>> lsb = new ArrayList<List<StringBuilder>>();
			List<StringBuilder> llsb = new ArrayList<StringBuilder>();
			int i = 0;
			for(ActionNode an : l){
				StringBuilder sb = new StringBuilder();
				boolean gen = false;
				switch(an.getActionType()){
				case JAVA:
					if(an.getCasenumber() < 0)
						throw new RuntimeException("Unresolved Action Case");
					sb.append("case "+an.getCasenumber()+":\n");
					if(an.isBeforeTestNode())
						sb.append("return "+an.getStmt()+";\n");
					else{
						sb.append(an.getStmt()+"\n");
						sb.append("break;\n");
					}
					gen = true;
					break;
				case GROUPED_JAVA:
					if(an.getCasenumber() < 0)
						throw new RuntimeException("Unresolved Action Case");
					sb.append("case "+an.getCasenumber()+":\n");
					for(String stmt : an.getStmts()){
						sb.append(stmt+"\n");
					}
					sb.append("break;\n");
					gen = true;
					break;
				case EMIT:
					if(an.hasEmitVal()){
						if(an.getCasenumber() < 0)
							throw new RuntimeException("Unresolved Action Case");
						sb.append("case "+an.getCasenumber()+":\n");
						sb.append(an.getStmt()+"\n");
						sb.append("break;\n");
						gen = true;
					}
					else
						continue;
					break;
				default:
					continue;
				}
				llsb.add(sb);
				i++;
				if(i > 100){
					sb.append("default: return MethodCall_"+(lsb.size()+1)+"(casen);\n");
					lsb.add(llsb);
					llsb = new ArrayList<StringBuilder>();
					i = 0;
				}
			}
			if(i <= 100)
				lsb.add(llsb);
			
			for(int j=0 ; j<lsb.size(); j++){
				List<StringBuilder> ll = lsb.get(j);
				pw.println("public static boolean MethodCall_"+j+"(int casen){");
				pw.println("switch(casen){");
				for(StringBuilder sb : ll){
					pw.print(sb.toString());
				}
				if(j == lsb.size()-1){
					pw.println("default: throw new RuntimeException(\"Unexpected case number \"+casen);");
				}
				pw.println("}");
				pw.println("return false;");
				pw.println("}");
			}

			
			
			
			pw.println("}");
			pw.flush();
			pw.close();
		}
		
		printJavaJOPThread(dir);
		
	}
	
	private void printJavaJOPThread(File dir) throws FileNotFoundException{
//		CommandLine cl = Helper.getSingleArgInstance();
//		String jopnum = cl.getOptionValue("j");
//		int numjop = 1;
//		if(jopnum != null){
//			numjop = Integer.valueOf(jopnum);
//		}
		
		PrintWriter pw = new PrintWriter(new File(dir, "JOPThread.java"));
		pw.println("package hmpsoc;");
		pw.println("/* import necessary packages */");
		pw.println();
		pw.println("public class JOPThread implements Runnable {\n");
		pw.println();
		pw.println("public void run (){");
		pw.println("int cd = 0;");
		pw.println("int case = 0;");
		pw.println("while(true){");
		pw.println("/* Retrieve cd and case numbers and assign them to 'cd' and 'case', respectively */");
		pw.println("switch(cd){");
		
		for(int i=0; i<nodes.size(); i++){
			pw.println("case "+i+":");
			pw.println("CD"+i+".MethodCall_0(case);");
			pw.println("break;");
			pw.println("default: throw new RuntimeException(\"Unrecognized CD number :\"+cd);");
		}
		
		
		pw.println("}");
		pw.println("}");
		pw.println("}");
		pw.println("}");
		
		pw.flush();
		pw.close();
	}

	public List<List<ActionNode>> getActmap() {
		return acts;
	}

	public void setActmap(List<List<ActionNode>> actmap) {
		this.acts = actmap;
	}

}
