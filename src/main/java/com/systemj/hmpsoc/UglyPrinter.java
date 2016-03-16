package com.systemj.hmpsoc;


import static com.systemj.hmpsoc.util.Helper.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.systemj.hmpsoc.DeclaredObjects.Channel;
import com.systemj.hmpsoc.DeclaredObjects.Signal;
import com.systemj.hmpsoc.DeclaredObjects.Var;
import com.systemj.hmpsoc.SharedMemory.MemorySlot;
import com.systemj.hmpsoc.config.ClockDomainConfig;
import com.systemj.hmpsoc.config.InterfaceConfig;
import com.systemj.hmpsoc.config.SignalConfig;
import com.systemj.hmpsoc.config.SystemConfig;
import com.systemj.hmpsoc.nodes.ActionNode;
import com.systemj.hmpsoc.nodes.ActionNode.TYPE;
import com.systemj.hmpsoc.nodes.BaseGRCNode;
import com.systemj.hmpsoc.nodes.ForkNode;
import com.systemj.hmpsoc.nodes.JoinNode;
import com.systemj.hmpsoc.nodes.SwitchNode;
import com.systemj.hmpsoc.util.IndentPrinter;

import args.Helper;

public class UglyPrinter {

	private String target;
	private List<BaseGRCNode> nodelist;
	private List<DeclaredObjects> declolist;
	private List<List<ActionNode>> acts;
	private List<List<List<ActionNode>>> actsDist;
	private String topdir;
	private SystemConfig systemConfig;
	private SharedMemory sm = new SharedMemory();
	private List<String> imports;

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
	
	public void setImports(List<String> l) {
		imports = l;
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
		public static final String CLASS_SIGNAL = "com.systemj.Signal";//"systemj.lib.emb.Signal";
		public static final String CLASS_I_CHANNEL = "com.systemj.input_Channel";//"systemj.lib.emb.input_Channel";
		public static final String CLASS_O_CHANNEL = "com.systemj.output_Channel";//"systemj.lib.emb.output_Channel";
		public static final String CLASS_INTERFACE_MANAGER = "com.systemj.InterfaceManager";
		public static final String CLASS_INTERCONNECTION = "com.systemj.Interconnection";
		public static final String CLASS_INTERCONNECTION_LINK = "com.systemj.Interconnection.Link";
		public static final String CLASS_GENERIC_INTERFACE = "com.systemj.ipc.GenericInterface";
		public static final String CLASS_GENERIC_SIGNAL_RECIEVER = "com.systemj.ipc.GenericSignalReceiver";
		public static final String CLASS_GENERIC_SIGNAL_SENDER = "com.systemj.ipc.GenericSignalSender";
		public static final String CLASS_GENERIC_CHANNEL = "com.systemj.GenericChannel";
		public static final String CLASS_SERIALIZABLE = "com.systemjx.jop.ipc.Serializable";

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
	}

