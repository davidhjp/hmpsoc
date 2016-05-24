package com.systemj.hmpsoc.nodes;

import java.io.PrintWriter;

import com.systemj.hmpsoc.DeclaredObjects;
import com.systemj.hmpsoc.MemoryPointer;

import args.Helper;

public class AjoinNode extends BaseGRCNode {

	private void setEOT(PrintWriter pw, MemoryPointer mp, int cdi){
		if(Helper.SCHED_POLICY.equals(Helper.SCHED_1)){
			pw.println("  LDR R1 #1");
			pw.println("  STR R1 $"+Long.toHexString(mp.getEOTPointer())+"; setting EOT bit");
			// TODO: need to send an EOT datacall
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
				pw.println("  STR R11 $"+Long.toHexString(pc_ptr)+"; Clearing PC (parent = term 1)");
				this.setEOT(pw, mp, cdi);
				pw.println("  JMP AJOIN"+cdi+"; Normal termination");
			} else if (parent.getTermcode() == TerminateNode.MAX_TERM){
				pw.println("  JMP AJOIN"+cdi+"; data-call is pending (parent = term inf)");
			}
		} else if(directParent instanceof EnterNode){
			EnterNode parent = (EnterNode)directParent;
			long pc_ptr = mp.getProgramCounterPointer();
			long ttnum = parent.getThnum() - mp.getToplevelThnum();
			pc_ptr += ttnum;
			pw.println("  STR R11 $"+Long.toHexString(pc_ptr)+"; Clearing PC (parent == enternode)");
			this.setEOT(pw, mp, cdi);
			pw.println("  JMP AJOIN"+cdi+"; Normal termination");
		} 
		
		return;
	}
}
