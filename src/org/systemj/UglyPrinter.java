package org.systemj;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.systemj.DeclaredObjects.Channel;
import org.systemj.DeclaredObjects.Signal;
import org.systemj.DeclaredObjects.Var;
import org.systemj.nodes.ActionNode;
import org.systemj.nodes.BaseGRCNode;
import org.systemj.nodes.ForkNode;
import org.systemj.nodes.JoinNode;
import org.systemj.nodes.SwitchNode;

public class UglyPrinter {

	private String target;
	private List<BaseGRCNode> nodes;
	private List<DeclaredObjects> declo;
	private List<List<ActionNode>> acts;
	private String topdir;

	public UglyPrinter () {}

	public UglyPrinter(List<BaseGRCNode> nodes) {
		super();
		this.nodes = nodes;
	}

	public UglyPrinter(List<BaseGRCNode> nodes, String dir) {
		super();
		this.topdir = dir;
		this.nodes = nodes;
	}

	public List<DeclaredObjects> getDelcaredObjects() {
		return declo;
	}

	public void setDelcaredObjects(List<DeclaredObjects> declo) {
		this.declo = declo;
	}

	public String getDir() {
		return topdir;
	}

	public void setDir(String dir) {
		this.topdir = dir;
	}

	public boolean hasDir() { return topdir != null; }

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
			f = new File(topdir+"/"+target);
			f.mkdirs();
		}
		else{
			f = new File(target);
			f.mkdir();
		}
		
		printJavaClass(f);
		
		