	private void printFiles(File dir) throws FileNotFoundException {
		if(acts.size() != declolist.size())
			throw new RuntimeException("Error !");
		
		distributeActions();
		
		printASM(dir);
		
		if(Helper.getSingleArgInstance().hasOption(Helper.DIST_MEM_OPTION)){
			IntStream.range(0, Helper.pMap.nJOP).forEachOrdered(i -> {
				File ff = new File(dir, "JOP"+i);
				List<List<ActionNode>> l = actsDist.get(i);
				try {
					printJavaMainDistributed(ff, l, i);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			});
		} else {
			printJavaJOPThread(dir);
			printJavaMain(dir);
		}
	}

	private void printJavaMainDistributed(File dir, List<List<ActionNode>> actList, int jopID) throws FileNotFoundException {
		IndentPrinter pw = new IndentPrinter(new PrintWriter(new File(dir, "RTSMain.java")));
		pw.println("package "+dir.getPath().replace("\\", ".").replace("/", ".")+";");
		pw.println();
		pw.println("import com.jopdesign.io.IOFactory;");
		pw.println("import com.jopdesign.io.SysDevice;");
		pw.println("import com.jopdesign.sys.Startup;");
		pw.println("import com.jopdesign.sys.Native;");
		pw.println();
		pw.println("import java.util.Hashtable;");
		pw.println();
		
		pw.println("public class RTSMain {");
		pw.incrementIndent();
		
		if(jopID == 0) {
			pw.println("public static void printStdOut(){");
			pw.println("// TODO: complete this");
			pw.println("}");
			pw.println();
		} else {
			pw.println("public static final java.io.PrintStream out = new java.io.PrintStream(new java.io.OutputStream() {");
			pw.incrementIndent();
			pw.println("public void write(int b) throws java.io.IOException {");
			pw.incrementIndent();
			pw.println("synchronized(RTSMain.class){");
			pw.incrementIndent();
			pw.println("// TODO: complete this");
			pw.decrementIndent();
			pw.println("}");
			pw.decrementIndent();
			pw.println("}");
			pw.decrementIndent();
			pw.println("});");
		}

		
		pw.println("public static void main(String[] arg){");
		pw.incrementIndent();
		pw.println("init_all();");

		pw.println("int dpcr = 0;");
		pw.println("int cd = 0;");
		pw.println("int data = 0;");
		pw.println("int rval = 0;");
		pw.println("int[] dl = new int[]{0};");
		pw.println("int recopId = 0;");
		pw.println("int result = 0;");
		pw.println();
		pw.println("try{");
		pw.incrementIndent();
		pw.println("while(true){");
		pw.incrementIndent();

		if(jopID == 0)
			pw.println("printStdOut();");
		
		pw.println("dpcr = Native.getDatacall();");
		pw.println("if ((dpcr >> 31) == 0) continue;");
		pw.println("cd = (dpcr >> 16) & 0xFF; // dpcr(23 downto 16)");
		pw.println("data = dpcr & 0xFFFF; // dpcr(15 downto 0)");

		pw.println("switch (cd) {");

		for (int i = 0; i < declolist.size(); i++) {
			String cdName = declolist.get(i).getCDName();

			if (systemConfig != null) {
				if (!systemConfig.isLocalClockDomain(cdName)) {
					// This clock domain does not run on this device
					continue;
				}
			}

			if(jopID == 0){
				pw.println("case " + i + ":");
				pw.incrementIndent();
				pw.println("recopId = " + cdName + ".recopId;");
				pw.println("rval = " + cdName + ".housekeeping(data, dl);");
				pw.println("break;");
				pw.decrementIndent();
			} else if (!actList.get(i).isEmpty()) {
				pw.println("case " + i + ":");
				pw.incrementIndent();
				pw.println("rval = "+cdName+".MethodCall_0(data, dl) ? 3 : 2;");
				pw.println("break;");
				pw.decrementIndent();
			}
		}

		pw.println("default: throw new RuntimeException(\"Unrecognized CD number :\"+cd);");

		pw.println("}");

		pw.println("result = 0x80000000 /*Valid Result Bit*/ " +
				"| ((recopId & 0x7F) << 24) /*RecopId*/ " +
				"| ((dl[0] & 0xFFF) << 12) /*WritebackAddress*/ " +
				"| (rval & 0xFFF); /* return val */");
		pw.println("Native.setDatacallResult(result);");

		pw.decrementIndent();
		pw.println("}");
		pw.decrementIndent();
		
		pw.println("} catch (Exception e){");
		
		pw.incrementIndent();
		pw.println("System.out.println(\"ERROR while executing housekeeping \"+recopId+\" \"+e.getMessage());");
		pw.println("System.exit(1);");
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
			
			if(jopID == 0 || !actList.get(i).isEmpty())
				pw.println(cdName + ".init();");
		}

		if (systemConfig == null) {
			pw.println("// ERROR - No System configuration specified");
			pw.println("// Complete init code can not be generated");
		}

		pw.println();
		pw.println("// Interface init");
		pw.println(Java.CLASS_GENERIC_INTERFACE + " gif = null;");
		pw.println("Hashtable ht = null;");
		pw.println();

		for (int i = 0; i < declolist.size(); i++) {
			if (systemConfig == null) break;

			DeclaredObjects d = declolist.get(i);
			String cdName = d.getCDName();

			if (!systemConfig.isLocalClockDomain(cdName)) {
				// This clock domain does not run on this device
				continue;
			}
			
			if(!actList.get(i).isEmpty()){
				ClockDomainConfig cdConfig = systemConfig.getClockDomain(cdName);

				pw.println("// Init for " + cdName);
				for (Iterator<Channel> it = d.getInputChannelIterator(); it.hasNext();) {
					Channel c = it.next();
					String channel = cdName + "." + c.name + "_in";
					String channelPartner = cdConfig.channelPartners.get(c.name) + "_o";
					pw.println(channel + ".Name = \"" + channel + "\";");
					pw.println(channel + ".PartnerName = \"" + channelPartner + "\";");

					if (cdConfig.isChannelPartnerLocal(c.name)) {
						if (Helper.getSingleArgInstance().hasOption(Helper.DIST_MEM_OPTION)) {
							if (sm.hasChan(channelPartner)) {
								MemorySlot ms = sm.getChanMem(channelPartner);
								pw.println("gif = new com.systemjx.jop.ipc.ChannelMemory(" + ms.start + "L," + ms.depth + "L);");
								if(!sm.hasChan(channel))
									sm.linkChannel(channel);
							} else{
								pw.println("gif = new com.systemjx.jop.ipc.ChannelMemory(" + sm.getPointer() + "L," + SharedMemory.DEPTH_CHAN + "L);");
								if(!sm.hasChan(channel))
									sm.addChannel(channel);
							}
							pw.println("gif.configure(null);");
							pw.println(channel + ".setLink(gif);");
						} else {
							pw.println(Java.CLASS_GENERIC_CHANNEL + ".setPartner(" + channel + ", " + channelPartner + ");");
						}
					} else {
						systemConfig.subSystems.stream().forEach(SSC -> {
							if (!SSC.local) {
								SSC.clockDomains.forEach((K, V) -> {
									if (V.isChannelPartnerLocal(c.name)) {
										Optional<InterfaceConfig> o = systemConfig.links.stream().flatMap(L -> L.interfaces.stream().filter(I -> I.subSystem.equals(SSC.name))).findAny();
										if (o.isPresent()) {
											InterfaceConfig ic = o.get();
											pw.println("gif = new " + ic.interfaceClass + "();");
											pw.println("ht = new Hashtable();");
											ic.cfg.forEach((KK, VV) -> pw.println("ht.put(\"" + KK + "\", \"" + VV + "\");"));
											pw.println("gif.configure(ht);");
											pw.println(channel + ".setLink(gif);");
										}
									}
								});
							}
						});
					}
					pw.println(channel + ".setInit();");
					pw.println();
				}
				for (Iterator<Channel> it = d.getOutputChannelIterator(); it.hasNext();) {
					Channel c = it.next();

					String channel = cdName + "." + c.name + "_o";
					String channelPartner = cdConfig.channelPartners.get(c.name) + "_in";
					pw.println(channel + ".Name = \"" + channel + "\";");
					pw.println(channel + ".PartnerName = \"" + channelPartner + "\";");

					if (cdConfig.isChannelPartnerLocal(c.name)) {
						if (Helper.getSingleArgInstance().hasOption(Helper.DIST_MEM_OPTION)) {
							if (sm.hasChan(channelPartner)) {
								MemorySlot ms = sm.getChanMem(channelPartner);
								pw.println("gif = new com.systemjx.jop.ipc.ChannelMemory(" + ms.start + "L," + ms.depth + "L);");
								if(!sm.hasChan(channel))
									sm.linkChannel(channel);
							} else {
								pw.println("gif = new com.systemjx.jop.ipc.ChannelMemory(" + sm.getPointer() + "L," + SharedMemory.DEPTH_CHAN + "L);");
								if(!sm.hasChan(channel))
									sm.addChannel(channel);
							}
							pw.println("gif.configure(null);");
							pw.println(channel + ".setLink(gif);");
						} else {
							pw.println(Java.CLASS_GENERIC_CHANNEL + ".setPartner(" + channelPartner + ", " + channel + ");");
						}
					} else {
						systemConfig.subSystems.stream().forEach(SSC -> {
							if (!SSC.local) {
								SSC.clockDomains.forEach((K, V) -> {
									if (V.isChannelPartnerLocal(c.name)) {
										Optional<InterfaceConfig> o = systemConfig.links.stream().flatMap(L -> L.interfaces.stream().filter(I -> I.subSystem.equals(SSC.name))).findAny();
										if (o.isPresent()) {
											InterfaceConfig ic = o.get();
											pw.println("gif = new " + ic.interfaceClass + "();");
											pw.println("ht = new Hashtable();");
											ic.cfg.forEach((KK, VV) -> pw.println("ht.put(\"" + KK + "\", \"" + VV + "\");"));
											pw.println("gif.configure(ht);");
											pw.println(channel + ".setLink(gif);");
										}
									}
								});
							}
						});
					}
					pw.println(channel + ".setInit();");
					pw.println();
				}
			}
		}

		pw.decrementIndent();
		pw.println("}");
		pw.decrementIndent();
		pw.println("}");
		pw.flush();
		pw.close();		
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
				
				log.info("====== "+cdname+" constructed memory map =====\n"
				+ "iSignal    :"+mp.getInputSignalPointer()		   + "\n"
				+ "oSignal    :"+mp.getOutputSignalPointer()	   + "\n"
				+ "DataLock   :"+mp.getDataLockPointer()		   + "\n"
				+ "Signal     :"+mp.getInternalSignalPointer()	   + "\n"
				+ mp.signalMap									   + "\n"
				+ "PreSig     :"+mp.getPreInternalSignalPointer()  + "\n"
				+ "PreISig    :"+mp.getPreInputSignalPointer()	   + "\n"
				+ "PreOSig    :"+mp.getPreOutputSignalPointer()	   + "\n"
				+ "PC         :"+mp.getProgramCounterPointer()	   + "\n"
				+ "Term       :"+mp.getTerminateCodePointer()	   + "\n"
				+ "Switch     :"+mp.getSwitchNodePointer()		   + "\n"
				+ "LastAddr+1 :"+mp.getLastAddr()				         );

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
					String label = swname.toLowerCase()+"_1";
					pw.println("  LDR R10 #"+label);
					pw.println("  STR R10 $"+Long.toHexString(mp.getSwitchNodePointer()+mp.switchMap.get(swname)));
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
				pw.println("  SUBV R10 R1 #$"+Long.toHexString((mp.getSizeProgramCounter()+mp.getProgramCounterPointer()))+"; Next DS loc");
				pw.println("  PRESENT R10 HOUSEKEEPING"+i);
				pw.println("DCHECK"+i+" LDR R10 R1; Loading the PC");
				pw.println("  PRESENT R10 DCHECKCONT"+i);
				pw.println("  JMP R10");


				pw.println("HOUSEKEEPING"+i+" CLFZ");
				pw.println("  LER R10; Checking whether reactive-interface-JOP is ready");
				pw.println("  PRESENT R10 HOUSEKEEPING"+i);
				pw.println("  SEOT; JOP is ready!");
				pw.println("  CER");
				pw.println("  STR R11 $2; locking this thread");
				pw.println("  LDR R10 $"+Long.toHexString(mp.getOutputSignalPointer())+"; Loading OSigs");
				pw.println("; Send OSig vals (R10) to JOP");
				pw.println("  DCALLNB R10 #$" + Long.toHexString(0x8000 | cdi) + " ; EOT Datacall ; Format = 1|IO-JOP|CD-ID|OSigs");
				//pw.println("  STR R11 $"+Long.toHexString(mp.getOutputSignalPointer())+"; Reseting to zero");
				pw.println("  LDR R10 $"+Long.toHexString(mp.getInputSignalPointer()) + "; Backup ISig");
				pw.println("  STR R11 $"+Long.toHexString(mp.getInputSignalPointer()) + "; Reset ISig");
				pw.println("  STR R10 $"+Long.toHexString(mp.getPreInputSignalPointer())+"; Updating PreISig");
				pw.println("  LDR R10 $"+Long.toHexString(mp.getOutputSignalPointer()) + "; Backup OSig");
				pw.println("  STR R11 $"+Long.toHexString(mp.getOutputSignalPointer()) + "; Reset OSig");
				pw.println("  STR R10 $"+Long.toHexString(mp.getPreOutputSignalPointer())+"; Updating PreOSig");
				for(long j=0; j<mp.getSizeInternalSignal(); j++){
					pw.println("  LDR R10 $"+Long.toHexString((mp.getInternalSignalPointer()+j)));
					pw.println("  STR R11 $"+Long.toHexString(mp.getInternalSignalPointer()+j));
					pw.println("  STR R10 $"+Long.toHexString((mp.getPreInternalSignalPointer()+j))+"; Updating PreSig");
				}
				for(long j=0; j<mp.getSizeProgramCounter(); j++){
					pw.println("  STR R11 $"+Long.toHexString((mp.getProgramCounterPointer()+j))+"; PC");
				}
				pw.println("; Wait for ISig vals from JOP");
				pw.println("  LDR R10 #HOUSEKEEPING_JOP"+i+" ; Save state in housekeeping");
				pw.println("  STR R10 $" + Long.toHexString(mp.getProgramCounterPointer()));
				pw.println("HOUSEKEEPING_JOP"+i+"  LDR R10 $"+Long.toHexString(mp.getDataLockPointer()));
				pw.println("  PRESENT R10 AJOIN"+cdi+"; Check for updated ISigs");
				pw.println("  STR R11 $" + Long.toHexString(mp.getProgramCounterPointer()) + " ; Clear housekeeping state");
				pw.println("  AND R10 R10 #$0FFF ; Keep only ISigs");

				pw.println("  STR R10 $"+Long.toHexString(mp.getInputSignalPointer())+"; Updating ISig");
				pw.println("  STR R11 $"+Long.toHexString(mp.getDataLockPointer())+"; Locking this thread");
				pw.println("  CEOT; Clearing EOT register");


				topnode.weirdPrint(pw, mp, 0, cdi);

				if(i == nodes.size()-1)
					pw.println("AJOIN"+cdi+" JMP RUN0");
				else
					pw.println("AJOIN"+cdi+" JMP RUN"+(i+1));
				
				if (Helper.getSingleArgInstance().hasOption(Helper.DIST_MEM_OPTION)) {
					IntStream ist = IntStream.range(0, Helper.pMap.nJOP);
					ist.forEachOrdered(ii -> {
						List<List<ActionNode>> actlists = actsDist.get(ii);
						File ff = new File(dir, "JOP"+ii);
						ff.mkdirs();
						try {
							printJavaClockDomainDistributed(ff, mp, actlists.get(cdi), declolist.get(cdi), cdi, ii);
						} catch (Exception e) {
							e.printStackTrace();
							System.exit(1);
						}
					});
				} else
					printJavaClockDomain(dir, mp, this.acts.get(cdi), declolist.get(cdi), cdi);
			}

