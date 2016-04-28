package com.systemj.hmpsoc.nodes;

import java.io.PrintWriter;

import com.systemj.hmpsoc.DeclaredObjects;
import com.systemj.hmpsoc.MemoryPointer;

public class TestLock extends BaseGRCNode {
	public enum TYPE {
		DCALL, DISPATCH
	}
	
	private TYPE type = TYPE.DCALL;

	public TYPE getType() {
		return type;
	}
	
	public void setType(TYPE t) {
		type = t;
	}

	@Override
	public void weirdPrint(PrintWriter pw, MemoryPointer mp, int termcode,
			int cdi, BaseGRCNode directParent, DeclaredObjects doo) {
		switch(type){
		case DCALL:
			long pc_ptr = mp.getProgramCounterPointer();
			long dl_ptr = mp.getDataLockPointer();
			long ttnum = this.thnum - mp.getToplevelThnum();
			pc_ptr += ttnum;
			dl_ptr += ttnum;
			pw.println("  STRPC $"+Long.toHexString(pc_ptr)+"; Testlock storing PC");
			pw.println("  LDR R10 $"+Long.toHexString(dl_ptr)+"; Loading datalock");
			String label = "DCPENDING"+(mp.cc++)+"CD"+cdi;
			pw.println("  PRESENT R10 "+label+"; checking result");
			pw.println("  STR R11 $"+Long.toHexString(pc_ptr)+"; Clearing PC");
			this.getChild(0).weirdPrint(pw, mp, termcode, cdi, this, doo);
			String overelse = "TLBRANCH"+(mp.cc++)+"CD"+cdi;
			pw.println("  JMP "+overelse);
			pw.println(label);
			this.getChild(1).weirdPrint(pw, mp, termcode, cdi, this, doo);
			pw.print(overelse+" \n");
			break;
		case DISPATCH:
			pc_ptr = mp.getProgramCounterPointer();
			ttnum = this.thnum - mp.getToplevelThnum();
			pc_ptr += ttnum;
			pw.println("  STRPC $"+Long.toHexString(pc_ptr)+"; Testlock storing PC");
			pw.println("  LSIP R10");
			pw.println("  AND R10 R10 #1");
			label = "DPCRCLREARED"+(mp.cc++)+"CD"+cdi;
			pw.println("  PRESENT R10 "+label+"; checking if it is okay to launch dcall");
			this.getChild(1).weirdPrint(pw, mp, termcode, cdi, this, doo);
			overelse = "TLBRANCH"+(mp.cc++)+"CD"+cdi;
			pw.println("  JMP "+overelse);
			pw.println(label);
			this.getChild(0).weirdPrint(pw, mp, termcode, cdi, this, doo);
			pw.print(overelse+" \n");
			break;
		}
	}
}
