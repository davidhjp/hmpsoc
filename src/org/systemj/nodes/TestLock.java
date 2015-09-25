package org.systemj.nodes;

import java.io.PrintWriter;

import org.systemj.MemoryPointer;

public class TestLock extends BaseGRCNode {

	@Override
	public void weirdPrint(PrintWriter pw, MemoryPointer mp, int termcode,
			int cdi) {
		long pc_ptr = mp.getProgramCounterPointer();
		long dl_ptr = mp.getDataLockPointer();
		long ttnum = this.thnum - mp.getToplevelThnum();
		pc_ptr += ttnum;
		dl_ptr += ttnum;
		pw.println("  STRPC $"+Long.toHexString(pc_ptr)+"; Testlock storing PC");
		pw.println("  LDR R10 $"+Long.toHexString(dl_ptr)+"; Loading datalock");
		String label = "DCPENDING"+(mp.cc++)+"CD"+cdi;
		pw.println("  PRESENT R10 "+label+"; checking result");
		pw.println("  LDR R11 $"+Long.toHexString(pc_ptr)+"; Clearing PC");
		this.getChild(0).weirdPrint(pw, mp, termcode, cdi);
		pw.print(label);
		this.getChild(1).weirdPrint(pw, mp, termcode, cdi);
	}
	
}
