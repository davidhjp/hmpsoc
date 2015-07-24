package org.systemj;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
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

import args.Helper;
import org.systemj.util.IndentPrinter;

public class UglyPrinter {

	private String target;
	private List<BaseGRCNode> nodelist;
	private List<DeclaredObjects> declolist;
	private List<List<ActionNode>> acts;
	private String topdir;

	public UglyPrinter () {}

	public UglyPrinter(List<BaseGRCNode> nodes) {
		super();
		this.nodelist = nodes;
	}

	public UglyPrinter(List<BaseGRCNode> nodes, String dir) {
		super();
		this.topdir = dir;
		this.nodelist = nodes;
	}

	public List<DeclaredObjects> getDelcaredObjects() {
		return declolist;
	}

	public void setDelcaredObjects(List<DeclaredObjects> declo) {
		this.declolist = declo;
	}

	public String getDir() {
		return topdir;
	}

	public void setDir(String dir) {
		this.topdir = dir;
	}

	public boolean hasDir() { return topdir != null; }

	public List<BaseGRCNode> getNodes() {
		return nodelist;
	}

	public void setNodes(List<BaseGRCNode> nodes) {
		this.nodelist = nodes;
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
		
		printFiles(f);
		
		
//		pw = new PrintWriter(f);
//		pw.println("www");
//		pw.flush();
//		pw.close();
		
	}

	private void printFiles(File dir) throws FileNotFoundException {

		printJavaJOPThread(dir);
		printJavaMain(dir);
		printASM(dir);
		
	}

