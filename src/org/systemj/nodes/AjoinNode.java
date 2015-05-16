package org.systemj.nodes;

import java.io.PrintWriter;

import org.systemj.MemoryPointer;

public class AjoinNode extends BaseGRCNode {

	@Override
	public void weirdPrint(PrintWriter pw, MemoryPointer mp, int termcode,
			int cdi) {
		
		
		pw.println("  JMP AJOIN"+cdi+"; Normal termination");
		return;
	}
}
