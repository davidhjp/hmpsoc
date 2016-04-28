package com.systemj.hmpsoc.nodes;

import java.io.PrintWriter;

import com.systemj.hmpsoc.DeclaredObjects;
import com.systemj.hmpsoc.MemoryPointer;

import args.Helper;

public class TestLock extends BaseGRCNode {
	private String elseLabel;

	public String getElseLabel() {
		return elseLabel;
	}

	public void generateElseLabel(MemoryPointer mp, int cdi){
		elseLabel = "DCPENDING"+(mp.cc++)+"CD"+cdi;	
	}

	@Override
	public void weirdPrint(PrintWriter pw, MemoryPointer mp, int termcode,
			int cdi, BaseGRCNode directParent, DeclaredObjects doo) {
		long pc_ptr = mp.getProgramCounterPointer();
		long dl_ptr = mp.getDataLockPointer();
		long ttnum = this.thnum - mp.getToplevelThnum();
		pc_ptr += ttnum;
		dl_ptr += ttnum;
		pw.println("  STRPC $"+Long.toHexString(pc_ptr)+"; Testlock storing PC");
		pw.println("  LDR R10 $"+Long.toHexString(dl_ptr)+"; Loading datalock");
		if(!Helper.getSingleArgInstance().hasOption(Helper.DYN_DISPATCH_OPTION))
			generateElseLabel(mp, cdi);
		pw.println("  PRESENT R10 "+elseLabel+"; checking result");
		pw.println("  STR R11 $"+Long.toHexString(pc_ptr)+"; Clearing PC");
		this.getChild(0).weirdPrint(pw, mp, termcode, cdi, this, doo);
		String overelse = "TLBRANCH"+(mp.cc++)+"CD"+cdi;
		pw.println("  JMP "+overelse);
		pw.println(elseLabel);
		this.getChild(1).weirdPrint(pw, mp, termcode, cdi, this, doo);
		pw.print(overelse+" \n");
	}
}
