package org.systemj;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.systemj.DeclaredObjects.Channel;
import org.systemj.DeclaredObjects.Signal;
import org.systemj.DeclaredObjects.Var;
import org.systemj.nodes.ActionNode;
import org.systemj.nodes.BaseGRCNode;

public class UglyPrinter {

	private List<BaseGRCNode> nodes;
	private List<DeclaredObjects> declo;
	private Map<Integer,List<ActionNode>> actmap;
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
		
		printJavaClass(f);
		
		
//		pw = new PrintWriter(f);
//		pw.println("www");
//		pw.flush();
//		pw.close();
		
	}

	private void printJavaClass(File dir) throws FileNotFoundException {
		Iterator<Integer> it = actmap.keySet().iterator();
		while(it.hasNext()){
			int jopid = it.next();
			pw = new PrintWriter(new File(dir, "JOP"+jopid+".java"));
			
			pw.println("package hmpsoc;\n");
			pw.println("public class JOP"+jopid+"{");
			for(DeclaredObjects d : declo){
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
			}
			List<ActionNode> l = actmap.get(jopid);
			pw.println();
			
			List<List<StringBuilder>> lsb = new ArrayList<List<StringBuilder>>();
			List<StringBuilder> llsb = new ArrayList<StringBuilder>();
			int i = 0;
			for(ActionNode an : l){
				if(an.getCasenumber() < 0)
					throw new RuntimeException("Unresolved Action Case");
				StringBuilder sb = new StringBuilder();
				boolean gen = false;
				switch(an.getActionType()){
				case JAVA:
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
					sb.append("case "+an.getCasenumber()+":\n");
					for(String stmt : an.getStmts()){
						sb.append(stmt+"\n");
					}
					sb.append("break;\n");
					gen = true;
					break;
				case EMIT:
//					if(an.hasEmitVal()){
//						sb.append("hoho");
//						sb.append("case "+an.getCasenumber()+":\n");
//						sb.append(an.getEmitVal());
//					}
					break;
				case SIG_DECL:
					break;
				}
				llsb.add(sb);
				i++;
				if(i > 100){
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
				pw.println("}");
				pw.println("}");
			}

			
			
			
			pw.println("}");
			pw.flush();
			pw.close();
		}
		
	}

	public Map<Integer, List<ActionNode>> getActmap() {
		return actmap;
	}

	public void setActmap(Map<Integer, List<ActionNode>> actmap) {
		this.actmap = actmap;
	}

}