			pw.println("ENDPROG");
			pw.flush();
			pw.close();
		}
		
	}
	
	private void distributeActions() {
		IntStream ist = IntStream.range(0, Helper.pMap.nJOP);
		List<List<List<ActionNode>>> sorted = ist.mapToObj(ii -> {
			Stream<List<ActionNode>> nds = acts.stream().flatMap(l -> {
				return Stream.of(l.stream().filter(a -> a.getJOPIDDist() == ii).collect(Collectors.toList()));
			});
			return nds.collect(Collectors.toList());
		}).collect(Collectors.toList());

		this.actsDist = sorted;
		
		// Adding HK
		this.actsDist = IntStream.range(0, actsDist.size()).mapToObj(jopi -> {
			return IntStream.range(0, actsDist.get(jopi).size()).mapToObj(cdi -> {
				List<ActionNode> l = actsDist.get(jopi).get(cdi);
				if (l.size() > 0) {
					ActionNode an = new ActionNode();
					an.setThnum(l.get(0).getThnum());
					an.setActionType(TYPE.GROUPED_JAVA);
					StringBuilder sb = new StringBuilder();
					ArrayList<String> ll = new ArrayList<>();
					String ln = "\n";
					sb.append("for (int i = 0; i < currentSignals.size(); i++) {").append(ln);
					sb.append("com.systemj.Signal sig = (com.systemj.Signal) currentSignals.elementAt(i);").append(ln);
					sb.append("if (sig.getStatus()) sig.setprepresent(); else sig.setpreclear();").append(ln);
					sb.append("sig.setpreval(sig.getValue());").append(ln);
					sb.append("byte[] b = (("+Java.CLASS_SERIALIZABLE+")sig.getpreval()).serialize();").append(ln);
					sb.append("com.jopdesign.sys.Native.wr(b[3] << 24 | b[2] << 16 | b[1] << 8 | b[0], (int)sig.getMemLoc());").append(ln);
					sb.append("}").append(ln);
					sb.append("currentSignals.removeAllElements();").append(ln);
					sb.append("// TODO: Read from channels").append(ln);
					
					
					ll.add(sb.toString());
					an.setStmts(ll);
					l.add(0, an);
					
					an = new ActionNode();
					an.setThnum(l.get(0).getThnum());
					an.setActionType(TYPE.GROUPED_JAVA);
					sb.delete(0, sb.length());
					ll = new ArrayList<>();
					
					DeclaredObjects d = declolist.get(cdi);
					Stream<Signal> sst = Stream.<List<Signal>>builder().add(d.getIsignals()).add(d.getOsignals()).add(d.getSignals()).build().flatMap( sl -> sl.stream());
					sst.forEachOrdered(s -> {
						if(s.type != null){
							sb.append("{").append(ln);
							sb.append("int val = com.jopdesign.sys.Native.rd((int)"+s.name+".getMemLoc());").append(ln);
							sb.append("byte[] b = new byte[4];").append(ln);
							sb.append("for (int i=0 ; i<b.length; i++)").append(ln);
							sb.append("b[i] = (byte)((val >> i*8) & 0xF);").append(ln);
							sb.append(s.name+".setpreval("+s.name+".getType().deserialize(b));").append(ln);
							sb.append("}").append(ln);
							sb.append("// TODO: Write from channels").append(ln);
						}
					});
					ll.add(sb.toString());
					an.setStmts(ll);
					l.add(0, an);
				}
				return l;
			}).collect(Collectors.toList());
		}).collect(Collectors.toList());
		
		// Setting caes number here
		actsDist.stream().map(ll -> 
			ll.stream().map(l -> 
				l.stream().filter(n -> 
					(n.getActionType() == ActionNode.TYPE.JAVA || n.getActionType() == ActionNode.TYPE.GROUPED_JAVA ||
							(n.getActionType() == ActionNode.TYPE.EMIT && n.hasEmitVal()) && n.getCasenumber() < 0)).collect(Collectors.toList())).collect(Collectors.toList()))
		.forEachOrdered( ll -> 
			ll.forEach( l -> 
				IntStream.range(0, l.size()).forEachOrdered(i -> 
					l.get(i).setCasenumber(i))));
		
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
		pw.println();
		pw.println("import com.jopdesign.io.IOFactory;");
		pw.println("import com.jopdesign.io.SysDevice;");
		pw.println("import com.jopdesign.sys.Startup;");
		pw.println("import com.jopdesign.sys.Native;");
		pw.println();
		pw.println("import java.util.Hashtable;");
		pw.println();
		pw.println("public class RTSMain {");
		pw.incrementIndent();
		
		pw.println("private static final StringBuffer sb = new StringBuffer();");
		pw.println("public static final java.io.PrintStream out = new java.io.PrintStream(new java.io.OutputStream() {");
		pw.incrementIndent();
		pw.println("public void write(int b) throws java.io.IOException {");
		pw.incrementIndent();
		pw.println("synchronized(RTSMain.class){");
		pw.incrementIndent();
		pw.println("sb.append((char)b);");
		pw.decrementIndent();
		pw.println("}");
		pw.decrementIndent();
		pw.println("}");
		pw.decrementIndent();
		pw.println("});");
		
		pw.println("private static final void printStdOut() {");
		pw.incrementIndent();
		pw.println("if(sb.length() > 0) {");
		pw.incrementIndent();
		pw.println("synchronized(RTSMain.class) {");
		pw.incrementIndent();
		pw.println("System.out.print(sb.toString());");
		pw.println("sb.delete(0, sb.length());");
		pw.decrementIndent();
		pw.println("}");
		pw.decrementIndent();
		pw.println("}");
		pw.decrementIndent();
		pw.println("}");
		
		pw.println("public static void main(String[] arg){");
		pw.incrementIndent();
		

		pw.println();
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


		pw.println("int dpcr = 0;");
		pw.println("int cd = 0;");
		pw.println("int osigs = 0;");
		pw.println("int isigs = 0;");
		pw.println("int[] dl = new int[]{0};");
		pw.println("int recopId = 0;");
		pw.println("int result = 0;");
		pw.println();
		pw.println("try{");
		pw.incrementIndent();
		pw.println("while(true){");
		pw.incrementIndent();

		pw.println("printStdOut();");
		pw.println("dpcr = Native.getDatacall();");
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
			
			pw.println("recopId = " + cdName + ".recopId;");
			pw.println("isigs = " + cdName + ".housekeeping(osigs, dl);");
			

			pw.println("break;");
			pw.decrementIndent();
		}

		pw.println("default: throw new RuntimeException(\"Unrecognized CD number :\"+cd);");

		pw.decrementIndent();
		pw.println("}");

		pw.println("result = 0x80000000 /*Valid Result Bit*/ " +
				"| ((recopId & 0x7F) << 24) /*RecopId*/ " +
				"| ((dl[0] & 0xFFF) << 12) /*WritebackAddress*/ " +
				"| (isigs & 0xFFF); /*Input Signals*/");
		pw.println("Native.setDatacallResult(result);");

		pw.decrementIndent();
		pw.println("}");
		pw.decrementIndent();
		
		pw.println("} catch (Exception e){");
		
		pw.incrementIndent();
		pw.println("System.out.println(\"ERROR while executing housekeeping \"+recopId+\" \"+e.getMessage());");
		pw.println("System.exit(1);");
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

		pw.println();
		pw.println("// Interface init");
		pw.println(Java.CLASS_GENERIC_INTERFACE + " gif = null;");
		pw.println("Hashtable ht = null;");
		pw.println();

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
				String channel = cdName + "." + c.name + "_in";
				String channelPartner = cdConfig.channelPartners.get(c.name) + "_o";
				pw.println(channel + ".Name = \"" + channel + "\";");
				pw.println(channel + ".PartnerName = \"" + channelPartner + "\";");

				if (cdConfig.isChannelPartnerLocal(c.name)) {
					pw.println(Java.CLASS_GENERIC_CHANNEL + ".setPartner(" + channel + ", " + channelPartner + ");");
				} else {
					systemConfig.subSystems.stream().forEach(SSC -> {
						if (!SSC.local) {
							SSC.clockDomains.forEach((K, V) -> {
								if (V.isChannelPartnerLocal(c.name)) {
									Optional<InterfaceConfig> o = systemConfig.links.stream().flatMap(L -> L.interfaces.stream().filter(I -> I.subSystem.equals(SSC.name))).findAny();
									if (o.isPresent()) {
										InterfaceConfig ic = o.get();
										pw.println("gif = new " + ic.interfaceClass + "();");
										pw.println("ht = new Hashtable();");
										ic.cfg.forEach((KK, VV) -> pw.println("ht.put(\"" + KK + "\", \"" + VV + "\");"));
										pw.println("gif.configure(ht);");
										pw.println(channel + ".setLink(gif);");
									}
								}
							});
						}
					});
				}
				pw.println(channel + ".setInit();");
				pw.println();
			}
			for (Iterator<Channel> it = d.getOutputChannelIterator(); it.hasNext();) {
				Channel c = it.next();

				String channel = cdName + "." + c.name + "_o";
				String channelPartner = cdConfig.channelPartners.get(c.name) + "_in";
				pw.println(channel + ".Name = \"" + channel + "\";");
				pw.println(channel + ".PartnerName = \"" + channelPartner + "\";");

				if (cdConfig.isChannelPartnerLocal(c.name)) {
					pw.println(Java.CLASS_GENERIC_CHANNEL + ".setPartner(" + channelPartner + ", " + channel + ");");
				} else {
					systemConfig.subSystems.stream().forEach(SSC -> {
						if (!SSC.local) {
							SSC.clockDomains.forEach((K, V) -> {
								if (V.isChannelPartnerLocal(c.name)) {
									Optional<InterfaceConfig> o = systemConfig.links.stream().flatMap(L -> L.interfaces.stream().filter(I -> I.subSystem.equals(SSC.name))).findAny();
									if (o.isPresent()) {
										InterfaceConfig ic = o.get();
										pw.println("gif = new " + ic.interfaceClass + "();");
										pw.println("ht = new Hashtable();");
										ic.cfg.forEach((KK, VV) -> pw.println("ht.put(\"" + KK + "\", \"" + VV + "\");"));
										pw.println("gif.configure(ht);");
										pw.println(channel + ".setLink(gif);");
									}
								}
							});
						}
					});
				}
				pw.println(channel + ".setInit();");
				pw.println();
			}
		}

		pw.decrementIndent();
		pw.println("}");
		pw.decrementIndent();
		pw.println("}");
		pw.flush();
		pw.close();
	}
	
	private void printJavaCDInit(IndentPrinter pw, DeclaredObjects d) {
		String cdName = d.getCDName();
		pw.println("public static void init() {");
		pw.incrementIndent();

		pw.println("Hashtable ht = null;");
		pw.println(Java.CLASS_GENERIC_SIGNAL_RECIEVER + " sigReceiver = null;");
		pw.println(Java.CLASS_GENERIC_SIGNAL_SENDER + " sigSender = null;");
		pw.println("currentSignals = new java.util.Vector();");
		pw.println();
		
		Consumer<Signal> mL = (ssig -> {
			if(Helper.getSingleArgInstance().hasOption(Helper.DIST_MEM_OPTION)) {
				String cdSigName = cdName+"."+ssig.name;
				if(!sm.hasSig(cdSigName))
					sm.addSignal(cdSigName);
				MemorySlot ms = sm.getSigMem(cdSigName);
				pw.println(ssig.name + ".setMemoryLoc(" + ms.start + "L, " + ms.depth + "L);");
			}
		});

		for (Iterator<Signal> it = d.getInputSignalIterator(); it.hasNext();) {
			Signal s = it.next();
			pw.println(s.name + " = new " + Java.CLASS_SIGNAL + "();");

			if (systemConfig != null) {
				ClockDomainConfig cdCfg = systemConfig.getClockDomain(cdName);
				SignalConfig sigCfg = cdCfg.isignals.get(s.name);

				if (sigCfg == null)
					throw new RuntimeException("Unconfigured input signal " + cdName + "." + s.name);
				pw.println("sigReceiver = new " + sigCfg.clazz + "();");
				pw.println("ht = new Hashtable();");
				for (Map.Entry<String, String> entry : sigCfg.cfg.entrySet())
					pw.println("ht.put(\"" + entry.getKey() + "\", \"" + entry.getValue() + "\");");
				pw.println("sigReceiver.configure(ht);");
				pw.println(s.name + ".setServer(sigReceiver);");
			}
			
			mL.accept(s);
			pw.println();
		}
		for (Iterator<Signal> it = d.getOutputSignalIterator(); it.hasNext();) {
			Signal s = it.next();
			pw.println(s.name + " = new " + Java.CLASS_SIGNAL + "();");

			if (systemConfig != null) {
				ClockDomainConfig cdCfg = systemConfig.getClockDomain(cdName);
				SignalConfig sigCfg = cdCfg.osignals.get(s.name);

				if (sigCfg == null)
					throw new RuntimeException("Unconfigured output signal " + cdName + "." + s.name);
				pw.println("sigSender = new " + sigCfg.clazz + "();");
				pw.println("ht = new Hashtable();");
				for (Map.Entry<String, String> entry : sigCfg.cfg.entrySet())
					pw.println("ht.put(\"" + entry.getKey() + "\", \"" + entry.getValue() + "\");");
				pw.println("sigSender.configure(ht);");
				pw.println(s.name + ".setClient(sigSender);");
			}
			
			mL.accept(s);
			pw.println();
		}
		for (Iterator<Signal> it = d.getInternalSignalIterator(); it.hasNext();) {
			Signal s = it.next();
			// TODO Check if internal pure signals are actually required to be created jop side
			if (s.type == null) pw.print("//");
			pw.println(s.name + " = new " + Java.CLASS_SIGNAL + "();");
			mL.accept(s);
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
	}
	
	private void printJavaFields(IndentPrinter pw, DeclaredObjects d, File dir) {
		String cdName = d.getCDName();
		Integer recopId = Helper.pMap.rAlloc != null ? Helper.pMap.rAlloc.get(cdName) : 0;

		pw.println("public static final String CDName = \"" + cdName + "\";");
		pw.println("public static final int recopId = " + recopId + ";");
		pw.println("private static java.util.Vector currentSignals;");
//		pw.println("public static " + Java.CLASS_INTERFACE_MANAGER + " im = null; // Note: Configured externally");
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
	}
	
	private void printJavaCDHK(IndentPrinter pw, DeclaredObjects d, MemoryPointer mp) {
		String cdName = d.getCDName();
		// House keeping
		pw.println("public static int housekeeping(int osigs, int[] dl) {");
		pw.incrementIndent();

		pw.println("// Set output signal statuses");
		Iterator<Signal> osigIt = d.getOutputSignalIterator();
		if (!osigIt.hasNext()) pw.println("// Note: No output signals for " + cdName);
		for (int i = 0; osigIt.hasNext(); i++) {
			Signal s = osigIt.next();
			pw.println("if ((osigs & " + (1 << i) + ") == 0) " + s.name + ".setClear(); else " + s.name + ".setPresent();");
		}

		pw.println();

		// START - Set preVal for internal valued signals which have been emitted this tick
		pw.println("// Set preval for valued signals which have been emitted this tick");
		pw.println("for (int i = 0; i < currentSignals.size(); i++) {");
		pw.incrementIndent();

		pw.println(Java.CLASS_SIGNAL + " sig = (" + Java.CLASS_SIGNAL + ") currentSignals.elementAt(i);");
		pw.println("if (sig.getStatus()) sig.setprepresent(); else sig.setpreclear();");
		pw.println("sig.setpreval(sig.getValue());");
		pw.println("sig.sethook();");

		pw.decrementIndent();
		pw.println("}");
		pw.println("currentSignals.removeAllElements();");
		pw.println();
		// START - GetHook for all input signals
		pw.println("// Update signals");
		for (Iterator<Signal> it = d.getInputSignalIterator(); it.hasNext();) {
			Signal s = it.next();
			pw.println(s.name + ".gethook();");
		}
		for (Iterator<Signal> it = d.getOutputSignalIterator(); it.hasNext();) {
			Signal s = it.next();
			pw.println(s.name + ".sethook();");
		}
		pw.println();
		
		// START - Update channels
		pw.println("// Update Channels");
		for (Iterator<Channel> it = d.getInputChannelIterator(); it.hasNext();) {
			Channel c = it.next();
			pw.println(c.name + "_in.gethook();");
			pw.println(c.name + "_in.sethook();");
		}
		for (Iterator<Channel> it = d.getOutputChannelIterator(); it.hasNext();) {
			Channel c = it.next();
			pw.println(c.name + "_o.gethook();");
			pw.println(c.name + "_o.sethook();");
		}
		pw.println();

		pw.println("// Get input signal statues");
		pw.println("int isigs = 0x800;");
		Iterator<Signal> isigIt = d.getInputSignalIterator();
		if (!isigIt.hasNext()) pw.println("// Note: No input signals for " + cdName);
		for (int i = 0; isigIt.hasNext(); i++) {
			Signal s = isigIt.next();
			pw.println("if (" + s.name + ".getStatus()) isigs |= " + (1 << i)+";");
		}
		pw.println();
		pw.println("// Write to isigs memory address");
		pw.println("dl[0] = " + mp.getDataLockPointer() + ";");

		pw.println("return isigs;");

		pw.decrementIndent();
		pw.println("}");

		pw.println();
	}
	
	private void printJavaClockDomainDistributed(File dir, MemoryPointer mp, List<ActionNode> actlists, DeclaredObjects d, int cdi, int jopID) throws FileNotFoundException {
		if (!actlists.isEmpty() || jopID == 0) {
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

			pw.println("package "+dir.getPath().replace("\\", ".").replace("/", ".")+";\n");
			pw.println();
			pw.println("import java.util.Hashtable;");
			pw.println();
			
			imports.forEach(v -> pw.println(v+"\n"));
			
			pw.println("public class "+cdName+"{");
			pw.incrementIndent();

			printJavaFields(pw, d, dir);

			printJavaCDInit(pw, d);

			if (jopID == 0)
				printJavaCDHK(pw, d, mp);
			
			if (jopID != 0){
				// Inserting non-io house-keeping actions to the list

				printJavaCDMethods(pw, mp, actlists, cdi);
			}

			pw.decrementIndent();
			pw.println("}");
			pw.flush();
			pw.close();
		}
	}
	
	private void printJavaCDMethods(IndentPrinter pw, MemoryPointer mp, List<ActionNode> l, int cdi) {
		// Non IO-JOP stuffs from here
		pw.println();

		pw.println("public static boolean MethodCall_0(int casen, int[] dl) {");
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
				pw.println("default: return MethodCall_" + (methodNum + 1) + "(casen);");
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

			switch (an.getActionType()) {
			case JAVA:
				if (an.getCasenumber() < 0)
					throw new RuntimeException("Unresolved Action Case");

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
				if (an.getCasenumber() < 0)
					throw new RuntimeException("Unresolved Action Case");
				pw.println("case " + an.getCasenumber() + ":");
				pw.incrementIndent();
				pw.println("dl[0] = " + ((an.getThnum() - mp.getToplevelThnum()) + mp.getDataLockPointer()) + ";");
				for (String stmt : an.getStmts()) {
					pw.println(stmt);
				}
				pw.println("break;");
				pw.decrementIndent();
				numCasesGened++;
				caseGen = true;
				break;
			case EMIT:
				if (an.hasEmitVal()) {
					if (an.getCasenumber() < 0)
						throw new RuntimeException("Unresolved Action Case");
					pw.println("case " + an.getCasenumber() + ":");
					pw.incrementIndent();
					pw.println("dl[0] = " + ((an.getThnum() - mp.getToplevelThnum()) + mp.getDataLockPointer()) + ";");
					pw.println(an.getStmt() + "");
					pw.println("currentSignals.addElement(" + an.getSigName() + ");");
					pw.println("break;");
					pw.decrementIndent();

					numCasesGened++;
					caseGen = true;
				} else {
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
		pw.println("return false;");
		pw.println("}"); // Method end
		pw.println();
	}
	
	private void printJavaClockDomain(File dir, MemoryPointer mp, List<ActionNode> actlists, DeclaredObjects d, int cdi) throws FileNotFoundException {
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
		
		pw.println("package "+dir.getPath().replace("\\", ".").replace("/", ".")+";\n");
		pw.println();
		pw.println("import java.util.Hashtable;");
		pw.println();
		
		imports.forEach(v -> pw.println(v+"\n"));
		pw.println("public class "+cdName+"{");

		pw.incrementIndent();
		
		printJavaFields(pw, d, dir);
		printJavaCDInit(pw, d);
		printJavaCDHK(pw, d, mp);
		printJavaCDMethods(pw, mp, actlists, cdi);

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
		pw.println("try{");
		pw.incrementIndent();
		pw.println("while(true){");
		pw.incrementIndent();

		pw.println();
		pw.println("/* Retrieve cd and case numbers from ReCOP and assign them to 'cd' and 'case', respectively */");
		pw.println();

		pw.println("dpcr = Native.getDatacall(); // Note getDatacall() is native method from Bjoern's project, need to add import");
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
			pw.println("recopId = "+cdName+".recopId; // Set recop id");
			pw.println("status = "+cdName+".MethodCall_0(casen, dl) ? 3 : 2;");
			pw.println("break;");
			pw.decrementIndent();
		}
		
		pw.println("default: throw new RuntimeException(\"Unrecognized CD number :\"+cd);");
		pw.decrementIndent();
		pw.println("}");
		pw.println();
		pw.println("result = 0x80000000 /*Valid Result Bit*/ " +
				"| ((recopId & 0x7F) << 24) /*RecopId*/ " +
				"| ((dl[0] & 0xFFF) << 12) /*WritebackAddress*/ " +
				"| (status & 0xFFF); /*Status*/");
		// NOTE Results = 1|ReCOP_id(7)|WritebackAddress(12)|Result(12)
		pw.println("Native.setDatacallResult(result);");
		pw.decrementIndent();
		pw.println("}");
		pw.decrementIndent();
		pw.println("} catch (Exception e) {");
		pw.incrementIndent();
		pw.println("RTSMain.out.println(\"Error while executing RECOP \"+recopId+\" CASE \"+casen+\" \"+e.getMessage());");
		pw.println("System.exit(1);");
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
