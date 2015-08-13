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
import org.systemj.config.ClockDomainConfig;
import org.systemj.config.SystemConfig;
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
	private SystemConfig systemConfig;

	public UglyPrinter () {}

	public UglyPrinter(List<BaseGRCNode> nodes, SystemConfig systemConfig) {
		super();
		this.nodelist = nodes;
		this.systemConfig = systemConfig;
	}

	public UglyPrinter(List<BaseGRCNode> nodes, String dir, SystemConfig systemConfig) {
		super();
		this.topdir = dir;
		this.nodelist = nodes;
		this.systemConfig = systemConfig;
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
		public static final String CLASS_INTERFACE_MANAGER = "systemj.common.InterfaceManager";
	
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

			String cdName = doo.getCDName();

			if (systemConfig != null) {
				if (!systemConfig.isLocalClockDomain(cdName)) {
					// This clock domain does not run on this device
					continue;
				}
			}

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
			
			PrintWriter pw = new PrintWriter(new File(dir, target+"_R"+o+".asm"));
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
				pw.println("; Send OSig vals (R0) to JOP");
				pw.println("  DCALLNB R0 #$" + Long.toHexString(0x8000 | cdi) + " ; EOT Datacall ; Format = 1|IO-JOP|CD-ID|OSigs");
				//pw.println("  STR R11 $"+Long.toHexString(mp.getOutputSignalPointer())+"; Reseting to zero");
				pw.println("  LDR R0 $"+Long.toHexString(mp.getInputSignalPointer()) + "; Backup ISig");
				pw.println("  STR R11 $"+Long.toHexString(mp.getInputSignalPointer()) + "; Reset ISig");
				pw.println("  STR R0 $"+Long.toHexString(mp.getPreInputSignalPointer())+"; Updating PreISig");
				pw.println("  LDR R0 $"+Long.toHexString(mp.getOutputSignalPointer()) + "; Backup OSig");
				pw.println("  STR R11 $"+Long.toHexString(mp.getOutputSignalPointer()) + "; Reset OSig");
				pw.println("  STR R0 $"+Long.toHexString(mp.getPreOutputSignalPointer())+"; Updating PreOSig");
				for(long j=0; j<mp.getSizeInternalSignal(); j++){
					pw.println("  LDR R0 $"+Long.toHexString((mp.getInternalSignalPointer()+j)));
					pw.println("  STR R11 $"+Long.toHexString(mp.getInternalSignalPointer()+j));
					pw.println("  STR R0 $"+Long.toHexString((mp.getPreInternalSignalPointer()+j))+"; Updating PreSig");
				}
				for(long j=0; j<mp.getSizeProgramCounter(); j++){
					pw.println("  STR R11 $"+Long.toHexString((mp.getProgramCounterPointer()+j))+"; PC");
				}
				pw.println("; Wait for ISig vals from JOP");
				pw.println("  LDR R0 #HOUSEKEEPING@JOPLOCK ; Save state in housekeeping");
				pw.println("  STR R0 $" + Long.toHexString(mp.getProgramCounterPointer()));
				pw.println("HOUSEKEEPING@JOPLOCK  LDR R0 $"+Long.toHexString(mp.getDataLockPointer()));
				pw.println("  PRESENT R0 AJOIN"+cdi+"; Check for updated ISigs");
				pw.println("  STR R11 $" + Long.toHexString(mp.getProgramCounterPointer()) + " ; Clear housekeeping state");

				pw.println("  STR R0 $"+Long.toHexString(mp.getInputSignalPointer())+"; Updating ISig");
				pw.println("  STR R11 $"+Long.toHexString(mp.getDataLockPointer())+"; Locking this thread");
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
		pw.println("import com.jopdesign.sys.Native;");
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
		pw.println("// TODO Check it is okay to never finish in main"); // TODO Check it is okay to never finish in main
		pw.println("for(int i=0; i < sys.nrCpu-1; i++){");
		pw.incrementIndent();
		pw.println("Runnable r = new JOPThread();");
		pw.println("Startup.setRunnable(r, i);");
		pw.decrementIndent();
		pw.println("}");
		pw.println("sys.signal = 1;");


		pw.println("int dpcr = 0;");
		pw.println("int cd = 0;");
		pw.println("int osigs = 0;");
		pw.println("int isigs = 0;");
		pw.println("int[] dl = new int[]{0};");
		pw.println("int recopId = 0");
		pw.println("int result = 0;");
		pw.println();
		pw.println("while(true){");
		pw.incrementIndent();

		pw.println("/* TODO: Check ER reg from ReCOP and perform corresponding housekeeping operations */"); // TODO
		pw.println("dpcr = getDatacall();");
		pw.println("if ((dpcr >> 31) == 0) continue;");
		pw.println("cd = (dpcr >> 16) & 0xFF; // dpcr(23 downto 16)");
		pw.println("osigs = dpcr & 0xFFFF; // dpcr(15 downto 0)");

		pw.println("switch (cd) {");
		pw.incrementIndent();

		for (int i = 0; i < declolist.size(); i++) {
			String cdName = declolist.get(i).getCDName();

			if (systemConfig != null) {
				if (!systemConfig.isLocalClockDomain(cdName)) {
					// This clock domain does not run on this device
					continue;
				}
			}

			pw.println("case " + i + ":");
			pw.incrementIndent();

			pw.println("isigs = " + cdName + ".housekeeping(osigs, dl);");
			pw.println("recopId = " + cdName + ".recopId;");

			pw.println("break;");
			pw.decrementIndent();
		}

		pw.println("default: throw new RuntimeException(\"Unrecognized CD number :\"+cd);");

		pw.decrementIndent();
		pw.println("}");

		pw.println("result = 0x80000000 | ((recopId & 0x7) << 28) | ((dl[0] & 0xFFF) << 16) | 0x8000 | (isigs & 0x7FFF)");
		pw.println("result = 0x80000000 /*Valid Result Bit*/ " +
				"| ((recopId & 0x7) << 28) /*RecopId*/ " +
				"| ((dl[0] & 0xFFF) << 16) /*WritebackAddress*/ " +
				"| 0x8000 /*Valid Result Bit*/ " +
				"| (isigs & 0x7FFF); /*Input Signals*/");
		pw.println("setDatacallResult(result);");

		pw.decrementIndent();
		pw.println("}");

		pw.decrementIndent();
		pw.println("}");

		pw.println();

		pw.println("public static void init_all() {");
		pw.incrementIndent();

		for (int i = 0; i < declolist.size(); i++) {
			String cdName = declolist.get(i).getCDName();

			if (systemConfig != null) {
				if (!systemConfig.isLocalClockDomain(cdName)) {
					// This clock domain does not run on this device
					continue;
				}
			}

			pw.println(cdName + ".init();");
		}
		
		if (systemConfig == null) {
			pw.println("// ERROR - No System configuration specified");
			pw.println("// Complete init code can not be generated");
		}
		for (int i = 0; i < declolist.size(); i++) {
			if (systemConfig == null) break;

			DeclaredObjects d = declolist.get(i);
			String cdName = d.getCDName();

			if (!systemConfig.isLocalClockDomain(cdName)) {
				// This clock domain does not run on this device
				continue;
			}

			ClockDomainConfig cdConfig = systemConfig.getClockDomain(cdName);

			pw.println("// Init for " + cdName);

			for (Iterator<Channel> it = d.getInputChannelIterator(); it.hasNext();) {
				Channel c = it.next();
				if (!cdConfig.isChannelPartnerLocal(c.name)) continue;
				String channelPartner = cdConfig.channelPartners.get(c.name);
				pw.println("cdName"+"." + c.name + "_in.set_partner(" + channelPartner + "_o);");
			}
			for (Iterator<Channel> it = d.getOutputChannelIterator(); it.hasNext();) {
				Channel c = it.next();
				if (!cdConfig.isChannelPartnerLocal(c.name)) continue;
				String channelPartner = cdConfig.channelPartners.get(c.name);
				pw.println("cdName"+"." + c.name + "_o.set_partner(" + channelPartner + "_in);");
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
		String cdName = d.getCDName();

		if (systemConfig != null) {
			if (!systemConfig.isLocalClockDomain(cdName)) {
				// This clock domain does not run on this device
				return;
			}
		}

		Integer recopId = Helper.pMap.rAlloc != null ? Helper.pMap.rAlloc.get(cdName) : 0;
		if (recopId == null)
			throw new RuntimeException("Could not find CD name: "+cdName);
		IndentPrinter pw = new IndentPrinter(new PrintWriter(new File(dir, cdName+".java")));

		pw.println("package "+target+";\n");
		pw.println("public class "+cdName+"{");

		pw.incrementIndent();

		pw.println("public static final String CDName = \"" + cdName + "\";");
		pw.println("public static final int recopId = " + recopId + ";");
		pw.println("private static java.util.Vector currentSignals;");
		pw.println("private static " + Java.CLASS_INTERFACE_MANAGER + " im = null; // TODO Configure InterfaceManager"); // TODO configure InterfaceManager
		{
			Iterator<Signal> iter = d.getInputSignalIterator();
			while(iter.hasNext()){
				Signal s = iter.next();
				pw.println("public static "+Java.CLASS_SIGNAL+" "+s.name+"; // isig");
			}
		}
		{
			Iterator<Signal> iter = d.getOutputSignalIterator();
			while(iter.hasNext()){
				Signal s = iter.next();
				pw.println("public static "+Java.CLASS_SIGNAL+" "+s.name+"; // osig");
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

		pw.println("public static int housekeeping(int osigs, int[] dl) {");
		pw.incrementIndent();

		pw.println("// Set output signal statuses");
		Iterator<Signal> osigIt = d.getOutputSignalIterator();
		if (!osigIt.hasNext()) pw.println("// Note: No output signals for " + cdName);
		for (int i = 0; osigIt.hasNext(); i++) {
			Signal s = osigIt.next();
			pw.println("if ((osigs & " + (1 << i) + ") > 0) " + s.name + ".setPresent(); else " + s.name + ".setClear();");
		}

		pw.println();

		// START - Set preVal for internal valued signals which have been emitted this tick
		pw.println("// Set preval for valued signals which have been emitted this tick");
		pw.println("for (int i = 0; i < currentSignals.size(); i++) {");
		pw.incrementIndent();

		pw.println(Java.CLASS_SIGNAL + "sig = (" + Java.CLASS_SIGNAL + ") currentSignals.elementAt(i);");
		pw.println("sig.getStatus() ? sig.setprepresent() : sig.setpreclear()");
		pw.println("sig.setpreval(sig.getValue()");
		pw.println("sig.sethook()");

		pw.decrementIndent();
		pw.println("}");
		pw.println("currentSignals.removeAllElements()");
		pw.println();
		// START - GetHook for all input signals
		pw.println("// Update signals");
		for (Iterator<Signal> it = d.getInputSignalIterator(); it.hasNext();) {
			Signal s = it.next();
			pw.println(s.name + ".gethook()");
		}
		pw.println();
		// START - Update channels
		pw.println("// Update Channels");
		for (Iterator<Channel> it = d.getInputChannelIterator(); it.hasNext();) {
			Channel c = it.next();
			pw.println(c.name + ".update_r_s();");
		}
		for (Iterator<Channel> it = d.getOutputChannelIterator(); it.hasNext();) {
			Channel c = it.next();
			pw.println(c.name + ".update_w_r();");
		}
		for (Iterator<Channel> it = d.getInputChannelIterator(); it.hasNext();) {
			Channel c = it.next();
			pw.println(c.name + ".gethook();");
			pw.println(c.name + ".sethook();");
		}
		for (Iterator<Channel> it = d.getOutputChannelIterator(); it.hasNext();) {
			Channel c = it.next();
			pw.println(c.name + ".gethook();");
			pw.println(c.name + ".sethook();");
		}
		pw.println();
		pw.println("// Run interface manager");
		pw.println("if (im != null) im.run();");

		pw.println();

		pw.println("// Get input signal statues");
		pw.println("int isigs = 0;");
		Iterator<Signal> isigIt = d.getInputSignalIterator();
		if (!isigIt.hasNext()) pw.println("// Note: No input signals for " + cdName);
		for (int i = 0; isigIt.hasNext(); i++) {
			Signal s = isigIt.next();
			pw.println("if (" + s.name + ".getStatus()) isigs |= " + (1 << i));
		}
		pw.println();
		pw.println("// Write to isigs to preIsigs");
		pw.println("dl[0] = " + mp.getPreInputSignalPointer() + ";");

		pw.println("return isigs;");

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
						pw.println("currentSignals.addElement(" + an.getSigName() + ");");
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
		pw.println();
		pw.println("import com.jopdesign.sys.Native;");
		pw.println("/* TODO: import necessary packages */"); // TODO
		pw.println();
		pw.println("public class JOPThread implements java.lang.Runnable {");
		pw.incrementIndent();
		//pw.println("// public static int JOP_NUM = "+nodelist.size()); // TODO What is this for?
		pw.println();
		pw.println("public void run (){");
		pw.incrementIndent();
		pw.println("int dpcr = 0;");
		pw.println("int cd = 0;");
		pw.println("int casen = 0;");
		pw.println("int result = 0;");
		// Response variables
		pw.println("int status = 0;");
		pw.println("int recopId = 0;");
		pw.println("int[] dl = new int[]{0};");
		pw.println("while(true){");
		pw.incrementIndent();

		pw.println();
		pw.println("/* Retrieve cd and case numbers from ReCOP and assign them to 'cd' and 'case', respectively */");
		pw.println();

		pw.println("dpcr = getDatacall(); // Note getDatacall() is native method from Bjoern's project, need to add import");
		pw.println("if ((dpcr >> 31) == 0) continue;");
		pw.println("cd = (dpcr >> 16) & 0xFF; // dpcr(23 downto 16)");
		pw.println("casen = dpcr & 0xFFFF; // dpcr(15 downto 0)");

		pw.println("switch(cd){");
		pw.incrementIndent();

		for(int i=0; i<declolist.size(); i++){
			String cdName = declolist.get(i).getCDName();

			if (systemConfig != null) {
				if (!systemConfig.isLocalClockDomain(cdName)) {
					// This clock domain does not run on this device
					continue;
				}
			}

			pw.println("case "+i+":");
			pw.incrementIndent();
			pw.println("status = "+cdName+".MethodCall_0(casen, dl);");
			pw.println("recopId = "+cdName+".recopId; // Set recop id");
			pw.println("break;");
			pw.decrementIndent();
		}
		
		pw.println("default: throw new RuntimeException(\"Unrecognized CD number :\"+cd);");
		pw.decrementIndent();
		pw.println("}");
		pw.println();
		pw.println("result = 0x80000000 /*Valid Result Bit*/ " +
				"| ((recopId & 0x7) << 28) /*RecopId*/ " +
				"| ((dl[0] & 0xFFF) << 16) /*WritebackAddress*/ " +
				"| 0x8000 /*Valid Result Bit*/ " +
				"| (status & 0x7FFF); /*Status*/");
		// NOTE Results = 1|ReCOP_id(3)|WritebackAddress(12)|Result(16)
		pw.println("setDatacallResult(result);");
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