	private void printASM(File dir) throws FileNotFoundException {
		if(nodelist.size() != declolist.size())
			throw new RuntimeException("Internal Error: nodelist size != declolist size");
		
		// Allocates CDs
		List<List<BaseGRCNode>> allocnodes = new ArrayList<List<BaseGRCNode>>();
		for(int i=0 ; i<Helper.pMap.nReCOP ; i++){
			allocnodes.add(new ArrayList<BaseGRCNode>());
		}
		
		for(int i=0;i<nodelist.size(); i++){
			BaseGRCNode n = nodelist.get(i);
			DeclaredObjects doo = declolist.get(i);
			if(Helper.pMap.nReCOP > 1){
				SwitchNode sw = (SwitchNode)n;
				Integer id = Helper.pMap.rAlloc.get(sw.getCDName());
				if(id == null)
					throw new RuntimeException("Could not find CD name: "+sw.getCDName());
				allocnodes.get(id).add(sw);
			}
			else{
				allocnodes.get(0).add(n);
			}
		}
		
		
	
		for(int o=0;o<allocnodes.size(); o++){
			List<BaseGRCNode> nodes = allocnodes.get(o);
			if(nodes.isEmpty())
				continue;
			
			PrintWriter pw = new PrintWriter(new File(dir, target+"_R"+(o+1)+".asm"));
			long c = 0;
			List<MemoryPointer> lmp = new ArrayList<MemoryPointer>();
			

			for(int i=0; i<nodes.size(); i++){
				BaseGRCNode bcn = nodes.get(i);
				String cdname = ((SwitchNode)bcn).getCDName();
				int cdid = ((SwitchNode)bcn).getCDid();
				DeclaredObjects doo = declolist.get(cdid);
				MemoryPointer mp = new MemoryPointer();
				if(!bcn.isTopLevel())
					throw new RuntimeException(""+bcn.getClass()+" must be top-level");
				mp.setToplevelThnum(bcn.getThnum());
				{
					// ====== Memory Layout ======
					mp.setInputSignalPointer(c++);
					mp.setOutputSignalPointer(c++);
					mp.setDataLockPointer(c);
					int dl = getMaxDataLock(bcn, 1, mp.getToplevelThnum()-1);
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
			pw.println("  LDR R11 #0; Content of R11 is always ZERO");
			for(int i=0; i<nodes.size(); i++){
				SwitchNode topnode = (SwitchNode) nodes.get(i);
				int cdi = topnode.getCDid();
				MemoryPointer mp = lmp.get(i);

				pw.println("RUN"+i+" NOOP");
				pw.println("  LDR R7 #"+cdi+"; Current CD number");

				for(long j=0; j<mp.getSizeTerminateCode(); j++)
					pw.println("  STR R11 $"+Long.toHexString(mp.getTerminateCodePointer()+j)+"; Clearing TerminateNode");
				pw.println("  LDR R1 #$"+Long.toHexString(mp.getProgramCounterPointer())+"; Pointer to PC");
				pw.println("  JMP DCHECK"+i+"; Jump to the last execution point");
				pw.println("DCHECKCONT"+i+" ADD R1 R1 #1");
				pw.println("  SUBV R0 R1 #"+Long.toHexString((mp.getSizeProgramCounter()+mp.getProgramCounterPointer()))+"; Next DS loc");
				pw.println("  PRESENT R0 HOUSEKEEPING"+i);
				pw.println("DCHECK"+i+" LDR R0 R1; Loading the PC");
				pw.println("  PRESENT R0 DCHECKCONT"+i);
				pw.println("  JMP R0");


				pw.println("HOUSEKEEPING"+i+" CLFZ");
				pw.println("  LER R0; Checking whether reactive-interface-JOP is ready");
				pw.println("  PRESENT R0 HOUSEKEEPING"+i);
				pw.println("  SEOT; JOP is ready!");
				pw.println("  CER");
				pw.println("  LDR R0 $"+Long.toHexString(mp.getOutputSignalPointer())+"; Loading OSigs");
				pw.println("; TODO: Send OSig vals (R0) to JOP"); // TODO
				pw.println("  DCALLNB R0 #" + (0x8000 | cdi) + " ; EOT Datacall ; Format = 1|IO-JOP|CD-ID|OSigs");
				pw.println("  STR R11 $"+Long.toHexString(mp.getOutputSignalPointer())+"; Reseting to zero");
				pw.println("  LDR R0 $"+Long.toHexString(mp.getInputSignalPointer()));
				pw.println("  STR R11 $"+Long.toHexString(mp.getInputSignalPointer()));
				pw.println("  STR R0 $"+Long.toHexString(mp.getPreInputSignalPointer())+"; Updating PreISig");
				pw.println("  LDR R0 $"+Long.toHexString(mp.getOutputSignalPointer()));
				pw.println("  STR R11 $"+Long.toHexString(mp.getOutputSignalPointer()));
				pw.println("  STR R0 $"+Long.toHexString(mp.getPreOutputSignalPointer())+"; Updating PreOSig");
				for(long j=0; j<mp.getSizeInternalSignal(); j++){
					pw.println("  LDR R0 $"+Long.toHexString((mp.getInternalSignalPointer()+j)));
					pw.println("  STR R11 $"+Long.toHexString(mp.getInternalSignalPointer()+j));
					pw.println("  STR R0 $"+Long.toHexString((mp.getPreInternalSignalPointer()+j))+"; Updating PreSig");
				}
				for(long j=0; j<mp.getSizeProgramCounter(); j++){
					pw.println("  STR R11 $"+Long.toHexString((mp.getProgramCounterPointer()+j))+"; PC");
				}
				pw.println("; TODO: Get ISig vals from JOP (I am expecting them to be stored in R0)"); // TODO
				pw.println("  STR R0 $"+Long.toHexString(mp.getInputSignalPointer())+"; Updating ISig");
				pw.println("  STR R11 $"+Long.toHexString(mp.getDataLockPointer())+"; Locking this thread");
				pw.println("  LDR R0 #$8000");
				pw.println("  DCALLNB R0; Sending casenumber 0 (housekeeing)");
				pw.println("LOCK"+mp.cc+"ITER"+i+" LDR R0 $"+Long.toHexString(mp.getDataLockPointer()));
				pw.println("  PRESENT R0 "+"LOCK"+(mp.cc++)+"ITER"+i+"; Blocking until housekeeping is done");
				pw.println("  CEOT; Clearing EOT register");


				topnode.weirdPrint(pw, mp, 0, cdi);

				if(i == nodes.size()-1)
					pw.println("AJOIN"+cdi+" JMP RUN0");
				else
					pw.println("AJOIN"+cdi+" JMP RUN"+(i+1));
				
				
				printJavaClockDomain(dir, mp, cdi);

			}

			pw.println("ENDPROG");
			pw.flush();
			pw.close();
		}
		
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

	private int getMaxDataLock(BaseGRCNode bcn, int dl, int ttnum) {
		if(bcn.getThnum() > dl){
			dl = bcn.getThnum() - ttnum;
		}
		
		for(BaseGRCNode child : bcn.getChildren()){
			int r = getMaxDataLock(child, dl, ttnum);
			if(r > dl)
				dl = r;
		}
		
		return dl;
		
	}

	private void printJavaMain(File dir) throws FileNotFoundException {
		IndentPrinter pw = new IndentPrinter(new PrintWriter(new File(dir, "RTSMain.java")));
		pw.println("package "+target+";");
		pw.println("import com.jopdesign.io.IOFactory;");
		pw.println("import com.jopdesign.io.SysDevice;");
		pw.println("import com.jopdesign.sys.Startup;");
		pw.println();
		for (int i = 1; i <= nodelist.size(); i++) {
			pw.println("import static hmpsoc.CD" + i + ".*;");
		}
		pw.println();
		pw.println("public class RTSMain {");
		pw.incrementIndent();
		pw.println("public static void main(String[] arg){");
		pw.incrementIndent();

		pw.println();
		pw.println("/* TODO: Parse the LCF(.xml file) and configure RTS (See Ding's work) */"); // TODO
		pw.println();
		pw.println("init_all();");

		pw.println("SysDevice sys = IOFactory.getFactory().getSysDevice();");
		pw.println("for(int i=0; i < sys.nrCpu-1; i++){");
		pw.incrementIndent();
		pw.println("Runnable r = new JOPThread();");
		pw.println("Startup.setRunnable(r, i);");
		pw.decrementIndent();
		pw.println("}");
		pw.println("sys.signal = 1;");

		pw.println("while(true){");
		pw.incrementIndent();
		pw.println("/* TODO: Check ER reg from ReCOP and perform corresponding housekeeping operations */"); // TODO
		pw.decrementIndent();
		pw.println("}");

		pw.decrementIndent();
		pw.println("}");

		pw.println();

		pw.println("public static void init_all() {");
		pw.incrementIndent();

		for (int i = 1; i <= nodelist.size(); i++) {
			pw.println("CD" + i + ".init();");
		}

		for (DeclaredObjects d : declolist) {
			pw.println("// Init for " + d.getCDName());
			for (Iterator<Channel> it = d.getInputChannelIterator(); it.hasNext();) {
				Channel c = it.next();
				pw.println(c.name + "_in.set_partner(" + c.name + "_o);");
			}
			for (Iterator<Channel> it = d.getOutputChannelIterator(); it.hasNext();) {
				Channel c = it.next();
				pw.println(c.name + "_o.set_partner(" + c.name + "_in);");
			}
		}

		// TODO Set up CAN/PCOMM interfaces
		pw.println("// TODO Set up CAN/PCOMM interfaces");

		pw.decrementIndent();
		pw.println("}");
		pw.decrementIndent();
		pw.println("}");
		pw.flush();
		pw.close();
	}
	
	private void printJavaClockDomain(File dir, MemoryPointer mp, int cdi) throws FileNotFoundException {
		if(acts.size() != declolist.size())
			throw new RuntimeException("Error !");
		DeclaredObjects d = declolist.get(cdi);
		String CDName = d.getCDName();
		Integer recopId = Helper.pMap.rAlloc != null ? Helper.pMap.rAlloc.get(CDName) : 0;
		if (recopId == null)
			throw new RuntimeException("Could not find CD name: "+CDName);
		IndentPrinter pw = new IndentPrinter(new PrintWriter(new File(dir, "CD"+(cdi+1)+".java")));

		pw.println("package "+target+";\n");
		pw.println("public class CD"+(cdi+1)+"{");

		pw.incrementIndent();

		{
			pw.println("public static final String CDName = \"" + CDName + "\";");
			pw.println("public static final int recopId = " + recopId + ";");
		}
		{
			Iterator<Signal> iter = d.getInputSignalIterator();
			while(iter.hasNext()){
				Signal s = iter.next();
				pw.println("public static "+Java.CLASS_SIGNAL+" "+s.name+";");
			}
		}
		{
			Iterator<Signal> iter = d.getOutputSignalIterator();
			while(iter.hasNext()){
				Signal s = iter.next();
				pw.println("public static "+Java.CLASS_SIGNAL+" "+s.name+";");
			}
		}
		{
			Iterator<Signal> iter = d.getInternalSignalIterator();
			while(iter.hasNext()){
				Signal s = iter.next();
				// TODO Check if internal pure signals are actually required to be created jop side
				if (s.type == null) pw.print("//");
				pw.println("public static "+Java.CLASS_SIGNAL+" "+s.name+";");
			}
		}
		{
			Iterator<Channel> iter = d.getInputChannelIterator();
			while(iter.hasNext()){
				Channel s = iter.next();
				pw.println("public static "+Java.CLASS_I_CHANNEL+" "+s.name+"_in;");
			}
		}
		{
			Iterator<Channel> iter = d.getOutputChannelIterator();
			while(iter.hasNext()){
				Channel s = iter.next();
				pw.println("public static "+Java.CLASS_O_CHANNEL+" "+s.name+"_o;");
			}
		}
		{
			Iterator<Var> iter = d.getVarDeclIterator();
			while(iter.hasNext()){
				Var s = iter.next();
				pw.println("public static "+s.type+" "+s.name+";");
			}
		}

		pw.println();

		pw.println("public static void init() {");
		pw.incrementIndent();
		for (Iterator<Signal> it = d.getInputSignalIterator(); it.hasNext();) {
			Signal s = it.next();
			pw.println(s.name + " = new " + Java.CLASS_SIGNAL + "();");
		}
		for (Iterator<Signal> it = d.getOutputSignalIterator(); it.hasNext();) {
			Signal s = it.next();
			pw.println(s.name + " = new " + Java.CLASS_SIGNAL + "();");
		}
		for (Iterator<Signal> it = d.getInternalSignalIterator(); it.hasNext();) {
			Signal s = it.next();
			// TODO Check if internal pure signals are actually required to be created jop side
			if (s.type == null) pw.print("//");
			pw.println(s.name + " = new " + Java.CLASS_SIGNAL + "();");
		}
		for (Iterator<Channel> it = d.getInputChannelIterator(); it.hasNext();) {
			Channel c = it.next();
			pw.println(c.name + "_in = new " + Java.CLASS_I_CHANNEL + "();");
		}
		for (Iterator<Channel> it = d.getOutputChannelIterator(); it.hasNext();) {
			Channel c = it.next();
			pw.println(c.name + "_o = new " + Java.CLASS_O_CHANNEL + "();");
		}
		pw.decrementIndent();
		pw.println("}");

		pw.println();


		List<ActionNode> l = acts.get(cdi);
		pw.println();

		//List<List<StringBuilder>> lsb = new ArrayList<List<StringBuilder>>();
		//List<StringBuilder> llsb = new ArrayList<StringBuilder>();

		pw.println("public static boolean MethodCall_0(int casen) {");
		pw.incrementIndent();
		pw.println("switch (casen) {");
		pw.incrementIndent();

		Iterator<ActionNode> nodeIterator = l.iterator();
		boolean caseGen = false;
		int numCasesGened = 0;
		while (nodeIterator.hasNext()) {
			ActionNode an = nodeIterator.next();
			int methodNum = numCasesGened / 100;
			boolean genMethod = caseGen && numCasesGened % 100 == 0;

			if (genMethod) {
				pw.println("default: return MethodCall_" + (methodNum+1) + "(casen);");
				pw.println("}"); // Switch end
				pw.decrementIndent();
				pw.println("}"); // Method end
				pw.decrementIndent();
				pw.println();

				pw.println("public static boolean MethodCall_" + methodNum + "(int casen) {");
				pw.incrementIndent();
				pw.println("switch (casen) {");
				pw.incrementIndent();
			}

			switch(an.getActionType()) {
				case JAVA:
					if (an.getCasenumber() < 0)
						throw new RuntimeException("Unresolved Action Case");
//				System.out.println(""+an.getThnum()+", "+mp.getDataLockPointer());
					pw.println("case " + an.getCasenumber() + ": ");
					pw.incrementIndent();
					pw.println("dl[0] = " + ((an.getThnum() - mp.getToplevelThnum()) + mp.getDataLockPointer()) + ";");
					if (an.isBeforeTestNode()) {
						pw.println("return " + an.getStmt() + ";");
						pw.decrementIndent();
					} else {
						pw.println(an.getStmt() + "");
						pw.println("break;");
						pw.decrementIndent();
					}
					numCasesGened++;
					caseGen = true;
					break;
				case GROUPED_JAVA:
					if(an.getCasenumber() < 0)
						throw new RuntimeException("Unresolved Action Case");
					pw.println("case " + an.getCasenumber() + ":");
					pw.incrementIndent();
					pw.println("dl[0] = " + ((an.getThnum() - mp.getToplevelThnum()) + mp.getDataLockPointer()) + ";");
					for(String stmt : an.getStmts()){
						pw.println(stmt);
					}
					pw.println("break;");
					pw.decrementIndent();
					numCasesGened++;
					caseGen = true;
					break;
				case EMIT:
					if(an.hasEmitVal()){
						if(an.getCasenumber() < 0)
							throw new RuntimeException("Unresolved Action Case");
						pw.println("case " + an.getCasenumber()+":");
						pw.incrementIndent();
						pw.println("dl[0] = " + ((an.getThnum() - mp.getToplevelThnum()) + mp.getDataLockPointer())+";");
						pw.println(an.getStmt()+"");
						pw.println("break;");
						pw.decrementIndent();

						numCasesGened++;
						caseGen = true;
					}
					else {
						caseGen = false;
						continue;
					}
					break;
				default:
					caseGen = false;
					continue;
			}
		}

		pw.println("default: throw new RuntimeException(\"Unexpected case number \"+casen);");
		pw.decrementIndent();
		pw.println("}"); // Switch end
		pw.decrementIndent();
		pw.println("}"); // Method end
		pw.println();


		pw.decrementIndent();
		pw.println("}");
		pw.flush();
		pw.close();

	}


	private void printJavaJOPThread(File dir) throws FileNotFoundException{

		IndentPrinter pw = new IndentPrinter(new PrintWriter(new File(dir, "JOPThread.java")));
		pw.println("package "+target+";");
		pw.println("/* TODO: import necessary packages */"); // TODO
		pw.println();
		pw.println("public class JOPThread implements java.lang.Runnable {");
		pw.incrementIndent();
		pw.println("public static int JOP_NUM = "+nodelist.size());
		pw.println();
		pw.println("public void run (){");
		pw.incrementIndent();
		pw.println("int dpcr = 0;");
		pw.println("int cd = 0;");
		pw.println("int casen = 0;");
		pw.println("int result = 0;");
		pw.println("int[] dl = new int[]{0};");
		pw.println("while(true){");
		pw.incrementIndent();

		pw.println();
		pw.println("/* Retrieve cd and case numbers from ReCOP and assign them to 'cd' and 'case', respectively */");
		pw.println();

		// TODO Note getDatacall() is native method from Bjoern's project, need to add import
		pw.println("dpcr = getDatacall(); // Note getDatacall() is native method from Bjoern's project, need to add import");
		pw.println("if ((dpcr >> 31) == 0) continue;");
		pw.println("cd = (dpcr >> 16) & 0xFF; // dpcr(23 downto 16)");
		pw.println("casen = dpcr & 0xFFFF; // dpcr(15 downto 0)");

		pw.println("switch(cd){");
		
		for(int i=0; i<nodelist.size(); i++){
			pw.println("case "+i+":");
			pw.incrementIndent();
			pw.println("result = CD"+i+".MethodCall_0(casen, dl);");
			pw.println("result |= CD\"+i+\".recopId << 28; // Set recop id");
			pw.println("result |= 0x80000000; // Set valid bit");
			pw.println("break;");
			pw.decrementIndent();
		}
		
		pw.println("default: throw new RuntimeException(\"Unrecognized CD number :\"+cd);");
		pw.decrementIndent();
		pw.println("}");
		pw.println();
		pw.println("/* Store result back to ReCOP_Mem[dl] */"); // TODO
		pw.println();
		// Set writeback address
		pw.println("result |= (dl[0] & 0xFFF) << 16;// Set writeback address // TODO Rethink result format"); // TODO Rethink result format
		// TODO Note setDatacallResult(int) is native method from Bjoern's project, need to add import
		// NOTE Results = 1|ReCOP_id(3)|WritebackAddress(12)|Result(16)
		pw.println("setDatacallResult(result);// Note setDatacallResult(int) is native method from Bjoern's project, need to add import");
		pw.decrementIndent();
		pw.println("}");
		pw.decrementIndent();
		pw.println("}");
		pw.decrementIndent();
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
