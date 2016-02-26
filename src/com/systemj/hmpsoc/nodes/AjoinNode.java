package com.systemj.hmpsoc.nodes;

import java.io.PrintWriter;

import com.systemj.hmpsoc.MemoryPointer;

public class AjoinNode extends BaseGRCNode {

	@Override
	public void weirdPrint(PrintWriter pw, MemoryPointer mp, int termcode,
			int cdi) {
		
		
		pw.println("  JMP AJOIN"+cdi+"; Normal termination");
		return;
	}
}