//		pw = new PrintWriter(f);
//		pw.println("www");
//		pw.flush();
//		pw.close();
		
	}

	private void printJavaClass(File dir) throws FileNotFoundException {

		printJavaClockDomain(dir);
		printJavaJOPThread(dir);
		printJavaMain(dir);
		
		printASM(dir);
		
	}

	private void printASM(File dir) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(new File(dir, target+".asm"));
		long c = 0;
		List<MemoryPointer> lmp = new ArrayList<MemoryPointer>();
		
		for(int i=0; i<nodes.size(); i++){
			BaseGRCNode bcn = nodes.get(i);
			String cdname = ((SwitchNode)bcn).getCDName();
			DeclaredObjects doo = declo.get(i);
			MemoryPointer mp = new MemoryPointer();
			{
				// ====== Memory Layout ======
				mp.setInputSignalPointer(c++);
				mp.setOutputSignalPointer(c++);
				mp.setDataLockPointer(c);
				long dl = getMaxDataLock(bcn, 1);
				c += dl;
				
				mp.setInternalSignalPointer(c);
				Iterator<Signal> ii = doo.getInternalSignalIterator();
				int counter = 0;
				while(ii.hasNext()){
					Signal sig = ii.next();
					mp.signalMap.put(sig.name, counter++);
				}
				long locs = 0;
				if(counter > mp.WORD_SIZE){
					locs = counter / mp.WORD_SIZE;
					if(!(counter % mp.WORD_SIZE == 0))
						locs++;
				}
				else{
					locs = 1;
				}
				
				c += locs;
				mp.setPreInternalSignalPointer(c);
				c += locs;
				mp.setPreInputSignalPointer(c++);

				ii = doo.getInputSignalIterator();
				counter = 0;
				while(ii.hasNext()){
					mp.insignalMap.put(ii.next().name, counter++);
				}

				mp.setPreOutputSignalPointer(c++);
				ii = doo.getOutputSignalIterator();
				counter = 0;
				while(ii.hasNext()){
					mp.osignalMap.put(ii.next().name, counter++);
				}

				mp.setProgramCounterPointer(c);
				c += dl;
				mp.setTermianteCodePointer(c);
				long numterm = getMaxTermCode(bcn, 1);
				c += numterm;
				
				mp.setSwitchNodePointer(c);
				Set<String> sws = getSwitchSet(bcn);
				Iterator<String> iii = sws.iterator();
				counter = 0;
				while(iii.hasNext()){
					mp.switchMap.put(iii.next(), counter++);
				}
				c += sws.size();

				mp.setLastAddr(c);
			}
			
			
			System.out.println("====== "+cdname+" constructed memory map =====");
			System.out.println("iSignal    :"+mp.getInputSignalPointer());
			System.out.println("oSignal    :"+mp.getOutputSignalPointer());
			System.out.println("DataLock   :"+mp.getDataLockPointer());
			System.out.println("Signal     :"+mp.getInternalSignalPointer());
			if(!mp.signalMap.isEmpty())
				System.out.println(mp.signalMap);
			System.out.println("PreSig     :"+mp.getPreInternalSignalPointer());
			System.out.println("PreISig    :"+mp.getPreInputSignalPointer());
			System.out.println("PreOSig    :"+mp.getPreOutputSignalPointer());
			System.out.println("PC         :"+mp.getProgramCounterPointer());
			System.out.println("Term       :"+mp.getTerminateCodePointer());
			System.out.println("Switch     :"+mp.getSwitchNodePointer());
			System.out.println("LastAddr+1 :"+mp.getLastAddr());
			
			lmp.add(mp);
		}
		
		// Printing ASM
		pw.println("start NOOP");
		for(int i=0; i<lmp.size(); i++){
			MemoryPointer mp = lmp.get(i);
			pw.println("; ====== "+((SwitchNode)nodes.get(i)).getCDName()+" constructed memory map =====");
			pw.println("; iSignal    :"+mp.getInputSignalPointer());
			pw.println("; oSignal    :"+mp.getOutputSignalPointer());
			pw.println("; DataLock   :"+mp.getDataLockPointer());
			pw.println("; Signal     :"+mp.getInternalSignalPointer());
			if(!mp.signalMap.isEmpty())
				pw.println("; "+mp.signalMap);
			pw.println("; PreSig     :"+mp.getPreInternalSignalPointer());
			pw.println("; PreISig    :"+mp.getPreInputSignalPointer());
			pw.println("; PreOSig    :"+mp.getPreOutputSignalPointer());
			pw.println("; PC         :"+mp.getProgramCounterPointer());
			pw.println("; Term       :"+mp.getTerminateCodePointer());
			pw.println("; Switch     :"+mp.getSwitchNodePointer());
			pw.println("; LastAddr+1 :"+mp.getLastAddr());
			
			Iterator<String> iter = mp.switchMap.keySet().iterator();
			while(iter.hasNext()){
				String swname = iter.next();
				String label = swname.toLowerCase()+"@1";
				pw.println("  LDR R0 #"+label);
				pw.println("  STR R0 $"+Long.toHexString(mp.getSwitchNodePointer()+mp.switchMap.get(swname)));
			}
		}
		
		for(int i=0; i<nodes.size(); i++){
			SwitchNode topnode = (SwitchNode) nodes.get(i);
			MemoryPointer mp = lmp.get(i);
			pw.println("HOUSEKEEPING NOOP");
			
			topnode.setTopLevel();
			topnode.weirdPrint(pw, mp, 1);
			
			
		}

		pw.println("ENDPROG");
		pw.flush();
		pw.close();
	}


	private Set<String> getSwitchSet(BaseGRCNode bcn) {
		Set<String> ss = new HashSet<String>();
		if(bcn instanceof SwitchNode){
			ss.add(((SwitchNode) bcn).getStatename());
		}
		
		for(BaseGRCNode child : bcn.getChildren()){
			ss.addAll(getSwitchSet(child));
		}
		return ss;
	}

	private long getMaxTermCode(BaseGRCNode bcn, long i) {
		if(bcn instanceof ForkNode)
			i++;
		else if(bcn instanceof JoinNode)
			i--;

		long temp = i;
		for(BaseGRCNode child : bcn.getChildren()){
			long r = getMaxTermCode(child, i);
			if(r > temp)
				temp = r;
		}
		return temp;

	}

	private long getMaxDataLock(BaseGRCNode bcn, long dl) {
		if(bcn.getThnum() > dl){
			dl = bcn.getThnum();
		}
		
		for(BaseGRCNode child : bcn.getChildren()){
			long r = getMaxDataLock(child, dl);
			if(r > dl)
				dl = r;
		}
		
		return dl;
		
	}

	private void printJavaMain(File dir) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(new File(dir, "RTSMain.java"));
		pw.println("package "+target+";");
		pw.println("import com.jopdesign.io.IOFactory;");
		pw.println("import com.jopdesign.io.SysDevice;");
		pw.println("import com.jopdesign.sys.Startup;");
		pw.println();
		pw.println("public class RTSMain {");
		pw.println("public static void main(String[] arg){");
		pw.println("SysDevice sys = IOFactory.getFactory().getSysDevice();");
		pw.println("for(int i=0; i < sys.nrCpu-1; i++){");
		pw.println("Runnable r = new JOPThread();");
		pw.println("Startup.setRunnable(r, i);");
		pw.println("}");
		pw.println("sys.signal = 1;");
		
		pw.println("\n/* TODO: Parse the LCF(.xml file) and configure RTS (See Ding's work) */\n");
		
		pw.println("while(true){");
		pw.println("\n/* TODO: Check ER reg from ReCOP and perform corresponding housekeeping operations */\n");
		pw.println("}");
		
		pw.println("}");
		pw.println("}");
		pw.flush();
		pw.close();
	}
	
	private void printJavaClockDomain(File dir) throws FileNotFoundException {
		if(acts.size() != declo.size())
			throw new RuntimeException("Error !");
		
		for(int k=0;k<acts.size();k++){
			DeclaredObjects d = declo.get(k);
			String CDName = d.getCDName();
			PrintWriter pw = new PrintWriter(new File(dir, "CD"+k+".java"));
			
			
			pw.println("package "+target+";\n");
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
		
		
	}


	private void printJavaJOPThread(File dir) throws FileNotFoundException{
//		CommandLine cl = Helper.getSingleArgInstance();
//		String jopnum = cl.getOptionValue("j");
//		int numjop = 1;
//		if(jopnum != null){
//			numjop = Integer.valueOf(jopnum);
//		}
		
		PrintWriter pw = new PrintWriter(new File(dir, "JOPThread.java"));
		pw.println("package "+target+";");
		pw.println("/* TODO: import necessary packages */");
		pw.println();
		pw.println("public class JOPThread implements java.lang.Runnable {");
		pw.println("public static int JOP_NUM = "+nodes.size());
		pw.println();
		pw.println("public void run (){");
		pw.println("int cd = 0;");
		pw.println("int casen = 0;");
		pw.println("while(true){");
		pw.println("\n/* TODO: Retrieve cd and case numbers and assign them to 'cd' and 'case', respectively */\n");
		pw.println("switch(cd){");
		
		for(int i=0; i<nodes.size(); i++){
			pw.println("case "+i+":");
			pw.println("CD"+i+".MethodCall_0(casen);");
			pw.println("break;");
			
		}
		
		pw.println("default: throw new RuntimeException(\"Unrecognized CD number :\"+cd);");
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

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

}
