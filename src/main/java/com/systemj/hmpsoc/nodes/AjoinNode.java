package com.systemj.hmpsoc.nodes;

import java.io.PrintWriter;
import java.util.stream.IntStream;

import com.systemj.hmpsoc.DeclaredObjects;
import com.systemj.hmpsoc.MemoryPointer;

import args.Helper;

public class AjoinNode extends BaseGRCNode {

	private void setEOT(PrintWriter pw, MemoryPointer mp, int cdi, BaseGRCNode parent){
		if(Helper.SCHED_POLICY.equals(Helper.SCHED_1)){
//			long pc_ptr = mp.getProgramCounterPointer();
//			long dl_ptr = mp.getDataLockPointer();
//			pw.println("  STR R11 $" + Long.toHexString(dl_ptr) + "; Thread is locked");
//			pw.println("  IOCALL R10 #$" + Long.toHexString(0x8000 | cdi) + " ; EOT Datacall ; Format = 1|IO-JOP|CD-ID|OSigs");
//			pw.println("  LDR R1 #5");
//			final String label1 = "LOOP"+(mp.cc++)+"CD"+cdi;
//			final String label2 = "LOOP"+(mp.cc++)+"CD"+cdi;
//			pw.println(label2+"  PRESENT R1 "+label1);
//			pw.println("  SUBV R1 R1 #1");
//			pw.println(" JMP "+label2);
//			pw.println(label1);
//			pw.println("  STRPC $"+Long.toHexString(pc_ptr)+"; Testlock storing PC");
//			pw.println("  LDR R10 $"+Long.toHexString(dl_ptr)+"; Loading datalock");
//			pw.println("  PRESENT R10 AJOIN"+cdi+"; checking result");
			
			pw.println("  LDR R1 #1");
			pw.println("  STR R1 $"+Long.toHexString(mp.getEOTPointer())+"; setting EOT bit");
		}
	}

	@Override
	public void weirdPrint(PrintWriter pw, MemoryPointer mp, int termcode,
			int cdi, BaseGRCNode directParent, DeclaredObjects doo) {
		
		if(directParent instanceof TerminateNode){
			TerminateNode parent = (TerminateNode)directParent;
			if(parent.getTermcode() == 1){
				long pc_ptr = mp.getProgramCounterPointer();
				long ttnum = parent.getThnum() - mp.getToplevelThnum();
				pc_ptr += ttnum;
				this.setEOT(pw, mp, cdi, parent);
				pw.println("  STR R11 $"+Long.toHexString(pc_ptr)+"; Clearing PC (parent = term 1)");
				pw.println("  JMP AJOIN"+cdi+"; Normal termination");
			} else if (parent.getTermcode() == TerminateNode.MAX_TERM){
				if(Helper.SCHED_POLICY.equals(Helper.SCHED_2)){
					pw.println("  JMP RUN"+cdi+" ; Re-run this CD again if EOT is not reached - sched2");
				} else
					pw.println("  JMP AJOIN"+cdi+"; data-call is pending (parent = term inf)");
			}
		} else if(directParent instanceof EnterNode){
			EnterNode parent = (EnterNode)directParent;
			long pc_ptr = mp.getProgramCounterPointer();
			long ttnum = parent.getThnum() - mp.getToplevelThnum();
			pc_ptr += ttnum;
			this.setEOT(pw, mp, cdi, parent);
			pw.println("  STR R11 $"+Long.toHexString(pc_ptr)+"; Clearing PC (parent == enternode)");
			pw.println("  JMP AJOIN"+cdi+"; Normal termination");
		} 
		
		return;
	}
}
