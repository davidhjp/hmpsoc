package com.systemj.hmpsoc.nodes;

import java.io.PrintWriter;

import com.systemj.hmpsoc.MemoryPointer;

public class AjoinNode extends BaseGRCNode {

	@Override
	public void weirdPrint(PrintWriter pw, MemoryPointer mp, int termcode,
			int cdi) {
		
		if(termcode != TerminateNode.MAX_TERM){
			long pc_ptr = mp.getProgramCounterPointer();
			long ttnum = this.thnum - mp.getToplevelThnum();
			pc_ptr += ttnum;
			pw.println("  STR R11 $"+Long.toHexString(pc_ptr)+"; Clearing PC");
			pw.println("  JMP AJOIN"+cdi+"; Normal termination");
		} else {
			pw.println("  JMP AJOIN"+cdi+"; data-call is pending");
		}
		
		
		return;
	}
}
